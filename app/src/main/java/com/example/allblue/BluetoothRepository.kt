package com.example.allblue

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class UserDevice(
    val uuid: String ="",
    val name: String ="",
    val address: String =""
)

class BluetoothRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    companion object {
        val BLUETOOTH_UUID = stringPreferencesKey("UUID")
        val BLUETOOTH_NAME = stringPreferencesKey("name")
        val BLUETOOTH_ADDRESS = stringPreferencesKey("address")
    }

    val selectedDevice = dataStore.data.map { device ->
        UserDevice(
            device[BLUETOOTH_UUID] ?: "",
            device[BLUETOOTH_NAME] ?: "",
            device[BLUETOOTH_ADDRESS] ?: "")
    }


    suspend fun saveDevice(bluetoothDevice: BluetoothDevice): Unit {
        dataStore.edit { device ->
            device[BLUETOOTH_UUID] = bluetoothDevice.uuids.toString()
            device[BLUETOOTH_NAME] = bluetoothDevice.name
            device[BLUETOOTH_ADDRESS] = bluetoothDevice.address
        }
    }

    fun pairedDevices(@ApplicationContext context: Context): ArrayList<BluetoothDevice> {
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

        return deviceList
    }
}