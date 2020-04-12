package com.example.ipscannerk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class IpScannerApp: Application() {
    val CHANNEL_ID = "generateCsvChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val exportCsvChannel = NotificationChannel(
                CHANNEL_ID,
                "Generate CSV File",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(exportCsvChannel)
        }
    }
}