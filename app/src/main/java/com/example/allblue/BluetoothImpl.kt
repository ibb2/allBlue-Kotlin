package com.example.allblue

import android.annotation.SuppressLint
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserSelectedDevice(
    val uuid: String,
    val name: String,
    val address: String
)

class BluetoothRepoImplementation (private val dataStore: DataStore<Preferences>): BluetoothRepository {

    private object SelectedObject {
        val UUID = stringPreferencesKey("uuid")
        val NAME = stringPreferencesKey("name")
        val ADDRESS = stringPreferencesKey("address")
    }

    override suspend fun selectedDevice(): List<Flow<String>> {
        TODO("Not yet implemented")

        val bluetoothUUID: Flow<String> = dataStore.data.map { it ->
                // No type safety.
            it[SelectedObject.UUID]!!
        }

        val bluetoothName: Flow<String> = dataStore.data.map { it ->
            // No type safety.
            it[SelectedObject.NAME]!!
        }

        val bluetoothAddress: Flow<String> = dataStore.data.map { it ->
            // No type safety.
            it[SelectedObject.ADDRESS]!!
        }

        return listOf(bluetoothUUID, bluetoothName, bluetoothAddress)
    }

    override suspend fun saveDevice(uuid: String, name: String, address: String) {
        TODO("Not yet implemented")

        dataStore.edit { it ->
                it[SelectedObject.UUID] = uuid
                it[SelectedObject.NAME] = name
                it[SelectedObject.ADDRESS] = address
            }
    }

    @SuppressLint("MissingPermission")
    override suspend fun pairedDevices(@ApplicationContext context: Context): ArrayList<BluetoothDevice> {
        TODO("Not yet implemented")

        val bluetoothManager: BluetoothManager? = context.getSystemService(BluetoothManager::class.java)
        var bluetoothAdapter = bluetoothManager?.adapter

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
        val devices_list: ArrayList<BluetoothDevice> = ArrayList()


        pairedDevice?.forEach { device ->
            devices_list.add(device)
        }

    }
}