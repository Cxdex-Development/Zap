package de.theskyscout.zap.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.database.models.MessageStatus
import de.theskyscout.zap.database.models.MessageStatusChange

object LiveDatabase {

    val database = Firebase.database("https://android-zap-421216-default-rtdb.europe-west1.firebasedatabase.app/")
    val messages = database.getReference("Message")
    val messsageStatus = database.getReference("MessageStatus")

    fun writeNewMessage(message: Message) {
        Log.d("LiveDatabase", "Writing message")
        messages.setValue(message)
    }

    fun writeMessageStatus(status: MessageStatusChange) {
        Log.d("LiveDatabase", "Writing message status")
        messsageStatus.setValue(status)
    }
}