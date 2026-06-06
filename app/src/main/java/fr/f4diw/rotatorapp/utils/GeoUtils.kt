package fr.f4diw.rotatorapp.utils

import kotlin.math.*

object GeoUtils {

    // ── Station F4DIW ─────────────────────────────────────────────
    const val STATION_LAT = 48.754
    const val STATION_LON = 2.555
    const val STATION_ALT = 104.0  // mètres
    const val CALLSIGN = "F4DIW"

    // ── Constantes ────────────────────────────────────────────────
    private const val EARTH_RADIUS_KM = 6371.0

    // ── Distance Haversine ────────────────────────────────────────
    fun haversineKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    // ── Calcul du QTH Locator (Maidenhead) ────────────────────────
    fun getQTHLocator(lat: Double, lon: Double): String {
        var lonRel = lon + 180.0
        var latRel = lat + 90.0

        val field1 = ('A'.code + (lonRel / 20.0).toInt()).toChar()
        val field2 = ('A'.code + (latRel / 10.0).toInt()).toChar()

        lonRel %= 20.0
        latRel %= 10.0
        val square1 = ('0'.code + (lonRel / 2.0).toInt()).toChar()
        val square2 = ('0'.code + (latRel / 1.0).toInt()).toChar()

        lonRel %= 2.0
        latRel %= 1.0
        val subsquare1 = ('a'.code + (lonRel * 12.0).toInt()).toChar().uppercaseChar()
        val subsquare2 = ('a'.code + (latRel * 24.0).toInt()).toChar().uppercaseChar()

        return "$field1$field2$square1$square2$subsquare1$subsquare2"
    }
}
