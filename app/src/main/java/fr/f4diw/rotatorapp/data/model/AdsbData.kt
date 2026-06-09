package fr.f4diw.rotatorapp.data.model

import com.google.gson.annotations.SerializedName

data class AdsbResponse(
    val aircraft: List<Aircraft>
)

data class Aircraft(
    val hex: String,
    val flight: String?,
    @SerializedName("r") val registration: String?,
    @SerializedName("t") val type: String?,
    val lat: Double?,
    val lon: Double?,
    @SerializedName("alt_baro") val altBaro: Any?, // Peut être "ground" ou un nombre
    @SerializedName("alt_geom") val altGeom: Double?,
    val gs: Double?,
    val track: Double?,
    val category: String?,
    var photoUrl: String? = null
) {
    fun getDisplayFlight(): String = flight?.trim() ?: hex.uppercase()
    
    fun getAltitudeMeters(): Double {
        val altFeet = when (val alt = altBaro) {
            is Number -> alt.toDouble()
            is String -> alt.toDoubleOrNull() ?: 0.0
            else -> altGeom ?: 0.0
        }
        return altFeet * 0.3048 // Conversion pieds en mètres
    }
}

// Model for Planespotters.net API
data class PlanespottersResponse(
    val photos: List<PlanespottersPhoto>
)

data class PlanespottersPhoto(
    @SerializedName("thumbnail_large") val thumbnailLarge: PlanespottersImage
)

data class PlanespottersImage(
    val src: String
)

// Model for Airport-Data.com API
data class AirportDataResponse(
    val data: List<AirportDataItem>?
)

data class AirportDataItem(
    val image: String?
)
