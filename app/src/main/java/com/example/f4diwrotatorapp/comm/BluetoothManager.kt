package com.example.f4diwrotatorapp.comm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothManager(private val context: Context) {

    companion object {
        const val BT_NAME = "F4DIW-Rotator"
        val UUID_SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var listenJob: Job? = null

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val _incomingLines = MutableSharedFlow<String>()
    val incomingLines: SharedFlow<String> = _incomingLines.asSharedFlow()

    fun connectByName(name: String? = null) {
        if (_connected.value) return // Déjà connecté, on ne fait rien
        try {
            val targetName = name ?: context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("bt_name", BT_NAME) ?: BT_NAME
                
            val device = adapter?.bondedDevices?.find { it.name == targetName }
            if (device != null) {
                connect(device)
            } else {
                _lastError.value = "Device $targetName not found in paired devices"
            }
        } catch (e: SecurityException) {
            _lastError.value = "Bluetooth permission missing"
        }
    }

    private fun connect(device: BluetoothDevice) {
        listenJob?.cancel()
        listenJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Pour l'ESP32, le socket "Insecure" est beaucoup plus stable
                socket = try {
                    device.createInsecureRfcommSocketToServiceRecord(UUID_SPP)
                } catch (e: Exception) {
                    // Fallback ultime
                    val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    method.invoke(device, 1) as BluetoothSocket
                }
                
                socket?.connect()

                reader = BufferedReader(InputStreamReader(socket?.inputStream))
                writer = PrintWriter(socket?.outputStream, true)

                // Petit délai pour laisser l'ESP32 se préparer
                delay(500)

                _connected.value = true
                _lastError.value = null

                while (isActive) {
                    val line = try {
                        reader?.readLine()
                    } catch (e: Exception) {
                        null
                    } ?: break
                    _incomingLines.emit(line)
                }
            } catch (e: Exception) {
                _lastError.value = "Erreur : ${e.message}"
            } finally {
                disconnect()
            }
        }
    }

    fun disconnect() {
        _connected.value = false
        try {
            socket?.close()
        } catch (e: Exception) {
            // ignore
        }
        socket = null
        reader = null
        writer = null
        listenJob?.cancel()
        listenJob = null
    }

    fun send(data: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                writer?.print(data) // On utilise print car le protocole contient déjà \n
                writer?.flush()
            } catch (e: Exception) {
                _lastError.value = "Send failed: ${e.message}"
            }
        }
    }
}
