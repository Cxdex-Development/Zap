package de.theskyscout.zap.database.models

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import org.mongodb.kbson.ObjectId

class Chat : EmbeddedRealmObject {
    var _id: ObjectId = ObjectId()
    var sender: User? = null
    var receiver: User? = null
    var messages: RealmList<Message> = realmListOf()
    var pinnedMessage: Message? = null
}
