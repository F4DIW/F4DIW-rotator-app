package com.example.f4diwrotatorapp.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.f4diwrotatorapp.R
import com.example.f4diwrotatorapp.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        // Méthode moderne recommandée par Android pour changer la langue sans redémarrage manuel complexe
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        
        // Sauvegarde dans les préférences pour persistance (optionnel car géré par AppCompatDelegate sur API récentes)
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("language_code", languageCode).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
