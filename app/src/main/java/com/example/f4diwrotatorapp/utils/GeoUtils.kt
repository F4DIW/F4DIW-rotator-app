package com.example.f4diwrotatorapp.utils

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
}
