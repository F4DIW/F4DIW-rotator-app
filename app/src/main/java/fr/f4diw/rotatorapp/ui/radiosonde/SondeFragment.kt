package fr.f4diw.rotatorapp.ui.radiosonde

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import fr.f4diw.rotatorapp.R
import fr.f4diw.rotatorapp.databinding.FragmentSondeBinding
import fr.f4diw.rotatorapp.databinding.ItemSondeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SondeFragment : Fragment() {

    private var _binding: FragmentSondeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SondeViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSondeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SondeAdapter { item ->
            val fragment = SondeTrackingFragment.newInstance(item.sonde.getEffectiveSerial())
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvSondes.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sondeList.collectLatest { list ->
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
        _binding = null
    }

    class SondeAdapter(private val onClick: (SondeViewModel.SondeWithDistance) -> Unit) :
        RecyclerView.Adapter<SondeAdapter.ViewHolder>() {

        private var items = emptyList<SondeViewModel.SondeWithDistance>()

        fun submitList(newList: List<SondeViewModel.SondeWithDistance>) {
            items = newList
            notifyDataSetChanged()
        }

        class ViewHolder(val binding: ItemSondeBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSondeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context
            
            holder.binding.tvSondeInfo.text = context.getString(R.string.label_sonde_info, 
                item.sonde.getEffectiveSerial(), item.sonde.getDisplayFrequency())
            holder.binding.tvSondeType.text = context.getString(R.string.label_sonde_type, 
                item.sonde.getDisplayType())
            holder.binding.tvDistance.text = "%.1f km".format(item.distanceKm)

            // Change color based on type
            val bgColor = if (item.sonde.isAmateur) {
                context.getColor(R.color.sonde_amateur)
            } else {
                context.getColor(R.color.sonde_pro)
            }
            holder.binding.cardSonde.setCardBackgroundColor(bgColor)
            
            holder.binding.cardSonde.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
