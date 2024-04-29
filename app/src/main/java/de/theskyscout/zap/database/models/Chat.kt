package de.theskyscout.zap.database.models



class Chat (
) {
    var sender_id: String? = null
    var receiver_id: String? = null
    var messages: ArrayList<Message> = ArrayList()
    var pinnedMessage: Message? = null
}
