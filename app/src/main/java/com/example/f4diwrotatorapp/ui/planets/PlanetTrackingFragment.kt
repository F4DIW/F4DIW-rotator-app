package com.example.f4diwrotatorapp.ui.planets

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.f4diwrotatorapp.R
import com.example.f4diwrotatorapp.databinding.FragmentPlanetTrackingBinding
import com.example.f4diwrotatorapp.ui.control.ControlViewModel
import io.github.cosinekitty.astronomy.*
import io.github.cosinekitty.astronomy.Observer as AstroObserver
import io.github.cosinekitty.astronomy.Time as AstroTime
import java.util.*

class PlanetTrackingFragment : Fragment() {

    private var _binding: FragmentPlanetTrackingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ControlViewModel by viewModels({ requireActivity() })

    private var planetName: String = ""
    private var isTracking = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateTask = object : Runnable {
        override fun run() {
            updatePlanetPosition()
            if (isTracking) {
                sendPositionToRotator()
            }
            handler.postDelayed(this, 200)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        planetName = arguments?.getString(ARG_PLANET_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanetTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val nameResId = requireContext().resources.getIdentifier("planet_${planetName.lowercase()}", "string", requireContext().packageName)
        binding.tvTitle.text = if (nameResId != 0) getString(nameResId) else planetName

        val iconResId = requireContext().resources.getIdentifier("ic_planet_${planetName.lowercase()}", "drawable", requireContext().packageName)
        if (iconResId != 0) {
            binding.ivPlanetBig.setImageResource(iconResId)
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTrack.setOnClickListener {
            isTracking = !isTracking
            binding.btnTrack.text = getString(if (isTracking) R.string.btn_stop_track else R.string.btn_track)
            binding.btnTrack.setBackgroundColor(
                if (isTracking) resources.getColor(R.color.status_disconnected, null)
                else resources.getColor(R.color.status_connected, null)
            )
        }

        handler.post(updateTask)
    }

    private fun updatePlanetPosition() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("station_lat", 0f).toDouble()
        val lon = prefs.getFloat("station_lon", 0f).toDouble()
        
        try {
            val body = Body.valueOf(planetName)
            val obs = AstroObserver(lat, lon, 0.0)
            val time = AstroTime.fromMillisecondsSince1970(System.currentTimeMillis())

            val equ = equator(body, time, obs, EquatorEpoch.OfDate, Aberration.Corrected)
            val hor = horizon(time, obs, equ.ra, equ.dec, Refraction.Normal)

            binding.tvAz.text = "AZ: %.1f°".format(hor.azimuth)
            binding.tvEl.text = "EL: %.1f°".format(hor.altitude)

            // Search rise 
            val nextRise = searchRiseSet(body, obs, Direction.Rise, time, 1.0)
            if (nextRise != null) {
                binding.tvNextRise.text = getString(R.string.label_next_rise, nextRise.toString())
            } else {
                binding.tvNextRise.text = getString(R.string.label_next_rise, "---")
            }
        } catch (e: Exception) {
            binding.tvNextRise.text = "Error"
        }
    }

    private fun sendPositionToRotator() {
        val azStr = binding.tvAz.text.toString().filter { it.isDigit() || it == '.' }
        val elStr = binding.tvEl.text.toString().filter { it.isDigit() || it == '.' }
        val az = azStr.toFloatOrNull() ?: 0f
        val el = elStr.toFloatOrNull() ?: 0f
        viewModel.repository.sendAzEl(az, el)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTask)
        _binding = null
    }

    companion object {
        private const val ARG_PLANET_NAME = "planet_name"

        fun newInstance(planetName: String) = PlanetTrackingFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PLANET_NAME, planetName)
            }
        }
    }
}
