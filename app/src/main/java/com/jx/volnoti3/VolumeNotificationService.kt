package com.jx.volnoti3

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.AudioManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.os.Build
import androidx.core.graphics.drawable.IconCompat

class VolumeNotificationService : Service() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var audioManager: AudioManager
    private val CHANNEL_ID = "VolumeNotificationChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
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
        val size = 96 // Icon size in pixels
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = if (volume == 0) Color.rgb(33, 150, 243) else Color.RED // Blue when 0, Red otherwise
            textSize = size * 0.6f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Draw the volume number
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(String.format("%02d", volume), xPos, yPos, paint)

        return IconCompat.createWithBitmap(bitmap)
    }

    private fun updateNotification() {
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercentage = ((volume.toFloat() / maxVolume.toFloat()) * 100).toInt()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(createVolumeIcon(volumePercentage))
            .setOngoing(true)
            .setShowWhen(false)
            .setSilent(true)
            .setContentTitle("Media Volume")
            .setContentText("Current volume: $volumePercentage%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
