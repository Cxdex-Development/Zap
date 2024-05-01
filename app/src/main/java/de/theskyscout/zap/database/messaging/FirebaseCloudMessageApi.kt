package de.theskyscout.zap.database.messaging

import retrofit2.http.Body
import retrofit2.http.POST


interface FirebaseCloudMessageApi {

    @POST("/send")
    suspend fun sendNotification(
       @Body notification: SendMessageDto
    )
}