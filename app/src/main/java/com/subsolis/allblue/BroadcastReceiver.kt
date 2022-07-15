package com.subsolis.allblue

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private var musicStatusBool = false
private val MY_UUID = UUID.fromString("81e615ee-072b-467c-b28f-5b60ad934e38")

@AndroidEntryPoint
class BroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var bluetoothRepository: BluetoothRepository

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        musicStatusBool = intent!!.getBooleanExtra("data", false)

        Log.d("BluetoothReceiver", "Is receining intent $musicStatusBool")
        // Connect to chosen bluetooth device.
        val cThread = ConnectThread(context)
        cThread.init()
        cThread.run()
        cThread.cancel()
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(context: Context?) : Thread() {

        val bluetoothManager: BluetoothManager? =
            context?.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter = bluetoothManager!!.adapter

        private lateinit var userBluetoothDevice: List<String?>
        private lateinit var bluetoothDeviceUUID: UUID
        private lateinit var macAddress: String
        private lateinit var device: BluetoothDevice
        private lateinit var mmSocket: BluetoothSocket

        @OptIn(DelicateCoroutinesApi::class)
        fun init() {
            GlobalScope.launch {
                userBluetoothDevice = bluetoothRepository.currentlySelectedDevice()
            }

            sleep(10)
            bluetoothDeviceUUID = UUID.fromString(userBluetoothDevice[0]) ?: MY_UUID
            macAddress = userBluetoothDevice[2]!!
            device = bluetoothAdapter.getRemoteDevice(macAddress)
            mmSocket = device.createRfcommSocketToServiceRecord(bluetoothDeviceUUID)

        }

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery()

            mmSocket.connect()
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            mmSocket.close()
        }
    }
}