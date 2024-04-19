package de.theskyscout.zap.database.models

import io.realm.kotlin.types.EmbeddedRealmObject
import org.mongodb.kbson.ObjectId

class Message : EmbeddedRealmObject {
    var _id: ObjectId = ObjectId()
    var sender: User? = null
    var receiver: User? = null
    var chat: Chat? = null
    var message: String = ""
    var time: String = ""
    var read: Boolean = false
    var readTime : String? = null
    var deleted: Boolean = false
    var edited: Boolean = false
    var editedTime: String? = null
    var editedMessage: String? = null

}
