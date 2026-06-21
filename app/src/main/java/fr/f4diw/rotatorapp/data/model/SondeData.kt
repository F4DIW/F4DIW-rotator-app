package fr.f4diw.rotatorapp.data.model

import com.google.gson.annotations.SerializedName

data class Sonde(
    @SerializedName("serial") var serial: String? = null,
    @SerializedName("callsign") val callsign: String? = null,
    @SerializedName("payload_name") val payloadName: String? = null,
    @SerializedName("payload_callsign") val payloadCallsign: String? = null,
    
    val type: String? = null,
    val model: String? = null,
    
    @SerializedName("lat", alternate = ["latitude"]) val lat: Double? = null,
    @SerializedName("lon", alternate = ["longitude"]) val lon: Double? = null,
    @SerializedName("alt", alternate = ["altitude"]) val alt: Double? = null,
    
    @SerializedName("vel_h") val velH: Double? = null,
    @SerializedName("vel_v") val velV: Double? = null,
    
    val datetime: String? = null,
    @SerializedName("time_received") val timeReceived: String? = null,
    
    val freq: Double? = null,
    val frequency: Double? = null,
    
    var isAmateur: Boolean = false
) {
    fun getEffectiveSerial(): String {
        return serial ?: callsign ?: payloadCallsign ?: payloadName ?: "Unknown"
    }

    fun getDisplayFrequency(): String {
        val f = freq ?: frequency ?: return "--"
        return "%.3f".format(f)
    }

    fun getDisplayType(): String = type ?: model ?: "Sonde"
}
