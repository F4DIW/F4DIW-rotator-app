package fr.f4diw.rotatorapp.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import fr.f4diw.rotatorapp.databinding.FragmentSettingsDataSourcesBinding

class DataSourcesSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsDataSourcesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsDataSourcesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        binding.etAdsbUrl.setText(prefs.getString("url_adsb", "http://82.64.206.3:8504/tar1090"))
        binding.etRadiosondeUrl.setText(prefs.getString("url_radiosondes", "https://api.v2.sondehub.org"))
        binding.etTrackingRadius.setText(prefs.getInt("tracking_radius", 400).toString())
        binding.swAmateurBalloons.isChecked = prefs.getBoolean("include_amateur", true)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val adsb = binding.etAdsbUrl.text.toString()
            val radiosondes = binding.etRadiosondeUrl.text.toString()
            val radius = binding.etTrackingRadius.text.toString().toIntOrNull() ?: 400
            val includeAmateur = binding.swAmateurBalloons.isChecked

            prefs.edit()
                .putString("url_adsb", adsb)
                .putString("url_radiosondes", radiosondes)
                .putInt("tracking_radius", radius)
                .putBoolean("include_amateur", includeAmateur)
                .apply()

            Toast.makeText(requireContext(), "Paramètres enregistrés", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
