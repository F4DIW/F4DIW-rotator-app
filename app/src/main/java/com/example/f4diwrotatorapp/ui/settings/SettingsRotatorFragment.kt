package com.example.f4diwrotatorapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.f4diwrotatorapp.databinding.FragmentSettingsRotatorBinding
import com.example.f4diwrotatorapp.ui.control.ControlViewModel

class SettingsRotatorFragment : Fragment() {

    private var _binding: FragmentSettingsRotatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ControlViewModel by viewModels({ requireActivity() })

    private var isNegativeAz = false
    private var isNegativeEl = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsRotatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignAz.setOnClickListener {
            isNegativeAz = !isNegativeAz
            binding.btnSignAz.text = if (isNegativeAz) "-" else "+"
        }

        binding.btnSignEl.setOnClickListener {
            isNegativeEl = !isNegativeEl
            binding.btnSignEl.text = if (isNegativeEl) "-" else "+"
        }

        binding.btnSetZeroAz.setOnClickListener {
            val value = binding.etCalibAz.text.toString().toFloatOrNull() ?: 0f
            val offset = if (isNegativeAz) -value else value
            viewModel.repository.calibrateAzimuth(offset)
            Toast.makeText(requireContext(), "Calib Azimut: $offset°", Toast.LENGTH_SHORT).show()
        }

        binding.btnSetZeroEl.setOnClickListener {
            val value = binding.etCalibEl.text.toString().toFloatOrNull() ?: 0f
            val offset = if (isNegativeEl) -value else value
            viewModel.repository.calibrateElevation(offset)
            Toast.makeText(requireContext(), "Calib Élévation: $offset°", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
