package fr.f4diw.rotatorapp.ui.planets

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import fr.f4diw.rotatorapp.R
import fr.f4diw.rotatorapp.databinding.FragmentPlanetTrackingBinding
import fr.f4diw.rotatorapp.ui.control.ControlViewModel
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
    private var currentAz: Float = 0f
    private var currentEl: Float = 0f

    private val handler = Handler(Looper.getMainLooper())
    private val updateTask = object : Runnable {
        override fun run() {
            updatePlanetPosition()
            
            // Mesure de sécurité : On n'envoie la position que si l'élévation est positive
            if (isTracking && currentEl >= 0f) {
                sendPositionToRotator()
            } else if (isTracking && currentEl < 0f) {
                // Si l'astre passe sous l'horizon pendant le tracking, on arrête par sécurité
                isTracking = false
                updateTrackButtonUi()
                Toast.makeText(requireContext(), R.string.status_below_horizon, Toast.LENGTH_LONG).show()
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
            if (currentEl < 0f && !isTracking) {
                Toast.makeText(requireContext(), R.string.status_below_horizon, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            isTracking = !isTracking
            updateTrackButtonUi()
        }

        handler.post(updateTask)
    }

    private fun updateTrackButtonUi() {
        binding.btnTrack.text = getString(if (isTracking) R.string.btn_stop_track else R.string.btn_track)
        binding.btnTrack.setBackgroundColor(
            if (isTracking) resources.getColor(R.color.status_disconnected, null)
            else resources.getColor(R.color.status_connected, null)
        )
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

            currentAz = hor.azimuth.toFloat()
            currentEl = hor.altitude.toFloat()

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
        viewModel.repository.sendAzEl(currentAz, currentEl)
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
