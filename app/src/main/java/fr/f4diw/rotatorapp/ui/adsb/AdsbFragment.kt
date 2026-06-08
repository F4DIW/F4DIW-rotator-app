package fr.f4diw.rotatorapp.ui.adsb

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import fr.f4diw.rotatorapp.R
import fr.f4diw.rotatorapp.databinding.FragmentAdsbBinding
import fr.f4diw.rotatorapp.databinding.ItemAircraftBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdsbFragment : Fragment() {

    private var _binding: FragmentAdsbBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdsbViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdsbBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AdsbAdapter { item ->
            val fragment = AdsbTrackingFragment.newInstance(item.aircraft.hex)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvAircraft.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.aircraftList.collectLatest { list ->
                adapter.submitList(list)
            }
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.startRefreshing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopRefreshing()
        _binding = null
    }

    class AdsbAdapter(private val onClick: (AdsbViewModel.AircraftWithDistance) -> Unit) :
        RecyclerView.Adapter<AdsbAdapter.ViewHolder>() {

        private var items = emptyList<AdsbViewModel.AircraftWithDistance>()

        fun submitList(newList: List<AdsbViewModel.AircraftWithDistance>) {
            items = newList
            notifyDataSetChanged()
        }

        class ViewHolder(val binding: ItemAircraftBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemAircraftBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvFlight.text = item.aircraft.getDisplayFlight()
            holder.binding.tvHex.text = item.aircraft.hex.uppercase()
            holder.binding.tvDistance.text = "%.1f km".format(item.distanceKm)

            holder.binding.ivAircraft.alpha = 0.5f
            holder.binding.ivAircraft.setPadding(12, 12, 12, 12)
            holder.binding.ivAircraft.setImageResource(android.R.drawable.ic_menu_send)
            
            holder.binding.cardAircraft.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
