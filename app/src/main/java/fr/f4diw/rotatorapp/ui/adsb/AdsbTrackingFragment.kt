package fr.f4diw.rotatorapp.ui.adsb

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import fr.f4diw.rotatorapp.R
import fr.f4diw.rotatorapp.databinding.FragmentAdsbTrackingBinding
import fr.f4diw.rotatorapp.ui.control.ControlViewModel
import fr.f4diw.rotatorapp.utils.GeoUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdsbTrackingFragment : Fragment() {

    private var _binding: FragmentAdsbTrackingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ControlViewModel by viewModels({ requireActivity() })
    private val adsbViewModel: AdsbViewModel by viewModels()

    private var targetHex: String = ""
    private var isTracking = false
    private var currentAz: Float = 0f
    private var currentEl: Float = 0f
    private var offsetAz: Float = 0f
    private var offsetEl: Float = 0f

    private val handler = Handler(Looper.getMainLooper())
    private val updateTask = object : Runnable {
        override fun run() {
            if (isTracking && currentEl >= -5f) { // Autorise un peu sous l'horizon pour les avions
                viewModel.repository.sendAzEl(currentAz + offsetAz, currentEl + offsetEl)
            }
            handler.postDelayed(this, 1000) // Mise à jour rotor toutes les secondes
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetHex = arguments?.getString(ARG_HEX) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdsbTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            adsbViewModel.aircraftList.collectLatest { list ->
                val aircraft = list.find { it.aircraft.hex == targetHex }
                aircraft?.let { updateUi(it) }
            }
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTrack.setOnClickListener {
            isTracking = !isTracking
            updateTrackButtonUi()
        }

        binding.btnJogUp.setOnClickListener { offsetEl += 0.5f }
        binding.btnJogDown.setOnClickListener { offsetEl -= 0.5f }
        binding.btnJogLeft.setOnClickListener { offsetAz -= 0.5f }
        binding.btnJogRight.setOnClickListener { offsetAz += 0.5f }

        adsbViewModel.startRefreshing()
        handler.post(updateTask)
    }

    private fun updateUi(data: AdsbViewModel.AircraftWithDistance) {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val obsLat = prefs.getFloat("station_lat", 0f).toDouble()
        val obsLon = prefs.getFloat("station_lon", 0f).toDouble()
        
        binding.tvTitle.text = data.aircraft.getDisplayFlight()
        
        val azEl = GeoUtils.calculateAzEl(
            obsLat, obsLon, GeoUtils.STATION_ALT,
            data.aircraft.lat!!, data.aircraft.lon!!, data.aircraft.getAltitudeMeters()
        )
        
        currentAz = azEl.first.toFloat()
        currentEl = azEl.second.toFloat()

        binding.tvAz.text = "AZ: %.1f°".format(currentAz)
        binding.tvEl.text = "EL: %.1f°".format(currentEl)
        binding.tvDistAlt.text = "Dist: %.1f km / Alt: %.0f m".format(data.distanceKm, data.aircraft.getAltitudeMeters())
    }

    private fun updateTrackButtonUi() {
        binding.btnTrack.text = getString(if (isTracking) R.string.btn_stop_track else R.string.btn_track)
        binding.btnTrack.setBackgroundColor(
            if (isTracking) resources.getColor(R.color.status_disconnected, null)
            else resources.getColor(R.color.status_connected, null)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adsbViewModel.stopRefreshing()
        handler.removeCallbacks(updateTask)
        _binding = null
    }

    companion object {
        private const val ARG_HEX = "target_hex"
        fun newInstance(hex: String) = AdsbTrackingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_HEX, hex)
            }
        }
    }
}
