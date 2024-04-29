package de.theskyscout.zap

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class ZapApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Zap"
            val descriptionText = "Zap"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(de.theskyscout.zap.utils.ZapNotificationManager.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            channel.enableVibration(true)
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("ZapApp", "Notification channel created")
        }
    }
}