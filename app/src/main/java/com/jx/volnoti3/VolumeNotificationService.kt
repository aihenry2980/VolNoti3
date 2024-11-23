package com.jx.volnoti3

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.media.AudioManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.os.Build
import androidx.core.graphics.drawable.IconCompat
import android.content.pm.ServiceInfo
import java.util.Locale

class VolumeNotificationService : Service() {
    companion object {
        private val skyBlue = Color.rgb(135, 206, 235) // Lighter, more vibrant blue
        private val pink = Color.rgb(255, 182, 193)    // Light pink
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private val channelId = "VolumeNotificationChannel"
    private val notificationId = 1
    
    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
                val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                if (streamType == AudioManager.STREAM_MUSIC) {
                    updateNotification()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        
        // Register volume change receiver
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(volumeReceiver)
        } catch (e: Exception) {
            // Receiver might already be unregistered
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(notificationId, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Volume Notification",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows current media volume"
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createVolumeIcon(volume: Int): IconCompat {
        val size = 128 // Increased icon size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Set background to transparent
        canvas.drawColor(Color.TRANSPARENT)

        // Draw the volume number
        val paint = Paint().apply {
            color = if (volume == 0) skyBlue else pink // Sky blue when 0, Pink otherwise
            textSize = size * 0.85f // Increased text size relative to icon
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
            style = Paint.Style.FILL
            strokeWidth = size * 0.1f // Make the text thicker
        }

        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        
        val formattedVolume = String.format(Locale.US, "%02d", volume)
        
        // Draw text with stroke for better visibility
        paint.style = Paint.Style.STROKE
        canvas.drawText(formattedVolume, xPos, yPos, paint)
        
        // Draw text fill
        paint.style = Paint.Style.FILL
        canvas.drawText(formattedVolume, xPos, yPos, paint)

        return IconCompat.createWithBitmap(bitmap)
    }

    private fun createNotification(): Notification {
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercentage = ((volume.toFloat() / maxVolume.toFloat()) * 100).toInt()

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(createVolumeIcon(volumePercentage))
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .setContentTitle("Media Volume")
            .setContentText("Current volume: $volumePercentage%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        notificationManager.notify(notificationId, notification)
    }
}
