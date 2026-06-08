package fr.f4diw.rotatorapp.data.model

import com.google.gson.annotations.SerializedName

data class AdsbResponse(
    val aircraft: List<Aircraft>
)

data class Aircraft(
    val hex: String,
    val flight: String?,
    val lat: Double?,
    val lon: Double?,
    @SerializedName("alt_baro") val altBaro: Any?, // Peut être "ground" ou un nombre
    @SerializedName("alt_geom") val altGeom: Double?,
    val gs: Double?,
    val track: Double?,
    val category: String?
) {
    fun getDisplayFlight(): String = flight?.trim() ?: hex.uppercase()
    
    fun getAltitudeMeters(): Double {
        val altFeet = when (altBaro) {
            is Number -> altBaro.toDouble()
            else -> altGeom ?: 0.0
        }
        return altFeet * 0.3048 // Conversion pieds en mètres
    }
}
