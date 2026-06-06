package fr.f4diw.rotatorapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import fr.f4diw.rotatorapp.R
import fr.f4diw.rotatorapp.databinding.FragmentHomeBinding
import fr.f4diw.rotatorapp.ui.control.ControlFragment

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardManual.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ControlFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardLook4Sat.setOnClickListener {
            launchExternalApp("com.rtbishop.look4sat")
        }

        binding.cardPlanets.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fr.f4diw.rotatorapp.ui.planets.PlanetsFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardAdsb.setOnClickListener {
            Toast.makeText(requireContext(), "Bientôt disponible : Tracking ADS-B", Toast.LENGTH_SHORT).show()
        }

        binding.cardRadiosonde.setOnClickListener {
            Toast.makeText(requireContext(), "Bientôt disponible : Tracking Radiosondes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchExternalApp(packageName: String) {
        val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "L'application $packageName n'est pas installée", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
