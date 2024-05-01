package de.theskyscout.zap.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import de.theskyscout.findex.database.MongoRepository
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.User
import de.theskyscout.zap.signIn.GoogleAuthClient
import de.theskyscout.zap.utils.Cache
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

object FireBase : MongoRepository {

    val database = FirebaseFirestore.getInstance()
    val collection = database.collection("users")

    suspend fun getUsers(): List<User> {
        return collection.get().await().toObjects(User::class.java)

    }

    suspend fun getUserById(id: String): User? {
        Log.d("Firebase:getUserById", "Getting user by id: $id")
        return collection.document(id).get().await().toObject(User::class.java)
    }

    suspend fun getCurrentUser(): User? {
        return getUserById(GoogleAuthClient.user?.uid ?: "")
    }

    fun addUser(user: User): Boolean {
        return collection.document(user.owner_id).set(user).isSuccessful
    }

    fun updateUser(user: User): Boolean {
        return collection.document(user.owner_id).set(user).isSuccessful
    }

    fun updateChatForBothUsers(chat: Chat, onSuccess: (() -> Unit)? = null) {
        GlobalScope.launch(Dispatchers.IO){
            val senderUser = UserCache.users.find { it.owner_id == chat.sender_id }
            updateChat(chat, senderUser!!)
            val receiverUser = UserCache.users.find { it.owner_id == chat.receiver_id }
            if (updateChat(chat, receiverUser!!)) {
                onSuccess?.invoke()
            }
        }
    }

   fun updateChat(chat: Chat, user: User) : Boolean{
       return GlobalScope.launch(Dispatchers.IO) {
           val chat2 = Chat().apply {
                sender_id = chat.sender_id
                receiver_id = chat.receiver_id
                messages = chat.messages
                pinnedMessage = chat.pinnedMessage
           }
           if (user.owner_id != chat.sender_id) {
               Log.d("Firebase:updateChat", "User id does not match sender id")
               val temp = chat.sender_id
               chat2.sender_id = user.owner_id
               chat2.receiver_id = temp
           }
           val userManager = UserManager(user)
           userManager.updateChat(chat2)
              addUser(user)
       }.isCompleted
   }
}