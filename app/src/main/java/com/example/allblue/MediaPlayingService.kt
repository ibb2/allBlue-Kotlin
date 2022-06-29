package com.example.allblue

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.concurrent.thread

@AndroidEntryPoint
class MediaPlayingService : Service() {

    @Inject lateinit var bluetoothRepository: BluetoothRepository

    private val ONGOING_NOTIFICATION_ID = 1
    private val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    private var isRunning: Boolean = false
    private val TAG = "Media Playing Service"
    var audioPlayingStatus: Boolean = false

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        isRunning = true

        Intent().also { intents ->
            intents.setAction("com.example.allblue_kotlin.SERVICE_STATUS")
            intents.putExtra("serviceStatus", isRunning)
            sendBroadcast(intents)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
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

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        GlobalScope.launch {
            bluetoothRepository.serviceStatus(false)
        }
        isRunning = false

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(true)
        GlobalScope.launch {
            bluetoothRepository.serviceStatus(false)
        }
        isRunning = false
    }
}





