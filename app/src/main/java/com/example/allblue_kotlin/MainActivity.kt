package com.example.allblue_kotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allblue_kotlin.databinding.ActivityMainBinding
import com.example.allblue_kotlin.databinding.BluetoothDeviceListBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val REQUEST_ENABLE_BT = 0
    private val devices_list : ArrayList<BluetoothDevice> = ArrayList()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        if (bluetoothAdapter?.isEnabled == false){
            Toast.makeText(this, "Bluetooth not supported on device", Toast.LENGTH_LONG).show()
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            @Suppress("DEPRECATION")
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Nullable items returning list of Bluetooth Objects of connected devices
        val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevice?.forEach { device ->
            devices_list.add(device)
            Toast.makeText(this, "$device", Toast.LENGTH_LONG).show()
        }
        binding.recyclerViewPairedDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPairedDevices.adapter = MainAdapter(devices_list)
    }
}
class MainAdapter(pairedDevice: ArrayList<BluetoothDevice>?) : RecyclerView.Adapter<CustomViewHolder>() {

    private val pDevice = pairedDevice

    override fun getItemCount(): Int {
        Log.i("getItemCount", pDevice.toString())
        return (pDevice?.size ?: Int) as Int
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = BluetoothDeviceListBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val pDevicePos = pDevice?.get(position)
        holder.binding.textViewName.text = pDevicePos.toString()
//        holder.binding.textViewAddress.text = pDevicePos.address
    }

}

class CustomViewHolder (val binding: BluetoothDeviceListBinding): RecyclerView.ViewHolder(binding.root){}
