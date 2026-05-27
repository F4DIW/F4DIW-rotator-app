package com.example.f4diwrotatorapp.model

data class SatPass(
    val name: String,
    val aos: Long,        // timestamp ms - Acquisition Of Signal
    val los: Long,        // timestamp ms - Loss Of Signal
    val maxEl: Float,     // élévation max en degrés
    val azStart: Float,   // azimut au AOS
    val azEnd: Float      // azimut au LOS
)