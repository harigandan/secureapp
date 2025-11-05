package com.example.secureapp // Your package name

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class AlarmService : Service() {

    companion object {
        const val CHANNEL_ID = "SecureDroid_Alarm"
        const val ACTION_STOP = "securedroid.action.STOP_ALARM"
    }

    private var mp: MediaPlayer? = null
    private lateinit var audio: AudioManager
    private var volumeObserver: ContentObserver? = null
    private var originalVolume: Int = -1   // store user’s original volume

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Alarm Siren",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSiren()
            stopSelf()
            return START_NOT_STICKY
        }

        // Save current volume before overriding
        try {
            originalVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
        } catch (_: Exception) { }

        startForeground(2, buildNotification())
        startSiren()
        lockVolume()   // 🚀 force max volume until stopped
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SecureDroid Alarm")
            .setContentText("Playing loud siren (cannot be silenced)")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .build()
    }

    private fun startSiren() {
        stopSiren() // just in case a previous one is running

        mp = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)?.apply {
            isLooping = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }
            start()
        }
    }

    private fun stopSiren() {
        mp?.run {
            try { stop() } catch (_: Exception) {}
            try { release() } catch (_: Exception) {}
        }
        mp = null
        unlockVolume()
    }

    private fun lockVolume() {
        val uri: Uri = android.provider.Settings.System.CONTENT_URI
        volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                try {
                    val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0)
                } catch (_: Exception) { }
            }
        }
        contentResolver.registerContentObserver(uri, true, volumeObserver!!)
    }

    private fun unlockVolume() {
        volumeObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
        volumeObserver = null

        // Restore user’s original volume
        if (originalVolume >= 0) {
            try {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
            } catch (_: Exception) {}
        }
    }
    override fun onDestroy() {
        stopSiren()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
   
