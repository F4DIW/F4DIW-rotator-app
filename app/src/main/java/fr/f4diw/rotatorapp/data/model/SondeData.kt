package fr.f4diw.rotatorapp.data.model

import com.google.gson.annotations.SerializedName

data class Sonde(
    val serial: String,
    val type: String?,
    val lat: Double?,
    val lon: Double?,
    val alt: Double?,
    @SerializedName("vel_h") val velH: Double?,
    @SerializedName("vel_v") val velV: Double?,
    val datetime: String?,
    @SerializedName("time_received") val timeReceived: String?,
    val freq: Double?,
    val frequency: Double?,
    @SerializedName("uploader_callsign") val uploaderCallsign: String?
) {
    fun getDisplayFrequency(): String {
        val f = freq ?: frequency ?: return "--"
        return "%.3f".format(f)
    }

    fun getDisplayType(): String = type ?: "Unknown"
}
