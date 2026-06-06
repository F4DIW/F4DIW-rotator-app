package fr.f4diw.rotatorapp.ui.control

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import fr.f4diw.rotatorapp.databinding.FragmentControlBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ControlViewModel by viewModels({ requireActivity() })

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
        binding.btnConnect.setOnClickListener { viewModel.repository.connect() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.tvAz.text = "${state.az}°"
                binding.tvEl.text = "${state.el}°"
                binding.tvStatus.text = if (state.connected) "CONNECTÉ" else "DÉCONNECTÉ"
                binding.tvVersion.text = state.version
                
                binding.indicatorBt.setBackgroundColor(
                    if (state.connected) Color.GREEN else Color.RED
                )
                
                binding.btnGo.isEnabled = state.connected
                binding.btnStop.isEnabled = state.connected
                binding.btnPark.isEnabled = state.connected
                binding.btnConnect.isEnabled = !state.connected
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.repository.lastError.collectLatest { error ->
                if (error != null) {
                    binding.tvError.text = error
                    binding.tvError.visibility = View.VISIBLE
                } else {
                    binding.tvError.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
