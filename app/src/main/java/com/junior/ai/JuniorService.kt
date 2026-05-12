package com.junior.ai

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class JuniorService : Service() {

    private lateinit var voiceManager: VoiceManager
    private lateinit var deviceControl: DeviceControl
    private var isAwake = false

    companion object {
        const val CHANNEL_ID = "junior_channel"
        const val NOTIF_ID = 1001
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Sun raha hu... 🎤"))

        deviceControl = DeviceControl(this)
        voiceManager = VoiceManager(this)
        setupVoice()

        Log.d("Junior", "Service started!")
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VOICE SETUP
    private fun setupVoice() {
        voiceManager.onWakeWord = {
            Log.d("Junior", "WAKE WORD!")
            isAwake = true
            updateNotification("Haan sir? 🎤")
            voiceManager.speak("Haan sir?") {
                voiceManager.startCommandListening()
            }
        }

        voiceManager.onCommand = { command ->
            Log.d("Junior", "Command: $command")
            isAwake = false
            updateNotification("Kaam kar raha hu... ⚡")
            processCommand(command)
        }

        voiceManager.onError = { error ->
            isAwake = false
            updateNotification("Sun raha hu... 🎤")
            if (error != "no_match") {
                voiceManager.speak(error) {
                    voiceManager.startWakeWordListening()
                }
            } else {
                voiceManager.startWakeWordListening()
            }
        }

        // Start karo
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            voiceManager.startWakeWordListening()
        }, 1000)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // PROCESS COMMAND
    private fun processCommand(command: String) {
        val cmd = command.lowercase()

        if (BrainRouter.isDeviceCommand(cmd)) {
            // Device command — seedha execute karo
            val result = deviceControl.execute(command)
            voiceManager.speak(result) {
                updateNotification("Sun raha hu... 🎤")
                voiceManager.startWakeWordListening()
            }
        } else {
            // AI se jawab lo
            BrainRouter.process(command) { response ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    voiceManager.speak(response) {
                        updateNotification("Sun raha hu... 🎤")
                        voiceManager.startWakeWordListening()
                    }
                }
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // NOTIFICATION
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Junior AI",
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
        val pi = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Junior AI")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Service band ho toh apne aap restart ho
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        voiceManager.destroy()
        Log.d("Junior", "Service destroyed — restarting...")
        // Apne aap restart karo
        val restartIntent = Intent(this, JuniorService::class.java)
        startService(restartIntent)
    }
}
