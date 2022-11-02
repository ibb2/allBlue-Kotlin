package com.subsolis.allblue

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.concurrent.thread

@kotlinx.serialization.Serializable
data class FirebaseIdentifier(val id: String?, val initial: Boolean?)

@kotlinx.serialization.Serializable
data class MusicPlaying(
    val musicIsPlaying: Boolean,
    val fromThisDevice: Boolean,
    val headphonesAlreadyConnected: Boolean,
    val blocked: Boolean
)

@AndroidEntryPoint
class MediaPlayingService : Service() {

    @Inject
    lateinit var bluetoothRepository: BluetoothRepository

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
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle("allBlue - status").setContentText("Running")
                .setContentIntent(pendingIntent).setPriority(Notification.PRIORITY_DEFAULT)
                .setTicker("blank").build()

        thread(true) {

            while (true) {
                val AudioManager: AudioManager = getSystemService(AudioManager::class.java)
                val AudioManagerActive: Boolean = AudioManager.isMusicActive

                audioPlayingStatus = false

                if (AudioManagerActive) {
                    audioPlayingStatus = true

                    webSockets(
                        musicPlaying = MusicPlaying(musicIsPlaying = audioPlayingStatus, fromThisDevice = true, headphonesAlreadyConnected = false, blocked = false)
                    )

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

    fun webSockets(musicPlaying: MusicPlaying) {

        val client = HttpClient {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }

        runBlocking {

            val auth_token = Firebase.auth.currentUser!!.getIdToken(true).await()
            val idToken = auth_token.token!!

            client.webSocket(method = HttpMethod.Get, host = "10.0.2.2", port = 8000, path = "/ws/music_room/auth/") {

                sendSerialized(FirebaseIdentifier(id = idToken, initial = true))

                while (true) {
                    val messageOutputRoutine = launch { outputMessages() }
                    val messageInputRoutine = launch { inputMessages(musicPlaying) }

                    messageInputRoutine.join()
                    messageOutputRoutine.cancelAndJoin()
                }
            }
        }
        client.close()
        println("Connection closed. Goodbye!")

    }

    private suspend fun DefaultClientWebSocketSession.outputMessages() {
        try {
            val musicInfoObj = receiveDeserialized<MusicPlaying>()
            println(musicInfoObj)
            for (message in incoming) {
                Log.d("WEBSOCKET", message.toString())
                message as? Frame.Text ?: continue
                println(message.readText())
            }
        } catch (e: Exception) {
            println("Error while receiving: " + e.localizedMessage)
        }
    }

    private suspend fun DefaultClientWebSocketSession.inputMessages(musicPlaying: MusicPlaying) {

        // Send first message on init, this will contain the authentication.
        // Most secure method, as of now. Replaced with sockets maybe later
        // Should send initially for connection, decreasing chances for the
        // ability for some body to spoof account.

        while (true) {
            try {
                sendSerialized(musicPlaying)
            } catch (e: Exception) {
                println("Error while sending: " + e.localizedMessage)
                return
            }
        }
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





