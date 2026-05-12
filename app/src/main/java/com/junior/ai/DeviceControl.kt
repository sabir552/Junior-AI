package com.junior.ai

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import java.util.Calendar

class DeviceControl(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MAIN EXECUTE — command parse karo
    fun execute(command: String): String {
        val cmd = command.lowercase().trim()
        return try {
            when {
                // Torch
                cmd.contains("torch on") || cmd.contains("torch chala") ||
                cmd.contains("flashlight on") || cmd.contains("torch jala") ->
                    setTorch(true)

                cmd.contains("torch off") || cmd.contains("torch band") ||
                cmd.contains("flashlight off") ->
                    setTorch(false)

                // Battery
                cmd.contains("battery") -> getBatteryInfo()

                // Volume
                cmd.contains("volume") -> handleVolume(cmd)

                // Brightness
                cmd.contains("brightness") || cmd.contains("roshan") -> handleBrightness(cmd)

                // WiFi
                cmd.contains("wifi") -> handleWifi(cmd)

                // Bluetooth
                cmd.contains("bluetooth") -> handleBluetooth(cmd)

                // SMS
                cmd.contains("sms") || cmd.contains("message bhejo") -> handleSms(cmd)

                // Call
                cmd.contains("call karo") || cmd.contains("phone karo") -> handleCall(cmd)

                // Screenshot
                cmd.contains("screenshot") -> takeScreenshot()

                // Vibrate
                cmd.contains("vibrate") || cmd.contains("hila") -> vibrate()

                // Alarm
                cmd.contains("alarm") -> handleAlarm(cmd)

                // Clipboard
                cmd.contains("clipboard") -> getClipboard()

                // DND
                cmd.contains("do not disturb") || cmd.contains("dnd") -> handleDnd(cmd)

                // Airplane mode info
                cmd.contains("airplane") -> "Airplane mode ke liye Settings mein jaana hoga sir."

                // Mobile data info
                cmd.contains("mobile data") || cmd.contains("data on") ||
                cmd.contains("data off") -> "Mobile data ke liye Settings mein jaana hoga sir."

                // App open karna
                cmd.contains("kholo") || cmd.contains("open") -> handleAppOpen(cmd)

                // Music control
                cmd.contains("music") || cmd.contains("pause") ||
                cmd.contains("play") || cmd.contains("next") -> handleMedia(cmd)

                // Storage info
                cmd.contains("storage") || cmd.contains("space") -> getStorageInfo()

                // RAM info
                cmd.contains("ram") || cmd.contains("memory") -> getRamInfo()

                else -> "Device command samajh nahi aya sir."
            }
        } catch (e: Exception) {
            Log.e("Junior", "Device error: ${e.message}")
            "Kuch masla hua sir: ${e.message}"
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // TORCH
    private fun setTorch(on: Boolean): String {
        return try {
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, on)
            if (on) "Torch on kar di sir!" else "Torch off kar di sir!"
        } catch (e: Exception) {
            "Torch control nahi ho saka sir."
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BATTERY
    private fun getBatteryInfo(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = bm.isCharging
        val status = if (isCharging) "charge ho rahi hai" else "charge nahi ho rahi"
        return "Battery $level percent hai sir, aur $status."
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VOLUME
    private fun handleVolume(cmd: String): String {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return when {
            cmd.contains("max") || cmd.contains("zyada") || cmd.contains("barha") -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0)
                "Volume max kar di sir!"
            }
            cmd.contains("mute") || cmd.contains("band") || cmd.contains("zero") -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                "Volume mute kar di sir!"
            }
            cmd.contains("half") || cmd.contains("adha") -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol / 2, 0)
                "Volume half kar di sir!"
            }
            cmd.contains("kam") || cmd.contains("ghata") -> {
                val curr = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    maxOf(0, curr - 2), 0)
                "Volume thodi kam kar di sir!"
            }
            else -> {
                val curr = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    minOf(maxVol, curr + 2), 0)
                "Volume thodi barha di sir!"
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BRIGHTNESS
    private fun handleBrightness(cmd: String): String {
        return try {
            val value = when {
                cmd.contains("max") || cmd.contains("zyada") -> 255
                cmd.contains("min") || cmd.contains("kam") -> 30
                cmd.contains("half") || cmd.contains("adha") -> 128
                cmd.contains("auto") -> -1
                else -> 128
            }
            if (value == -1) {
                Settings.System.putInt(context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
                "Auto brightness on kar di sir!"
            } else {
                Settings.System.putInt(context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Settings.System.putInt(context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, value)
                "Brightness set kar di sir!"
            }
        } catch (e: Exception) {
            "Brightness change ke liye permission chahiye sir. Settings mein Allow karein."
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // WIFI
    private fun handleWifi(cmd: String): String {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return when {
            cmd.contains("info") || cmd.contains("kya") ->
                "WiFi se connected hain sir: ${info.ssid}, Signal: ${info.rssi} dBm"
            cmd.contains("on") -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                "WiFi settings khol diye sir!"
            }
            else -> "WiFi se connected hain sir: ${info.ssid}"
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // BLUETOOTH
    private fun handleBluetooth(cmd: String): String {
        val bt = BluetoothAdapter.getDefaultAdapter()
        return when {
            cmd.contains("on") -> {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                "Bluetooth on kar raha hu sir!"
            }
            cmd.contains("off") -> {
                @Suppress("DEPRECATION")
                bt?.disable()
                "Bluetooth off kar di sir!"
            }
            else -> {
                val state = if (bt?.isEnabled == true) "on hai" else "off hai"
                "Bluetooth $state sir."
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SMS
    private fun handleSms(cmd: String): String {
        return try {
            val parts = cmd.replace("sms bhejo", "")
                .replace("message bhejo", "")
                .replace("sms", "").trim()
                .split(" ", limit = 2)
            if (parts.size == 2) {
                val number = parts[0]
                val message = parts[1]
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(number, null, message, null, null)
                "SMS bhej diya sir $number ko!"
            } else {
                "Number aur message dono chahiye sir. Jaise: sms 0300xxxxxxx Hello"
            }
        } catch (e: Exception) {
            "SMS nahi bhej saka sir: ${e.message}"
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CALL
    private fun handleCall(cmd: String): String {
        return try {
            val number = cmd.replace("call karo", "")
                .replace("phone karo", "").trim()
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Call kar raha hu sir $number pe!"
        } catch (e: Exception) {
            "Call nahi ho saki sir."
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // SCREENSHOT
    private fun takeScreenshot(): String {
        return "Screenshot ke liye Accessibility Service enable karein sir."
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // VIBRATE
    private fun vibrate(): String {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        return "Vibrate kar diya sir!"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ALARM
    private fun handleAlarm(cmd: String): String {
        return try {
            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (cmd.contains("subah") || cmd.contains("morning")) {
                    putExtra(android.provider.AlarmClock.EXTRA_HOUR, 7)
                    putExtra(android.provider.AlarmClock.EXTRA_MINUTES, 0)
                }
            }
            context.startActivity(intent)
            "Alarm app khol diya sir!"
        } catch (e: Exception) {
            "Alarm set nahi ho saka sir."
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // CLIPBOARD
    private fun getClipboard(): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
        return if (text != null) "Clipboard mein yeh hai sir: $text"
        else "Clipboard khali hai sir."
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DND
    private fun handleDnd(cmd: String): String {
        return if (notificationManager.isNotificationPolicyAccessGranted) {
            if (cmd.contains("on")) {
                notificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_NONE)
                "Do Not Disturb on kar diya sir!"
            } else {
                notificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_ALL)
                "Do Not Disturb off kar diya sir!"
            }
        } else {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            "DND permission do sir — Settings khul gaye!"
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // APP OPEN
    private fun handleAppOpen(cmd: String): String {
        val appMap = mapOf(
            "whatsapp" to "com.whatsapp",
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "camera" to "com.android.camera",
            "settings" to "com.android.settings",
            "calculator" to "com.android.calculator2",
            "maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "play store" to "com.android.vending",
            "facebook" to "com.facebook.katana",
            "instagram" to "com.instagram.android",
            "twitter" to "com.twitter.android",
            "tiktok" to "com.zhiliaoapp.musically",
            "spotify" to "com.spotify.music",
            "telegram" to "org.telegram.messenger"
        )
        val appName = appMap.entries.find { cmd.contains(it.key) }
        return if (appName != null) {
            try {
                val intent = context.packageManager
                    .getLaunchIntentForPackage(appName.value)
                if (intent != null) {
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    "${appName.key} khol diya sir!"
                } else {
                    "${appName.key} install nahi hai sir."
                }
            } catch (e: Exception) {
                "App nahi khul saki sir."
            }
        } else {
            "Kaunsa app kholuun sir?"
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // MEDIA CONTROL
    private fun handleMedia(cmd: String): String {
        val keyCode = when {
            cmd.contains("pause") || cmd.contains("stop") ->
                android.view.KeyEvent.KEYCODE_MEDIA_PAUSE
            cmd.contains("play") || cmd.contains("chala") ->
                android.view.KeyEvent.KEYCODE_MEDIA_PLAY
            cmd.contains("next") || cmd.contains("agla") ->
                android.view.KeyEvent.KEYCODE_MEDIA_NEXT
            cmd.contains("previous") || cmd.contains("pichla") ->
                android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }
        audioManager.dispatchMediaKeyEvent(
            android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(
            android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, keyCode))
        return "Media control kar diya sir!"
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // STORAGE INFO
    private fun getStorageInfo(): String {
        val stat = android.os.StatFs(android.os.Environment.getExternalStorageDirectory().path)
        val free = stat.availableBlocksLong * stat.blockSizeLong / (1024 * 1024 * 1024)
        val total = stat.blockCountLong * stat.blockSizeLong / (1024 * 1024 * 1024)
        return "Storage: $free GB free out of $total GB sir."
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // RAM INFO
    private fun getRamInfo(): String {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE)
            as android.app.ActivityManager
        val memInfo = android.app.ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val free = memInfo.availMem / (1024 * 1024)
        val total = memInfo.totalMem / (1024 * 1024)
        return "RAM: ${free}MB free out of ${total}MB sir."
    }
}
