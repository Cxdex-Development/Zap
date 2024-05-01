package de.theskyscout.zap.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.theskyscout.zap.database.FireBase
import de.theskyscout.zap.database.messaging.FirebaseCloudMessageApi
import de.theskyscout.zap.database.messaging.NotificationBody
import de.theskyscout.zap.database.messaging.SendMessageDto
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.ZapNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import java.io.IOException


class ZapFirebaseMessaging : FirebaseMessagingService() {

    private val api: FirebaseCloudMessageApi = Retrofit.Builder()
        .baseUrl("http://192.168.178.31:8080/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create()


    init {
        val user = UserCache.currentUser
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                val user = UserCache.currentUser
                if (user != null) {
                    user.fcmToken = token
                    FireBase.updateUser(user)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("ZapFirebaseMessaging", "Message received")
        val title = message.notification?.title ?: "Zap"
        val body = message.notification?.body ?: "New message"
        ZapNotificationManager(this).sendNotification(title, body)
        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        Log.d("ZapFirebaseMessaging", "New token: $token")
        val user = UserCache.currentUser
        if (user != null) {
            user.fcmToken = token
            FireBase.updateUser(user)
        }
        super.onNewToken(token)
    }

    fun sendCloudMessage(receiverId: String, title: String, body: String) {
        Log.d("ZapFirebaseMessaging", "Sending message to $receiverId")
        GlobalScope.launch(Dispatchers.IO) {
            val message = SendMessageDto(
                to = receiverId,
                notification = NotificationBody(title, body)
            )
            try {
                api.sendNotification(message)
            } catch (e: HttpException) {
                Log.e("ZapFirebaseMessaging", "Failed to send message", e)
            } catch (e: IOException) {
                Log.e("ZapFirebaseMessaging", "Failed to send message", e)
            }
        }
    }

}