package fr.f4diw.rotatorapp.ui.adsb

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.f4diw.rotatorapp.data.model.Aircraft
import fr.f4diw.rotatorapp.data.repository.AdsbRepository
import fr.f4diw.rotatorapp.utils.GeoUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdsbViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdsbRepository(application)
    
    private val _aircraftList = MutableStateFlow<List<AircraftWithDistance>>(emptyList())
    val aircraftList: StateFlow<List<AircraftWithDistance>> = _aircraftList

    private var isRefreshing = false

    fun startRefreshing() {
        if (isRefreshing) return
        isRefreshing = true
        viewModelScope.launch {
            while (isRefreshing) {
                try {
                    refreshData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000) // On repasse à 1s pour laisser l'API respirer et assurer la stabilité
            }
        }
    }

    fun stopRefreshing() {
        // On ne l'arrête plus forcément au onDestroyView du fragment pour garder les données au retour
    }

    private suspend fun refreshData() {
        val aircraft = repository.fetchAircraft()
        val prefs = getApplication<Application>().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("station_lat", 0f).toDouble()
        val lon = prefs.getFloat("station_lon", 0f).toDouble()

        val listWithDistance = aircraft.map {
            val dist = GeoUtils.haversineKm(lat, lon, it.lat!!, it.lon!!)
            AircraftWithDistance(it, dist)
        }.sortedBy { it.distanceKm }

        _aircraftList.value = listWithDistance
    }

    fun fetchPhoto(hex: String) {
        viewModelScope.launch {
            val url = repository.fetchPhotoForAircraft(hex)
            if (url != null) {
                // Force un refresh immédiat de la liste pour afficher la photo
                refreshData()
            }
        }
    }

    data class AircraftWithDistance(val aircraft: Aircraft, val distanceKm: Double)
}
