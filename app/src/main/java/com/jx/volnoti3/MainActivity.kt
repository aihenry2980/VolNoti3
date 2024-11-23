package com.jx.volnoti3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val EXTRA_FOREGROUND_SERVICE_TYPE = "android.foregroundServiceType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check and request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                startVolumeService()
            }
        } else {
            startVolumeService()
        }

        // Close the activity after starting the service
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVolumeService()
            }
            finish()
        }
    }

    private fun startVolumeService() {
        val serviceIntent = Intent(this, VolumeNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            serviceIntent.putExtra(EXTRA_FOREGROUND_SERVICE_TYPE, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        }
        startForegroundService(serviceIntent)
    }
}
