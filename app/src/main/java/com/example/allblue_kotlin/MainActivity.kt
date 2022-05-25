package com.example.allblue_kotlin

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allblue_kotlin.databinding.ActivityMainBinding
import com.example.allblue_kotlin.databinding.BluetoothDeviceListBinding

private var musicStatusBool = false

class MainActivity : AppCompatActivity() {

    private val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    private val devices_list : ArrayList<BluetoothDevice> = ArrayList()
    private val REQUEST_ENABLE_BT = 1
    private lateinit var binding: ActivityMainBinding



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Create Notification channel, Immediate run needed, No perf impact
        createNotificationChannel()

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

        binding.buttonPaired.setOnClickListener {
            startService(Intent(this, MediaPlayingService::class.java))
        }

        // Nullable items returning list of Bluetooth Objects of connected devices
        val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevice?.forEach { device ->
            devices_list.add(device)
            Toast.makeText(this, "$device", Toast.LENGTH_LONG).show()
        }

        binding.recyclerViewPairedDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPairedDevices.adapter = MainAdapter(devices_list)

        startService(Intent(this, MediaPlayingService::class.java))

        val br: BroadcastReceiver = MyBroadcastReceiver()
        val filter = IntentFilter("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED").apply {
            addAction("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED")
        }
        registerReceiver(br, filter)

    }

    override fun onStop(br) {
        super.onStop()
        unregisterReceiver(br)
    }

    override fun onDestroy(br) {
        super.onDestroy()
        unregisterReceiver(br)
    }

    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: String = "Main Activity Notificaion"
            val descriptionText = "Notifications of allBlue Application"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_DEFAULT_IMPORTANCE, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

class MyBroadcastReceiver : BroadcastReceiver() {
    private val TAG2 = "MyBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        musicStatusBool = intent!!.getBooleanExtra("data", false) ?: return

        Log.i(TAG2, "Intent received")
        Log.i(TAG2, "$intent")
        Log.i(TAG2, "$musicStatusBool")
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
