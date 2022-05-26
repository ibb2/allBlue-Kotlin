package com.example.allblue_kotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.net.MacAddress
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.allblue_kotlin.databinding.BluetoothDeviceListBinding

class MainAdapter(private val pairedDevice: List<BluetoothDevice>?, private val onItemClick: (position: Int) -> Unit) : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    inner class MainViewHolder(val binding: BluetoothDeviceListBinding) : RecyclerView.ViewHolder(binding.root) {

//        fun bind(
//            position: Int,
//            onItemClick: (position: Int) -> Unit) {
//
//            // bind your view here
//            binding.root.setOnClickListener {
//                if (position != null) {
//                    onItemClick(position)
//                }
//            }
//
//        }

        fun bind(position: Int, onItemClick: (position: Int) -> Unit) {
            // bind your view here
            binding.root.setOnClickListener {
                onItemClick(position)
            }
        }
    }
    private val pDevices: List<BluetoothDevice>? = pairedDevice

    override fun getItemCount(): Int {
        return pDevices?.size ?: return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding = BluetoothDeviceListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.binding.apply {
            textViewName.text = pDevices?.get(position)?.name ?: return
            textViewAddress.text = pairedDevice?.get(position)?.address ?: return
        }
        holder.bind(holder.adapterPosition, onItemClick)
    }
}

data class Bluetooth(val name: String, val address: MacAddress)