package com.example.allblue

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
    val selectedDevice: UserDevice? = null,
    val pairedDevices: ArrayList<BluetoothDevice> = ArrayList()
)

@HiltViewModel
class BluetoothViewModel @Inject constructor(@ApplicationContext context: Context, private val bluetoothRepository: BluetoothRepository) : ViewModel() {

    private val _viewState = MutableStateFlow(BluetoothState())
    val viewState: StateFlow<BluetoothState> = _viewState

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

    fun saveSelectedDevice(bluetoothDevice: BluetoothDevice, ) {
        viewModelScope.launch {
            bluetoothRepository.saveDevice(bluetoothDevice)
        }
    }
}