package de.theskyscout.zap.database.models


class Message() {
    var id: String? = null
    var sender_id: String? = null
    var receiver_id: String? = null
    var message: String = ""
    var time: String = ""
    var status: MessageStatus? = null
    var readTime : String? = null
    var edited: Boolean? = false
    var editedTime: String? = null
    var editedMessage: String? = null
}

enum class MessageStatus (val text: String) {
    SENT("Sent"),
    DELIVERED("Delivered"),
    READ("Read")
}

class MessageStatusChange {
    var message_id: String? = null
    var receiver_id: String? = null
    var sender_id: String? = null
    var status: MessageStatus? = null
}
