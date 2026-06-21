package fr.f4diw.rotatorapp.ui.radiosonde

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.f4diw.rotatorapp.data.model.Sonde
import fr.f4diw.rotatorapp.data.repository.SondeRepository
import fr.f4diw.rotatorapp.utils.GeoUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SondeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SondeRepository(application)
    
    private val _sondeList = MutableStateFlow<List<SondeWithDistance>>(emptyList())
    val sondeList: StateFlow<List<SondeWithDistance>> = _sondeList

    private var isRefreshing = false

    fun startRefreshing() {
        if (isRefreshing) return
        isRefreshing = true
        viewModelScope.launch {
            while (isRefreshing) {
                refreshData()
                delay(30000) // SondeHub conseille 30s min
            }
        }
    }

    fun stopRefreshing() {
        isRefreshing = false
    }

    private suspend fun refreshData() {
        val sondes = repository.fetchSondes()
        val prefs = getApplication<Application>().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("station_lat", 0f).toDouble()
        val lon = prefs.getFloat("station_lon", 0f).toDouble()
        val radiusKm = prefs.getInt("tracking_radius", 400).toDouble()

        val now = System.currentTimeMillis()
        val maxAgeMs = 30 * 60 * 1000 // 30 minutes

        val listWithDistance = sondes.mapNotNull { sonde ->
            val dist = GeoUtils.haversineKm(lat, lon, sonde.lat!!, sonde.lon!!)
            
            // Filtrage par distance (déjà fait par l'API mais sécurité)
            if (dist > radiusKm) return@mapNotNull null
            
            // Filtrage par âge (en vol / données récentes)
            val ts = sonde.datetime ?: sonde.timeReceived
            if (ts != null) {
                try {
                    val dt = java.time.OffsetDateTime.parse(ts.replace("Z", "+00:00"))
                    val ageMs = now - dt.toInstant().toEpochMilli()
                    if (ageMs > maxAgeMs) return@mapNotNull null
                } catch (e: Exception) {
                    // Si on ne peut pas parser, on garde par précaution
                }
            }

            SondeWithDistance(sonde, dist)
        }.sortedBy { it.distanceKm }

        _sondeList.value = listWithDistance
    }

    data class SondeWithDistance(val sonde: Sonde, val distanceKm: Double)
}
