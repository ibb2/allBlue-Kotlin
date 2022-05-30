package com.example.allblue_kotlin

import android.Manifest
import android.R
import android.app.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.util.*
import java.util.prefs.Preferences
import kotlin.concurrent.thread
import java.util.UUID


class MediaPlayingService: Service() {

    private val ONGOING_NOTIFICATION_ID = 1
    private val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    private val TAG = "Media Playing Service"
    var audioPlayingStatus: Boolean = false

    override fun onBind(p0: Intent?): IBinder? {
        return  null
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE) }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
            .setContentTitle("allBlue - status")
            .setContentText("Running")
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setTicker("blank")
            .build()

        thread(true) {

            while (true) {
                val AudioManager: AudioManager = getSystemService(AudioManager::class.java)
                val AudioManagerActive: Boolean = AudioManager.isMusicActive

                audioPlayingStatus = false

                if (AudioManagerActive) {
                    audioPlayingStatus = true

                    Intent().also { intent ->
                        intent.setAction("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED")
                        intent.putExtra("data", audioPlayingStatus)
                        sendBroadcast(intent)
                    }
                }

                Thread.sleep(5000)
            }
        }

        // Start Foreground Service
        startForeground(ONGOING_NOTIFICATION_ID, notification)

        return START_REDELIVER_INTENT
    }

//    private inner class ConnectBluetoothDevices {
//        fun connect() {
//            val sharedPref = getSharedPreferences("Selected Bluetooth Device", Context.MODE_PRIVATE)
//
//            try {
//                val bluetoothDeviceObj = sharedPref.getString("uuid", "")
//                bluetoothDeviceObj.connect()
//            } catch (e: Exception) {
//
//            }
//        }
//    }
}





