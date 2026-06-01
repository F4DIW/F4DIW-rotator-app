package com.example.f4diwrotatorapp.comm

import com.example.f4diwrotatorapp.model.RotatorState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RotatorRepository(private val bt: BluetoothManager) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow(RotatorState())
    val state: StateFlow<RotatorState> = _state.asStateFlow()

    val connected: StateFlow<Boolean> = bt.connected
    val lastError: StateFlow<String?> = bt.lastError

    init {
        // Écoute les réponses du firmware
        scope.launch {
            bt.incomingLines.collect { line ->
                when (val r = EasyCommProtocol.parse(line)) {
                    is EasyCommProtocol.Response.Position ->
                        _state.update { it.copy(az = r.az, el = r.el) }
                    is EasyCommProtocol.Response.Status ->
                        _state.update { it.copy(status = r.code) }
                    is EasyCommProtocol.Response.Error ->
                        _state.update { it.copy(error = r.code) }
                    is EasyCommProtocol.Response.Version ->
                        _state.update { it.copy(version = r.text) }
                    else -> {}
                }
            }
        }

        // Sync état connecté → RotatorState
        scope.launch {
            bt.connected.collect { isConnected ->
                _state.update { it.copy(connected = isConnected) }
            }
        }

        // Polling position toutes les 1000 ms quand connecté
        scope.launch {
            while (true) {
                if (bt.connected.value) {
                    bt.send(EasyCommProtocol.getPosition())
                }
                delay(1000L)
            }
        }
    }

    // ── Commandes ─────────────────────────────────────────────────
    fun sendAzEl(az: Float, el: Float) =
        bt.send(EasyCommProtocol.setAzEl(az, el))

    fun sendAzimuth(az: Float) =
        bt.send(EasyCommProtocol.setAzimuth(az))

    fun sendElevation(el: Float) =
        bt.send(EasyCommProtocol.setElevation(el))

    fun stop() {
        bt.send(EasyCommProtocol.stopAz())
        bt.send(EasyCommProtocol.stopEl())
    }

    fun park() = bt.send(EasyCommProtocol.park())

    fun reboot() = bt.send(EasyCommProtocol.reboot())

    fun getVersion() = bt.send(EasyCommProtocol.getVersion())

    fun connect() = bt.connectByName()

    // ── Calibration directe par Pas (Steps) ───────────────────────
    
    // Constantes basées sur le firmware F4DIW : Ratio 19.2, SPR 1600
    private val STEPS_PER_DEGREE = (1600.0 * 19.2) / 360.0

    fun calibrateAzimuth(offsetDegrees: Float) {
        scope.launch {
            val steps = (offsetDegrees * STEPS_PER_DEGREE).toLong()
            // 1. On force le mouvement du nombre de pas calculés
            bt.send("STEP_AZ $steps\n")
            
            // 2. On attend que le mouvement se termine (environ 2s)
            delay(2000)
            
            // 3. On définit cette nouvelle position comme le ZÉRO
            bt.send("SET_ZERO\n")
        }
    }

    fun calibrateElevation(offsetDegrees: Float) {
        scope.launch {
            val steps = (offsetDegrees * STEPS_PER_DEGREE).toLong()
            // 1. Mouvement forcé
            bt.send("STEP_EL $steps\n")
            
            // 2. Pause
            delay(2000)
            
            // 3. Définition du ZÉRO
            bt.send("SET_ZERO\n")
        }
    }
}
