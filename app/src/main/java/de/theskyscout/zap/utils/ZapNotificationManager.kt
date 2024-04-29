package de.theskyscout.zap.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.collection.ArrayMap
import androidx.collection.arrayMapOf
import androidx.core.app.NotificationCompat
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.database.models.Message

class ZapNotificationManager(
    private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val notificationCount = arrayMapOf<Int, Any>()

    companion object {
        const val CHANNEL_ID = "zap"
    }

    fun sendNotification(title: String, message: String, obj: Any? = null) {
        val activityIntent = Intent(context, MainActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        activityIntent.putExtra("notification", true)
        val pendingIntent = PendingIntent.getActivity(
            context,
            getNotificationId(),
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
        notificationManager.notify(1, notification)
    }

    fun sendNotification(message: Message) {
        val sender = UserCache.getUser(message.sender_id!!) ?: return
        sendNotification(sender.name, message.message, message)
    }

    fun deleteNotification(id: Int) {
        notificationManager.cancel(id)
        notificationCount.remove(id)
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
        notificationCount[id] = obj ?: Any()
        return id
    }
}