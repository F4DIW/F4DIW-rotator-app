package fr.f4diw.rotatorapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import fr.f4diw.rotatorapp.databinding.FragmentSettingsRotatorBinding
import fr.f4diw.rotatorapp.ui.control.ControlViewModel

class SettingsRotatorFragment : Fragment() {

    private var _binding: FragmentSettingsRotatorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ControlViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsRotatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Jog Commands (ML, MR, MU, MD) ──
        binding.btnJogUp.setOnClickListener {
            viewModel.repository.jogElUp()
            showToast("Élévation +1.0°")
        }

        binding.btnJogDown.setOnClickListener {
            viewModel.repository.jogElDown()
            showToast("Élévation -1.0°")
        }

        binding.btnJogLeft.setOnClickListener {
            viewModel.repository.jogAzLeft()
            showToast("Azimut -1.0°")
        }

        binding.btnJogRight.setOnClickListener {
            viewModel.repository.jogAzRight()
            showToast("Azimut +1.0°")
        }

        // ── Calibration (RST) ──
        binding.btnReset.setOnClickListener {
            viewModel.repository.reset()
            showToast("Position calibrée à 0.0°")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
