package com.example.allblue

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BluetoothState(
    val selectedDevice: UserDevice = UserDevice(),
    val pairedDevices: ArrayList<BluetoothDevice> = ArrayList(),
    val bluetoothEnabled: Boolean = false,
    val serviceActive: Boolean = false,
)

@HiltViewModel
class BluetoothViewModel @Inject constructor(private val bluetoothRepository: BluetoothRepository) : ViewModel() {

    private val _viewState = MutableStateFlow(BluetoothState())
    val viewState: StateFlow<BluetoothState> = _viewState

    fun checkBluetoothStatus(@ApplicationContext context: Context) {
        if (!bluetoothAdapter.isEnabled){
            _viewState.value = _viewState.value.copy(
                bluetoothEnabled = true
            )
        }
    }

    fun getSelectedDevice() {
        viewModelScope.launch {
            /*
            Function collects the first value with the first function on the SelectedDevice Flow object.
            Collect function does not seem to work. Solution will be to figure out how to store data class in
            object to be returned. As that seems to be the issue. This is functional now as only one item is returned
            therefore first() can be used.
            The function takes bluetoothRepository, no interface.
             */
            val selectedDevice = bluetoothRepository.selectedDevice.first()

            Log.d("ViewModelModelModelModel", "$selectedDevice")
            _viewState.value = _viewState.value.copy(
                selectedDevice = selectedDevice
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(@ApplicationContext context: Context) {
        viewModelScope.launch {

            val bluetoothManager: BluetoothManager? = context.getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter = bluetoothManager?.adapter

            if (!bluetoothAdapter?.isEnabled!!){
                Toast.makeText(context, "Bluetooth not supported on device", Toast.LENGTH_LONG).show()
            }

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//            @Suppress("DEPRECATION")
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }

            // Nullable items returning list of Bluetooth Objects of connected devices
            val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            val deviceList: ArrayList<BluetoothDevice> = ArrayList()

            pairedDevice?.forEach { device ->
                deviceList.add(device)
            }

            _viewState.value = _viewState.value.copy(
                pairedDevices = deviceList
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun saveSelectedDevice(bluetoothDevice: BluetoothDevice) {
        viewModelScope.launch {
            bluetoothRepository.saveDevice(bluetoothDevice)
            _viewState.value = _viewState.value.copy(
                selectedDevice = UserDevice(
                    bluetoothDevice.uuids.toString(),
                    bluetoothDevice.name,
                    bluetoothDevice.address
                )
            )
        }
    }

    fun startMediaPlayingService(@ApplicationContext context: Context) {
        val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled){
            Toast.makeText(context, "Bluetooth not supported on device", Toast.LENGTH_SHORT).show()
        }

        context.startService(Intent(context, MediaPlayingService::class.java))

        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(
                serviceActive = true
            )
        }
    }

    fun stopMediaPlayingService(@ApplicationContext context: Context) {
        val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled){
            Toast.makeText(context, "Bluetooth not supported on device", Toast.LENGTH_SHORT).show()
        }

        context.stopService(Intent(context, MediaPlayingService::class.java))

        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(
                serviceActive = false
            )
        }
    }
}