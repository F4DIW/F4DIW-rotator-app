package com.example.f4diwrotatorapp.ui.control

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.f4diwrotatorapp.databinding.FragmentControlBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ControlViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGo.setOnClickListener {
            val az = binding.etAz.text.toString().toFloatOrNull() ?: 0f
            val el = binding.etEl.text.toString().toFloatOrNull() ?: 0f
            viewModel.repository.sendAzEl(az, el)
        }

        binding.btnStop.setOnClickListener { viewModel.repository.stop() }
        binding.btnPark.setOnClickListener { viewModel.repository.park() }
        binding.btnReboot.setOnClickListener { viewModel.repository.reboot() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.tvAz.text = "AZ : ${state.az}"
                binding.tvEl.text = "EL : ${state.el}"
                binding.tvStatus.text = "Status : ${state.status}"
                binding.tvVersion.text = state.version
                
                binding.indicatorBt.setBackgroundColor(
                    if (state.connected) Color.GREEN else Color.RED
                )
                
                binding.btnGo.isEnabled = state.connected
                binding.btnStop.isEnabled = state.connected
                binding.btnPark.isEnabled = state.connected
                binding.btnReboot.isEnabled = state.connected
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
