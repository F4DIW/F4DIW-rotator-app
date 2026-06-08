package fr.f4diw.rotatorapp.data.repository

import android.content.Context
import com.google.gson.Gson
import fr.f4diw.rotatorapp.data.model.AdsbResponse
import fr.f4diw.rotatorapp.data.model.Aircraft
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdsbRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchAircraft(): List<Aircraft> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val baseUrl = prefs.getString("url_adsb", "http://82.64.206.3:8504/tar1090") ?: "http://82.64.206.3:8504/tar1090"
        
        // On s'assure que l'URL finit par data/aircraft.json si ce n'est pas le cas
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
                adsbResponse.aircraft.filter { it.lat != null && it.lon != null }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
