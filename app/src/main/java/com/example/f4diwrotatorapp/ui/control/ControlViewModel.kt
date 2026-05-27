package com.example.f4diwrotatorapp.ui.control

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.f4diwrotatorapp.comm.BluetoothManager
import com.example.f4diwrotatorapp.comm.RotatorRepository
import com.example.f4diwrotatorapp.model.RotatorState
import com.example.f4diwrotatorapp.utils.GeoUtils
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val btManager = BluetoothManager(application.applicationContext)
    val repository = RotatorRepository(btManager)

    // ── Flux observables par le Fragment ──────────────────────────
    val state: StateFlow<RotatorState> = repository.state
    val co