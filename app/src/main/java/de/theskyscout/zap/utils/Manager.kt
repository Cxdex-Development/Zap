package de.theskyscout.zap.utils

import android.util.Log
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.database.LiveDatabase
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.database.models.MessageStatus
import de.theskyscout.zap.database.models.MessageStatusChange
import de.theskyscout.zap.database.models.User

class UserManager (val user: User) {
    fun addChat(chat: Chat) {
        user.chats.add(chat)
    }

    fun updateChat(chat: Chat) {
        val chatIndex = user.chats.indexOfFirst {
            it.sender_id == chat.sender_id && it.receiver_id == chat.receiver_id
        }
        if (chatIndex != -1) {
            user.chats[chatIndex] = chat
        } else {
            addChat(chat)
        }
    }
}

class ChatManager (val chat: Chat) {
    fun addMessage(message: Message) {
        chat.messages.add(message)
    }

    fun pinMessage(message: Message) {
        chat.pinnedMessage = message
    }

    fun readMessages() {
        chat.messages.filter { it.sender_id != chat.sender_id  }.forEach {
            MessageManager(it).read()
        }
    }
}

class MessageManager (val message: Message) {
    fun read() {
        if (message.status == MessageStatus.READ) return
        message.status = MessageStatus.READ
        val messageStatusChange = MessageStatusChange().apply {
            this.message_id = message.id
            this.receiver_id = message.receiver_id
            this.sender_id = message.sender_id
            this.status = MessageStatus.READ
        }
        val map = MainActivity.notificationManager.getCountMap()
        if (map.map { (it.value as Message).id }.contains(message.id)) {
            Log.d("MessageManager", "Deleting notification")
            val key = map.filterValues { it == message }.keys.first()
            MainActivity.notificationManager.deleteNotification(key)
        } else {
            Log.d("MessageManager", "Notification not found")
            Log.d("MessageManager", "Message id: ${message.id}")
            Log.d("MessageManager", "Notification ids: ${map.map { (it.value as Message).id }}")
        }
        LiveDatabase.writeMessageStatus(messageStatusChange)
    }

    fun edit(newMessage: String) {
        message.edited = true
        message.editedMessage = newMessage
    }
}