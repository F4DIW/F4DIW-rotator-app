package fr.f4diw.rotatorapp.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.f4diw.rotatorapp.data.model.Sonde
import fr.f4diw.rotatorapp.utils.GeoUtils
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

        // 1. Sondes Professionnelles (Format Map/Dictionnaire)
        try {
            val profUrl = "https://api.v2.sondehub.org/sondes?lat=$myLat&lon=$myLon&distance=${radiusKm.toInt() * 1000}"
            val body = fetchRaw(profUrl)
            if (body != null) {
                val type = object : TypeToken<Map<String, Sonde>>() {}.type
                val map: Map<String, Sonde> = gson.fromJson(body, type)
                map.forEach { (key, sonde) -> 
                    if (sonde.serial == null) sonde.serial = key
                    sonde.isAmateur = false
                    results.add(sonde)
                }
            }
        } catch (e: Exception) { }

        // 2. Ballons Amateurs (Méthode de télémétrie groupée)
        if (includeAmateur) {
            try {
                // On demande la télémétrie des 2 dernières heures dans votre rayon
                val amateurUrl = "https://api.v2.sondehub.org/amateur/telemetry?lat=$myLat&lon=$myLon&distance=${radiusKm.toInt() * 1000}&duration=2h"
                val body = fetchRaw(amateurUrl)
                if (body != null) {
                    val type = object : TypeToken<List<Sonde>>() {}.type
                    val allTelemetry: List<Sonde> = gson.fromJson(body, type)
                    
                    // On ne garde que la position la plus récente pour chaque ballon (groupé par indicatif)
                    val latestAmateurs = allTelemetry
                        .filter { it.lat != null && it.lon != null }
                        .groupBy { it.getEffectiveSerial() }
                        .map { entry -> 
                            val s = entry.value.maxByOrNull { it.datetime ?: it.timeReceived ?: "" } !!
                            s.isAmateur = true
                            s
                        }
                    
                    results.addAll(latestAmateurs)
                }
            } catch (e: Exception) { }
        }

        // Déduplication et retour
        results.distinctBy { it.getEffectiveSerial() }
    }

    private fun fetchRaw(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0")
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (e: Exception) { null }
    }
}
