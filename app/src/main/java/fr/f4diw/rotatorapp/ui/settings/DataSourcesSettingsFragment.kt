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
        binding.etAdsbUrl.setText(prefs.getString("url_adsb", "http://192.168.1.100:8080"))
        binding.etRadiosondeUrl.setText(prefs.getString("url_radiosondes", "http://192.168.1.100:8081"))

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val adsb = binding.etAdsbUrl.text.toString()
            val radiosondes = binding.etRadiosondeUrl.text.toString()

            prefs.edit()
                .putString("url_adsb", adsb)
                .putString("url_radiosondes", radiosondes)
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
