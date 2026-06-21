package fr.f4diw.rotatorapp.ui.radiosonde

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
import fr.f4diw.rotatorapp.databinding.FragmentSondeTrackingBinding
import fr.f4diw.rotatorapp.ui.control.ControlViewModel
import fr.f4diw.rotatorapp.utils.GeoUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SondeTrackingFragment : Fragment() {

    private var _binding: FragmentSondeTrackingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ControlViewModel by viewModels({ requireActivity() })
    private val sondeViewModel: SondeViewModel by viewModels({ requireActivity() })

    private var targetSerial: String = ""
    private var isTracking = false
    private var currentAz: Float = 0f
    private var currentEl: Float = 0f
    private var offsetAz: Float = 0f
    private var offsetEl: Float = 0f

    private val handler = Handler(Looper.getMainLooper())
    private val updateTask = object : Runnable {
        override fun run() {
            if (isTracking && currentEl >= -5f) {
                viewModel.repository.sendAzEl(currentAz + offsetAz, currentEl + offsetEl)
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetSerial = arguments?.getString(ARG_SERIAL) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSondeTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            sondeViewModel.sondeList.collectLatest { list ->
                val sonde = list.find { it.sonde.getEffectiveSerial() == targetSerial }
                sonde?.let { updateUi(it) }
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

        sondeViewModel.startRefreshing()
        handler.post(updateTask)
    }

    private fun updateUi(data: SondeViewModel.SondeWithDistance) {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val obsLat = prefs.getFloat("station_lat", 0f).toDouble()
        val obsLon = prefs.getFloat("station_lon", 0f).toDouble()
        
        binding.tvTitle.text = data.sonde.getEffectiveSerial()
        
        val azEl = GeoUtils.calculateAzEl(
            obsLat, obsLon, GeoUtils.STATION_ALT,
            data.sonde.lat!!, data.sonde.lon!!, data.sonde.alt ?: 0.0
        )
        
        currentAz = azEl.first.toFloat()
        currentEl = azEl.second.toFloat()

        binding.tvAz.text = "AZ: %.1f°".format(currentAz)
        binding.tvEl.text = "EL: %.1f°".format(currentEl)
        binding.tvSondeDetails.text = "Freq: %s MHz / Alt: %.0f m".format(
            data.sonde.getDisplayFrequency(), data.sonde.alt ?: 0.0)

        // Color feedback on tracking page
        val iconColor = if (data.sonde.isAmateur) {
            resources.getColor(R.color.sonde_amateur, null)
        } else {
            resources.getColor(R.color.sonde_pro, null)
        }
        binding.ivSondeIcon.setColorFilter(iconColor)
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
        handler.removeCallbacks(updateTask)
        _binding = null
    }

    companion object {
        private const val ARG_SERIAL = "target_serial"
        fun newInstance(serial: String) = SondeTrackingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SERIAL, serial)
            }
        }
    }
}
