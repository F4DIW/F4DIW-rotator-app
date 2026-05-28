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
        const val BT_NAME = "SatNOGS-Rotator"
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

    fun connectByName(name: String = BT_NAME) {
        val device = adapter?.bondedDevices?.find { it.name == name }
        if (device != null) {
            connect(device)
        } else {
            _lastError.value = "Device $name not found in paired devices"
        }
    }

    private fun connect(device: BluetoothDevice) {
        listenJob?.cancel()
        listenJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = device.createRfcommSocketToServiceRecord(UUID_SPP)
                socket?.connect()

                reader = BufferedReader(InputStreamReader(socket?.inputStream))
                writer = PrintWriter(socket?.outputStream, true)

                _connected.value = true
                _lastError.value = null

                while (isActive) {
                    val line = reader?.readLine() ?: break
                    _incomingLines.emit(line)
                }
            } catch (e: IOException) {
                _lastError.value = e.message
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
                writer?.println(data)
            } catch (e: Exception) {
                _lastError.value = "Send failed: ${e.message}"
            }
        }
    }
}
