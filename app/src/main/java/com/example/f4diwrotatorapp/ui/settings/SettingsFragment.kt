package com.example.f4diwrotatorapp.ui.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.f4diwrotatorapp.R
import com.example.f4diwrotatorapp.databinding.FragmentSettingsBinding
import com.example.f4diwrotatorapp.utils.GeoUtils
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadSavedPosition()

        binding.cardBluetooth.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BluetoothSettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardCalibration.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SettingsRotatorFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardLanguage.setOnClickListener {
            showLanguageDialog()
        }

        binding.btnGpsUpdate.setOnClickListener {
            updatePositionGps()
        }
    }

    private fun loadSavedPosition() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("station_lat", 0f).toDouble()
        val lon = prefs.getFloat("station_lon", 0f).toDouble()
        val lastUpdate = prefs.getString("station_last_update", "--/-- --:--")

        updateUiWithPosition(lat, lon, lastUpdate)
    }

    private fun updatePositionGps() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(requireActivity(), 
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 
                2)
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                saveAndDisplayLocation(location.latitude, location.longitude)
            } else {
                Toast.makeText(requireContext(), "Recherche du signal GPS...", Toast.LENGTH_SHORT).show()
                val priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
                fusedLocationClient.getCurrentLocation(priority, null).addOnSuccessListener { freshLocation ->
                    if (freshLocation != null) {
                        saveAndDisplayLocation(freshLocation.latitude, freshLocation.longitude)
                    } else {
                        Toast.makeText(requireContext(), "GPS indisponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveAndDisplayLocation(lat: Double, lon: Double) {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val lastUpdate = sdf.format(Date())

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat("station_lat", lat.toFloat())
            .putFloat("station_lon", lon.toFloat())
            .putString("station_last_update", lastUpdate)
            .apply()

        updateUiWithPosition(lat, lon, lastUpdate)
        Toast.makeText(requireContext(), "Position mise à jour", Toast.LENGTH_SHORT).show()
    }

    private fun updateUiWithPosition(lat: Double, lon: Double, lastUpdate: String?) {
        binding.tvLatLon.text = getString(R.string.label_lat_lon, lat, lon)
        binding.tvQth.text = getString(R.string.label_qth, GeoUtils.getQTHLocator(lat, lon))
        binding.tvLastUpdate.text = getString(R.string.label_last_update, lastUpdate ?: "--/-- --:--")
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Français", "English", "Русский")
        val codes = arrayOf("fr", "en", "ru")

        MaterialAlertDialogBuilder(requireContext(), R.style.Theme_F4DIWRotatorApp_Dialog)
            .setTitle(R.string.settings_language)
            .setItems(languages) { _, which ->
                setLocale(codes[which])
            }
            .show()
    }

    private fun setLocale(languageCode: String) {
        // Enregistre dans les préférences pour le prochain démarrage
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language_code", languageCode).apply()

        // Applique dynamiquement
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        
        // Sur Android 13+ (Pixel 7), l'activité est recréée automatiquement par setApplicationLocales
        // mais on peut forcer pour être sûr sur les versions intermédiaires
        requireActivity().recreate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
