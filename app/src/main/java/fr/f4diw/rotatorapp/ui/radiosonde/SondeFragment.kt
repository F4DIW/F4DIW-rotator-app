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
            
            if (item.sonde.isAmateur) {
                holder.binding.tvSondeInfo.text = context.getString(R.string.label_sonde_info_amateur, 
                    item.sonde.getEffectiveSerial())
                
                val mod = item.sonde.modulation ?: ""
                holder.binding.tvSondeType.text = "${item.sonde.getDisplayFrequency()} MHz $mod".trim()

                // Show icon for HAB
                holder.binding.ivSonde.visibility = View.VISIBLE
                holder.binding.tvRsLabel.visibility = View.GONE
            } else {
                holder.binding.tvSondeInfo.text = context.getString(R.string.label_sonde_info_pro, 
                    item.sonde.getDisplayType(), item.sonde.getEffectiveSerial())
                holder.binding.tvSondeType.text = context.getString(R.string.label_sonde_freq, 
                    item.sonde.getDisplayFrequency())

                // Show "RS" text for PRO
                holder.binding.ivSonde.visibility = View.GONE
                holder.binding.tvRsLabel.visibility = View.VISIBLE
            }

            holder.binding.tvDistance.text = "%.1f km".format(item.distanceKm)

            // Change background image based on type
            val bgResId = if (item.sonde.isAmateur) {
                context.resources.getIdentifier("sonde_amateur_list_bg", "drawable", context.packageName)
            } else {
                context.resources.getIdentifier("sonde_pro_list_bg", "drawable", context.packageName)
            }

            if (bgResId != 0) {
                holder.binding.ivSondeBg.setImageResource(bgResId)
            } else {
                holder.binding.ivSondeBg.setImageDrawable(null)
            }
            
            holder.binding.cardSonde.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
