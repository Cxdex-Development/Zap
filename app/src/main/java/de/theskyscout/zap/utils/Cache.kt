package de.theskyscout.zap.utils

import android.util.Log
import de.theskyscout.zap.database.FireBase
import de.theskyscout.zap.database.models.User
import de.theskyscout.zap.signIn.GoogleAuthClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface Cache {
    fun get(key: String): Any?
    fun set(key: String, value: Any)
}

object UserCache: Cache {
    private val cache = mutableMapOf<String, User>()
    private var firstRefresh = false

    val currentUser
        get() = getCurrentCacheUser()
    val users
        get() = getCacheUsers()
    override fun get(key: String): User? {
        return cache[key]
    }

    override fun set(key: String, value: Any) {
        if (value !is User) {
            Log.e("UserCache:set", "Value is not a User")
            return
        }
        cache[key] = value as User
    }

    fun getUser(id: String): User? {
        return get(id)
    }

    private fun getCurrentCacheUser(): User? {
        return users.find { it.owner_id == GoogleAuthClient.user?.uid }
    }

    private fun getCacheUsers(): List<User> {
        return cache.values.toList()
    }

    suspend fun isCacheUpToDate(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            val cachedUser = get(user.owner_id)
            val databaseUser = FireBase.getUserById(user.owner_id)
            cachedUser == databaseUser
        }
    }

    fun cacheRefresh(onSuccess: (() -> Unit)? = null) {
        if (firstRefresh) {
            onSuccess?.invoke()
            return
        }
        firstRefresh = true
        runBlocking {
            val users = FireBase.getUsers()
            for (user in users) {
                set(user.owner_id, user)
            }
            onSuccess?.invoke()
        }
        GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                Log.d("UserCache:cacheRefresh", "Refreshing cache")
                val users = FireBase.getUsers()
                for (user in users) {
                    set(user.owner_id, user)
                }
                delay(20000)
            }
        }
    }


    fun update(user: User) {
        GlobalScope.launch(Dispatchers.IO) {
            FireBase.collection.document(user.owner_id).set(user)
        }
    }
}