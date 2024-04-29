package de.theskyscout.zap.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.theskyscout.zap.utils.ZapNotificationManager

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val service = ZapNotificationManager(context!!)
        service.sendNotification("Zap", "You have a new message")
    }
}