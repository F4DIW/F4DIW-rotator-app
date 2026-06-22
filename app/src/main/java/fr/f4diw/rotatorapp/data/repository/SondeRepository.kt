package fr.f4diw.rotatorapp.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.f4diw.rotatorapp.data.model.Sonde
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SondeRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchSondes(): List<Sonde> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val myLat = prefs.getFloat("station_lat", 0f).toDouble()
        val myLon = prefs.getFloat("station_lon", 0f).toDouble()
        val radiusKm = prefs.getInt("tracking_radius", 400).toDouble()
        val includeAmateur = prefs.getBoolean("include_amateur", true)
        
        val results = mutableListOf<Sonde>()

        // 1. SONDES PRO (Map<String, Sonde>)
        try {
            val profUrl = "https://api.v2.sondehub.org/sondes?lat=$myLat&lon=$myLon&distance=${radiusKm.toInt() * 1000}"
            fetchRaw(profUrl)?.let { body ->
                val type = object : TypeToken<Map<String, Sonde>>() {}.type
                val map: Map<String, Sonde> = gson.fromJson(body, type)
                map.forEach { (key, sonde) -> 
                    sonde.serial = key
                    sonde.isAmateur = false
                    results.add(sonde)
                }
            }
        } catch (e: Exception) { }

        // 2. BALLONS AMATEUR (Map<String, Map<String, Sonde>>)
        if (includeAmateur) {
            try {
                val amateurUrl = "https://api.v2.sondehub.org/amateur/telemetry?lat=$myLat&lon=$myLon&distance=${radiusKm.toInt() * 1000}&last=3600"
                fetchRaw(amateurUrl)?.let { body ->
                    val type = object : TypeToken<Map<String, Map<String, Sonde>>>() {}.type
                    val amateurData: Map<String, Map<String, Sonde>> = gson.fromJson(body, type)
                    
                    amateurData.forEach { (callsign, telemetryHistory) ->
                        val latestEntry = telemetryHistory.maxByOrNull { it.key }
                        latestEntry?.value?.let { sonde ->
                            sonde.serial = callsign
                            sonde.isAmateur = true
                            results.add(sonde)
                        }
                    }
                }
            } catch (e: Exception) { }
        }

        results.filter { it.lat != null && it.lon != null }
               .distinctBy { it.getEffectiveSerial() }
    }

    private fun fetchRaw(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android; F4DIW-Rotator)")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (e: Exception) { null }
    }
}
