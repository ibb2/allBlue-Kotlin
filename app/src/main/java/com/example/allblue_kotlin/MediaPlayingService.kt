package com.example.allblue_kotlin

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat




class MediaPlayingService: Service() {

    private val ONGOING_NOTIFICATION_ID = 1
    private val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    private val TAG = "Media Playing Service"


    override fun onBind(p0: Intent?): IBinder? {
        return  null
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO Detect music playing

        val AudioManager: AudioManager = getSystemService(AudioManager::class.java)
        val AudioManagerActive: Boolean? = AudioManager.isMusicActive

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE) }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
            .setContentTitle("allBlue")
            .setContentText("View")
            .setSmallIcon(R.drawable.arrow_down_float)
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_DEFAULT)
            .setTicker("blank")
            .build()

        // Start Foreground Service
        startForeground(ONGOING_NOTIFICATION_ID, notification)

        while (!AudioManagerActive!!) {

        }

        Log.i(TAG, "Foreground Service running")
        Log.i(TAG, "Is Music Active: $AudioManagerActive")

        return START_REDELIVER_INTENT
    }
}