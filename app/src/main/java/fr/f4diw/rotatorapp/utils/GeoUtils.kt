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

    /**
     * Calcule l'Azimut et l'Elévation d'une cible par rapport à un observateur.
     * @return Pair(Azimut, Elévation) en degrés.
     */
    fun calculateAzEl(
        obsLat: Double, obsLon: Double, obsAlt: Double,
        targetLat: Double, targetLon: Double, targetAlt: Double
    ): Pair<Double, Double> {
        val lat1 = Math.toRadians(obsLat)
        val lon1 = Math.toRadians(obsLon)
        val lat2 = Math.toRadians(targetLat)
        val lon2 = Math.toRadians(targetLon)

        val dLon = lon2 - lon1

        // Azimut
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var az = Math.toDegrees(atan2(y, x))
        if (az < 0) az += 360.0

        // Elévation (simplifiée avec courbure de la Terre)
        val distance = haversineKm(obsLat, obsLon, targetLat, targetLon) * 1000.0 // en mètres
        val deltaAlt = targetAlt - obsAlt

        // El = atan( (deltaH) / D ) - (D / 2R) -> Approximation
        // Pour plus de précision on utilise la trigo sphérique ou ECEF
        // Ici on reste simple pour les avions
        val el = Math.toDegrees(atan2(deltaAlt, distance))

        return Pair(az, el)
    }
}
