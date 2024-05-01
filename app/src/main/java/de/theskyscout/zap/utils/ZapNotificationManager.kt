package de.theskyscout.zap.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.app.NotificationCompat
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.ChatActivity
import de.theskyscout.zap.database.models.Message

class ZapNotificationManager(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "zap"
        val notificationCount = arrayMapOf<Int, Any>()
    }

    fun sendNotification(title: String, message: String) {
        if (notificationCount.containsValue(title)) {
            val id = notificationCount.filterValues { it == title }.keys.first()
            val notification = notificationManager.activeNotifications.find { it.id == id }
            if (notification != null) {
                val currentMessage = notification.notification.extras.getString(Notification.EXTRA_TEXT)
                val newMessage = "$currentMessage\n$message"
                editNotification(id, title, newMessage)
                return
            }
            editNotification(id, title, message)
            return
        }
        val id = getNotificationId(title)
        sendNotify(title, message, id)
    }

    private fun sendNotify(title: String, message: String, id: Int) {
        Log.d("ZapNotificationManager", "Sending notification")
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activityIntent.putExtra("notification", true)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(id, notification)
    }

    fun sendNotification(message: Message) {
        val sender = UserCache.getUser(message.sender_id!!) ?: return
        val id = getNotificationId(message)
        sendNotify(sender.name, message.message, id)
    }

    fun deleteNotification(id: Int) {
        notificationManager.cancel(id)
        notificationCount.remove(id)
    }

    fun editNotification(id: Int, title: String, message: String) {
        val activeNotification = notificationManager.activeNotifications.find { it.id == id }
        if (activeNotification != null) {
            val notification = activeNotification.notification
            val newNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(notification.contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            notificationManager.notify(id, newNotification)
        }
    }

    fun getStartNotification (): Notification {
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activityIntent.putExtra("notification", true)
        val pendingIntent = PendingIntent.getActivity(
            context,
            getNotificationId(),
            activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Zap")
            .setContentText("Listening for messages")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

    }

    fun getNotificationCount(): Int {
        return notificationCount.size
    }

    fun getCountMap(): ArrayMap<Int, Any> {
        return notificationCount
    }

    private fun getNotificationId(obj: Any? = null): Int {
        var id = 0
        while (notificationCount.containsKey(id)) {
            id++
        }
        notificationCount.put(id, obj ?: "")
        return id
    }
}