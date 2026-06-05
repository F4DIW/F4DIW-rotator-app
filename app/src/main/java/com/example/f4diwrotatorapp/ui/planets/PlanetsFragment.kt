package com.example.f4diwrotatorapp.ui.planets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.f4diwrotatorapp.R
import com.example.f4diwrotatorapp.databinding.FragmentPlanetsBinding
import com.example.f4diwrotatorapp.databinding.ItemPlanetBinding
import io.github.cosinekitty.astronomy.*
import io.github.cosinekitty.astronomy.Observer as AstroObserver
import io.github.cosinekitty.astronomy.Time as AstroTime
import java.text.SimpleDateFormat
import java.util.*

class PlanetsFragment : Fragment() {

    private var _binding: FragmentPlanetsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlanetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.rvPlanets.layoutManager = LinearLayoutManager(requireContext())
        updatePlanetsList()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        binding.tvOrbitalUpdate.text = getString(R.string.label_last_orbital_update, sdf.format(Date()))
    }

    private fun updatePlanetsList() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("station_lat", 0f).toDouble()
        val lon = prefs.getFloat("station_lon", 0f).toDouble()
        
        val obs = AstroObserver(lat, lon, 0.0)
        val time = AstroTime.fromMillisecondsSince1970(System.currentTimeMillis())

        val bodies = listOf(
            Body.Sun, Body.Moon, Body.Mercury, Body.Venus, Body.Mars,
            Body.Jupiter, Body.Saturn, Body.Uranus, Body.Neptune, Body.Pluto
        )

        val planetData = bodies.map { body ->
            val equ = equator(body, time, obs, EquatorEpoch.OfDate, Aberration.Corrected)
            val hor = horizon(time, obs, equ.ra, equ.dec, Refraction.Normal)
            PlanetInfo(body, hor.azimuth, hor.altitude)
        }

        binding.rvPlanets.adapter = PlanetAdapter(planetData) { info ->
            val fragment = PlanetTrackingFragment.newInstance(info.body.name)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    data class PlanetInfo(val body: Body, val az: Double, val el: Double)

    class PlanetAdapter(
        private val items: List<PlanetInfo>,
        private val onClick: (PlanetInfo) -> Unit
    ) : RecyclerView.Adapter<PlanetAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemPlanetBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemPlanetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.itemView.context
            
            val nameResId = context.resources.getIdentifier("planet_${item.body.name.lowercase()}", "string", context.packageName)
            holder.binding.tvPlanetName.text = if (nameResId != 0) context.getString(nameResId) else item.body.name
            
            holder.binding.tvPlanetElevation.text = "%.1f°".format(item.el)
            
            val isVisible = item.el > 0
            holder.binding.cardPlanet.alpha = if (isVisible) 1.0f else 0.4f
            holder.binding.cardPlanet.strokeWidth = if (isVisible) 1 else 0
            holder.binding.tvPlanetStatus.text = context.getString(
                if (isVisible) R.string.status_visible else R.string.status_below_horizon
            )

            // Placeholder logic for planet icons
            val iconResId = context.resources.getIdentifier("ic_planet_${item.body.name.lowercase()}", "drawable", context.packageName)
            if (iconResId != 0) {
                holder.binding.ivPlanet.setImageResource(iconResId)
            }

            holder.binding.cardPlanet.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
