package com.subsolis.allblue

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subsolis.allblue.databinding.ActivityMainBinding
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

    private val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    private val devices_list : java.util.ArrayList<BluetoothDevice> = java.util.ArrayList()
    private val REQUEST_ENABLE_BT = 1
    private val TAG = "Main Activity"
    private lateinit var binding: ActivityMainBinding

//    fun checkBluetoothStatus(@ApplicationContext context: Context) {
//        if (!bluetoothAdapter.isEnabled){
//            _viewState.value = _viewState.value.copy(
//                bluetoothEnabled = true
//            )
//        }
//    }
    override fun onCleared() {
        super.onCleared()
        clearMediaPlayingService()
        viewModelScope.launch {
            bluetoothRepository.serviceStatus(false)

            _viewState.value = _viewState.value.copy(
                serviceActive = false
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

    fun getServiceStatus() {
        viewModelScope.launch {
            val status = bluetoothRepository.currentServiceStatus.first()

            _viewState.value = _viewState.value.copy(
                serviceActive = bluetoothRepository.currentServiceStatus.first()
            )

            Log.d("Service", "Service Being Called or nah? $status")
        }
    }

    fun startMediaPlayingService(@ApplicationContext context: Context) {
        val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled){
            Toast.makeText(context, "Bluetooth not supported on device", Toast.LENGTH_SHORT).show()
        }

        context.startService(Intent(context, MediaPlayingService::class.java))

        viewModelScope.launch {

            bluetoothRepository.serviceStatus(true)

            _viewState.value = _viewState.value.copy(
                serviceActive = true,
            )
        }
    }

    fun stopMediaPlayingService(@ApplicationContext context: Context) {
        val bluetoothManager: BluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled){
            Toast.makeText(context, "Bluetooth not supported on device", Toast.LENGTH_SHORT).show()
        }

        context.stopService(Intent(context, MediaPlayingService::class.java))

        viewModelScope.launch {
            bluetoothRepository.serviceStatus(false)

            _viewState.value = _viewState.value.copy(
                serviceActive = false,
            )
        }
    }

    private fun clearMediaPlayingService() {
        viewModelScope.launch {

            val serviceStatus = bluetoothRepository.serviceStatus(false)

            _viewState.value = _viewState.value.copy(
                serviceActive = false
            )
        }
    }

    fun createNotificationChannel(@ApplicationContext context: Context) {
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
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}