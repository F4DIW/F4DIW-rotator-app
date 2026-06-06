package fr.f4diw.rotatorapp.ui.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import fr.f4diw.rotatorapp.comm.BluetoothManager
import fr.f4diw.rotatorapp.comm.RotatorRepository
import fr.f4diw.rotatorapp.model.RotatorState
import kotlinx.coroutines.flow.StateFlow

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val btManager = BluetoothManager(application.applicationContext)
    val repository = RotatorRepository(btManager)

    // ── Flux observables par le Fragment ──────────────────────────
    val state: StateFlow<RotatorState> = repository.state

    init {
        btManager.connectByName()
    }

    override fun onCleared() {
        super.onCleared()
        btManager.disconnect()
    }
}
