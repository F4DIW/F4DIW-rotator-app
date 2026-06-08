package fr.f4diw.rotatorapp.data.repository

import android.content.Context
import com.google.gson.Gson
import fr.f4diw.rotatorapp.data.model.AdsbResponse
import fr.f4diw.rotatorapp.data.model.Aircraft
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class AdsbRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    private val photoCache = mutableMapOf<String, String?>()
    private val failedAttempts = mutableSetOf<String>()
    private var lastApiCallTime = 0L

    suspend fun fetchAircraft(): List<Aircraft> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val baseUrl = prefs.getString("url_adsb", "http://82.64.206.3:8504/tar1090") ?: "http://82.64.206.3:8504/tar1090"
        
        val fullUrl = if (baseUrl.endsWith(".json")) baseUrl else {
            val base = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            "${base}data/aircraft.json"
        }

        val request = Request.Builder().url(fullUrl).build()
        
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val adsbResponse = gson.fromJson(body, AdsbResponse::class.java)
                val list = adsbResponse.aircraft.filter { it.lat != null && it.lon != null }
                
                list.forEach { it.photoUrl = photoCache[it.hex] }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchPhotoForAircraft(hex: String): String? = withContext(Dispatchers.IO) {
        if (photoCache.containsKey(hex) && photoCache[hex] != null) return@withContext photoCache[hex]
        if (failedAttempts.contains(hex)) return@withContext null

        // On ne met pas de délai si c'est la première requête depuis longtemps
        val now = System.currentTimeMillis()
        val timeSinceLastCall = now - lastApiCallTime
        if (timeSinceLastCall < 1000) {
            delay(1000)
        }
        lastApiCallTime = System.currentTimeMillis()

        val url = "https://api.planespotters.net/pub/photos/hex/${hex.lowercase()}"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "F4DIW-Rotator-App/1.2")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.code == 429) {
                    return@withContext null
                }
                if (!response.isSuccessful) {
                    failedAttempts.add(hex)
                    return@withContext null
                }
                val body = response.body?.string() ?: return@withContext null
                val psResponse = gson.fromJson(body, fr.f4diw.rotatorapp.data.model.PlanespottersResponse::class.java)
                val photoUrl = psResponse.photos.firstOrNull()?.thumbnailLarge?.src
                
                if (photoUrl != null) {
                    photoCache[hex] = photoUrl
                    photoUrl
                } else {
                    failedAttempts.add(hex)
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
