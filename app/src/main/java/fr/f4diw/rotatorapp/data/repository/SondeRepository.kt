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
        val lat = prefs.getFloat("station_lat", 0f).toDouble()
        val lon = prefs.getFloat("station_lon", 0f).toDouble()
        val radiusKm = prefs.getInt("tracking_radius", 400)
        val includeAmateur = prefs.getBoolean("include_amateur", true)
        
        val radiusMeters = radiusKm * 1000
        val results = mutableListOf<Sonde>()

        // 1. Fetch Professional Sondes
        val profUrl = "https://api.v2.sondehub.org/sondes?lat=$lat&lon=$lon&distance=$radiusMeters"
        results.addAll(fetchFromUrl(profUrl))

        // 2. Fetch Amateur Balloons if enabled
        if (includeAmateur) {
            val amateurUrl = "https://api.v2.sondehub.org/amateur/sondes?lat=$lat&lon=$lon&distance=$radiusMeters"
            results.addAll(fetchFromUrl(amateurUrl))
        }

        results.filter { it.lat != null && it.lon != null }
    }

    private fun fetchFromUrl(url: String): List<Sonde> {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "F4DIW-rotor-tracker/1.0")
            .build()
        
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val type = object : TypeToken<Map<String, Sonde>>() {}.type
                val rawData: Map<String, Sonde> = gson.fromJson(body, type)
                rawData.values.toList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
