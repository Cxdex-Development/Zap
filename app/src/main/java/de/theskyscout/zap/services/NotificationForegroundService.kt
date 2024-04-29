package de.theskyscout.zap.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.database.LiveDatabase
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.ZapNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationForegroundService: Service() {

    companion object {
        var isNotificationServiceRunning = false
    }

    private val notificationManager = MainActivity.notificationManager
    private lateinit var messageListener: ValueEventListener

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.startForeground(1, notificationManager.getStartNotification())
        isNotificationServiceRunning = true
        Log.d("NotifiService", "Listening for messages")
        messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("NotifiService", "Message received")
                GlobalScope.launch(Dispatchers.IO) {
                    val message = snapshot.getValue(Message::class.java) ?: return@launch
                    val user = UserCache.currentUser
                    if (user == null) {
                        Log.e("NotifiService", "User not found")
                        return@launch
                    }
                    if(message.receiver_id == user.owner_id) {
                        val sender = UserCache.getUser(message.sender_id!!) ?: return@launch
                        val messageString = "${sender.name}: ${message.message}"
                        notificationManager.sendNotification(message)
                        Log.d("NotifiService", "Notification sent")
                        snapshot.ref.removeValue()
                    } else {
                        Log.d("NotifiService", "Message not for this user")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotifiService", "Failed to listen for messages", error.toException())
            }
        }
        LiveDatabase.messages.addValueEventListener(messageListener)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        LiveDatabase.messages.removeEventListener(messageListener)
        super.onDestroy()
    }
}