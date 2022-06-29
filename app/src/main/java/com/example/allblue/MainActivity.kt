package com.example.allblue

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.allblue.databinding.ActivityMainBinding
import java.util.*

private var musicStatusBool = false
private val MY_UUID = UUID.fromString("81e615ee-072b-467c-b28f-5b60ad934e38")
lateinit var bluetoothAdapter: BluetoothAdapter

class MainActivity : AppCompatActivity() {

    private val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    private val devices_list : ArrayList<BluetoothDevice> = ArrayList()
    private val REQUEST_ENABLE_BT = 1
    private val TAG = "Main Activity"
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //check android12+
        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.d("test006", "${it.key} = ${it.value}")
                }
            }

        var requestBluetooth =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    //granted
                } else {
                    //deny
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        // Create Notification channel, Immediate run needed, No perf impact
        createNotificationChannel()

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth not supported on device", Toast.LENGTH_LONG).show()
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            @Suppress("DEPRECATION")
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        // Nullable items returning list of Bluetooth Objects of connected devices
        val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevice?.forEach { device ->
            devices_list.add(device)
        }

        // Display selected device to connect to
        val sharedPrefBluetooth = getSharedPreferences("Selected Bluetooth Device",Context.MODE_PRIVATE) ?: return
        binding.tvBluetoothName.text = sharedPrefBluetooth.getString("name", "")
        binding.tvBluetoothAddress.text = sharedPrefBluetooth.getString("address", "")

        // Recyclerview
        binding.recyclerViewPairedDevices.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPairedDevices.adapter = MainAdapter(devices_list, {position -> onItemClick(position)})

        // Broadcast Receiver Val
        val br: BroadcastReceiver = MyBroadcastReceiver()

        // Start Stop service button
        val sharedPrefService = getSharedPreferences("Service Running", Context.MODE_PRIVATE)
        val serviceStatus = sharedPrefService.getBoolean("status", false)

        if (serviceStatus) {
            startService(Intent(this, MediaPlayingService::class.java))
            binding.btnService.text = resources.getString(R.string.stopallblue_service)
        } else {
            binding.btnService.text = resources.getString(R.string.startallblue_service)
        }

        binding.btnService.setOnClickListener {

            val textOfBtn = binding.btnService.text

            if (textOfBtn == "Stop Service") {
                stopService(Intent(this, MediaPlayingService::class.java))
                binding.btnService.text = resources.getString(R.string.startallblue_service)
            } else {
                startService(Intent(this, MediaPlayingService::class.java))
                binding.btnService.text = resources.getString(R.string.stopallblue_service)
            }
        }

        // Register Broadcast Receiver
        val filter = IntentFilter("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED").apply {
            addAction("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED")
        }
        registerReceiver(br, filter)
    }

    @SuppressLint("MissingPermission")
    private fun onItemClick(position: Int) {
        // Save information of selected bluetooth device to connect to
        val TAG3 = "Recyclerview onClickListener"
        val devicePos = devices_list[position]

        // Using preferences, to store key-value pairs
        val sharedPref = getSharedPreferences("Selected Bluetooth Device",Context.MODE_PRIVATE)
        val parcelUUID = devicePos.getUuids()
        val asString = parcelUUID[0].toString()
        with(sharedPref.edit()) {
            putString("uuid", asString)
            putString("name", devicePos.name)
            putString("address", devicePos.address.toString())
            apply()
        }

        binding.tvBluetoothName.text = sharedPref.getString("name", "")
        binding.tvBluetoothAddress.text = sharedPref.getString("address", "")

    }

    override fun onDestroy() {
        super.onDestroy()
        val br: BroadcastReceiver = MyBroadcastReceiver()
        unregisterReceiver(br)
    }

    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Main Activity Notificaion"
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

class StoreBluetoothDevice {
    //TODO store selected bluetooth information
}

class MyBroadcastReceiver : BroadcastReceiver() {
    private val TAG2 = "MyBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        musicStatusBool = intent!!.getBooleanExtra("data", false)

        // Connect to chosen bluetooth device.
        val cThread = ConnectThread(context)
        cThread.run()
        cThread.cancel()

    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(context: Context?) : Thread() {

        private val sharedPref = context?.getSharedPreferences("Selected Bluetooth Device", Context.MODE_PRIVATE)
        val bluetoothDeviceUUID: UUID = UUID.fromString(sharedPref?.getString("uuid",
            MY_UUID.toString()))
        val macAddress: String? = sharedPref?.getString("address", MY_UUID.toString())
        val device = bluetoothAdapter.getRemoteDevice(macAddress)


        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device?.createRfcommSocketToServiceRecord(bluetoothDeviceUUID)
        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            mmSocket?.close()
        }
    }

}