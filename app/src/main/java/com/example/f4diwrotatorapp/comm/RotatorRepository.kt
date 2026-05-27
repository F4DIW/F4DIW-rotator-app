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

        // Polling position toutes les 500 ms quand connecté
        scope.launch {
            while (true) {
                if (bt.connected.value) {
                    bt.send(EasyCommProtocol.getPosition())
                    bt.send(EasyCommProtocol.getStatus())
                }
                delay(500L)
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

    fun reboot() = bt.send(EasyCommProtocol.reb