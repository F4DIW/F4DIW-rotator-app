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

    privat