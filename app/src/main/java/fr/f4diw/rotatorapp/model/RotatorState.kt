package fr.f4diw.rotatorapp.model

data class RotatorState(
    val az: Float = 0f,
    val el: Float = 0f,
    val status: Int = 0,
    val error: Int = 0,
    val version: String = "",
    val tracking: Boolean = false,
    val connected: Boolean = false
)