package com.example.f4diwrotatorapp.ui.settings

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.f4diwrotatorapp.R
import com.example.f4diwrotatorapp.databinding.FragmentSettingsBluetoothBinding
import com.example.f4diwrotatorapp.databinding.ItemDeviceBinding

class BluetoothSettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBluetoothBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // On réutilise le layout fragment_settings_bluetooth (que je vais créer juste après)
        _binding = FragmentSettingsBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvDevices.layoutManager = LinearLayoutManager(requireContext())
        refreshDeviceList()

        binding.btnRefresh.setOnClickListener {
            refreshDeviceList()
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshDeviceList() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val devices = adapter?.bondedDevices?.toList() ?: emptyList()
        
        binding.rvDevices.adapter = DeviceAdapter(devices) { device ->
            val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putString("bt_name", device.name).apply()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class DeviceAdapter(
        private val devices: List<BluetoothDevice>,
        private val onClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            holder.binding.tvDeviceName.text = device.name ?: "Inconnu"
            holder.binding.tvDeviceAddress.text = device.address
            holder.binding.root.setOnClickListener { onClick(device) }
        }

        override fun getItemCount() = devices.size
    }
}
