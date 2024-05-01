package de.theskyscout.zap.database.models

class User (
) {
    var owner_id: String = ""
    var name: String = ""
    var email: String = ""
    var chats: ArrayList<Chat> = ArrayList()
    var bio: String? = null
    var fcmToken = ""
    var profilePictureURI: String? = null
    var status: String = "offline"
}