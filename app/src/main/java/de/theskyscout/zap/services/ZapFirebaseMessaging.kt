package de.theskyscout.zap.services

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.theskyscout.zap.database.FireBase
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.ZapNotificationManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.FileInputStream
import java.util.Arrays


class ZapFirebaseMessaging : FirebaseMessagingService() {


    init {
        val user = UserCache.currentUser
        FirebaseMessaging.getInstance().subscribeToTopic(user!!.owner_id).addOnSuccessListener {
            Log.d("ZapFirebaseMessaging", "Subscribed to topic ${user.owner_id}")
        }
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
        if (message.data.isNotEmpty()) {
            val title = message.notification?.title ?: "Zap"
            val body = message.notification?.body ?: "New message"
            ZapNotificationManager(this).sendNotification(title, body)
        }
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
        val jsonObject = JSONObject()
        val message = JSONObject()
        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", body)
        message.put("notification", notification)
        message.put("token", receiverId)
        jsonObject.put("message", message)
        callApi(jsonObject)

    }

    private fun callApi(jsonObject: JSONObject) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            val json = "application/json; charset=utf-8".toMediaType()
            val client = OkHttpClient()
            val url = "https://fcm.googleapis.com/v1/projects/android-zap-421216/messages:send"
            val body = jsonObject.toString().toRequestBody(json)
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ${getAccessToken()}")
                .build()
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    Log.d("ZapFirebaseMessaging", "Message failed")
                    Log.d("ZapFirebaseMessaging", e.message.toString())
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    Log.d("ZapFirebaseMessaging", "Message sent")
                    Log.d("ZapFirebaseMessaging", response.body.toString())
                    Log.d("ZapFirebaseMessaging", response.message)
                    Log.d("ZapFirebaseMessaging", response.code.toString())
                    Log.d("ZapFirebaseMessaging", response.headers.toString())
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAccessToken(): String? {
        return try {
            val jsonFile = resources.openRawResource(de.theskyscout.zap.R.raw.serviceaccount)
            val googleCredentials = GoogleCredentials
                .fromStream(jsonFile)
                .createScoped(Arrays.asList<String>(
                    "https://www.googleapis.com/auth/firebase.messaging"
                ))
            googleCredentials.refresh()
            Log.d("ZapFirebaseMessaging", googleCredentials.accessToken.tokenValue)
            googleCredentials.getAccessToken().tokenValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}