package com.example.allblue

import android.bluetooth.BluetoothDevice
import android.content.Context
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@AndroidEntryPoint
@InstallIn(ActivityContext::class)
@Singleton
interface BluetoothRepository {

    suspend fun selectedDevice(): List<Flow<String>>
    suspend fun saveDevice(uuid: String, name:String, address: String): Unit
    suspend fun pairedDevices(context: Context): ArrayList<BluetoothDevice>
}