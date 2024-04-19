package de.theskyscout.zap.database.models

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class User : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var owner_id: String = ""
    var name: String = ""
    var email: String = ""
    var chats: RealmList<Chat> = realmListOf()
    var bio: String = ""
    var profilePictureURI: String = ""
    var status: String = ""
}