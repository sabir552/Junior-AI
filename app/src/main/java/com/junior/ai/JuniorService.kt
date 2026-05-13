package com.junior.ai

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class JuniorService : Service() {

    private lateinit var voiceManager: VoiceManager
    private lateinit var deviceControl: DeviceControl
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val CHANNEL_ID = "junior_channel"
        const val NOTIF_ID = 1001
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Shuru ho raha hu... 🎤"))
        deviceControl = DeviceControl(this)

        Log.d("Junior", "Service onCreate")

        // Thoda wait karo phir voice start karo
        handler.postDelayed({
            initVoice()
        }, 2000)
    }

    private fun initVoice() {
        try {
            voiceManager = VoiceManager(this)
            setupVoiceCallbacks()
            handler.postDelayed({
                voiceManager.startWakeWordListening()
                updateNotification("\"Junior\" boliye... 🎤")
                Log.d("Junior", "Wake word listening started!")
            }, 1500)
        } catch (e: Exception) {
            Log.e("Junior", "Voice init error: ${e.message}")
        }
    }

    private fun setupVoiceCallbacks() {
        voiceManager.onWakeWord = {
            Log.d("Junior", "WAKE WORD DETECTED!")
            handler.post {
                updateNotification("Haan sir? 🎤")
                voiceManager.speak("Haan sir?") {
                    handler.postDelayed({
                        voiceManager.startCommandListening()
                    }, 300)
                }
            }
        }

        voiceManager.onCommand = { command ->
            Log.d("Junior", "Command received: $command")
            handler.post {
                updateNotification("Soch raha hu... ⚡")
                processCommand(command)
            }
        }

        voiceManager.onError = { error ->
            Log.d("Junior", "Voice error: $error")
            handler.post {
                updateNotification("\"Junior\" boliye... 🎤")
                if (error != "no_match") {
                    voiceManager.speak(error) {
                        handler.postDelayed({
                            voiceManager.startWakeWordListening()
                        }, 500)
                    }
                } else {
                    voiceManager.startWakeWordListening()
                }
            }
        }
    }

    private fun processCommand(command: String) {
        val cmd = command.lowercase()
        if (BrainRouter.isDeviceCommand(cmd)) {
            val result = deviceControl.execute(command)
            voiceManager.speak(result) {
                updateNotification("\"Junior\" boliye... 🎤")
                handler.postDelayed({
                    voiceManager.startWakeWordListening()
                }, 500)
            }
        } else {
            BrainRouter.process(command) { response ->
                handler.post {
                    voiceManager.speak(response) {
                        updateNotification("\"Junior\" boliye... 🎤")
                        handler.postDelayed({
                            voiceManager.startWakeWordListening()
                        }, 500)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Junior AI",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Junior background service"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Junior AI")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Junior", "onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d("Junior", "Service destroyed — restarting...")
        if (::voiceManager.isInitialized) voiceManager.destroy()
        // Auto restart
        handler.postDelayed({
            val intent = Intent(this, JuniorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }, 1000)
    }
}
