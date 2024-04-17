package de.theskyscout.findex.database

import android.util.Log
import de.theskyscout.template.database.models.User
import de.theskyscout.template.utils.Constants
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.runBlocking

object MongoDB : MongoRepository {

    private val app = App.create(Constants.APP_ID)
    var user: io.realm.kotlin.mongodb.User? = app.currentUser
    private var realm: Realm? = null

    private var isClosed = false


    init {
        Log.d("MongoDB:init", "Initialized")
        configureRealm()
    }

    private fun configureRealm() {
        if(user == null || realm != null) {
            if (user == null) Log.e("MongoDB:configureRealm", "User is null")
            if (realm != null) Log.e("MongoDB:configureRealm", "Realm is already configured")
            return
        }
        val user = user!!
        val config = SyncConfiguration.Builder(user, setOf(User::class))
            .waitForInitialRemoteData()
            .initialSubscriptions {
                add(query = it.query<User>(query = "owner == ${user.id}"))
            }
            .log(LogLevel.ALL)
            .build()
        realm = Realm.open(config)
        isClosed = false
        Log.d("MongoDB:configureRealm", "Realm configured")
    }

    private fun closeRealm() {
        if(realm == null) {
            Log.e("MongoDB:closeRealm", "Realm is null")
            return
        }
        realm!!.close()
        realm = null
        isClosed = true
        Log.d("MongoDB:closeRealm", "Realm closed")
    }

    fun login(tokenID: String, callback: (Boolean, Exception?) -> Unit) {
        val credentials = Credentials.google(tokenID, GoogleAuthType.ID_TOKEN)
        Log.d("MongoDB:login", "Logging in")
        try {
            val app = App.create(Constants.APP_ID)
            runBlocking {
                user = app.login(credentials)
            }
            configureRealm()
            Log.d("MongoDB:login", "Logged in")
        } catch (e: Exception) {
            Log.e("MongoDB:login", "Failed to login", e)
            callback(false, e)
        }
    }

    fun logout() {
        if (user == null) {
            Log.e("MongoDB:logout", "User is null")
            return
        }
        val user = user!!
        runBlocking {
            user.logOut()
            closeRealm()
        }
        Log.d("MongoDB:logout", "Logged out")
    }

    fun getCurrentUser(): User? {
        Log.d("MongoDB:getCurrentUser", "Getting current user")
        Log.d("MongoDB:getCurrentUser", "User ID: ${user?.id}")
        return getUser(user?.id ?: "ERROR 001")
    }

    fun getUser(userID: String): User? {
        if(isClosed) {
            Log.e("MongoDB:getUser", "Realm is closed")
            return null
        }
        if(realm == null) {
            Log.e("MongoDB:getUser", "Realm is null")
            return null
        }
        val realm = realm!!
        Log.d("MongoDB:getUser", "Getting user with ID: $userID")
        val user = realm.query<User>(query = "owner_id == $0", userID).first().find()
        return user
    }

}