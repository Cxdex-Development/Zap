package de.theskyscout.zap
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.activities.LoginActivity
import de.theskyscout.zap.databinding.ActivityMainBinding
import de.theskyscout.zap.fragments.ChatsFragment
import de.theskyscout.zap.screens.popups.RegisterPopup
import de.theskyscout.zap.services.NotificationForegroundService
import de.theskyscout.zap.services.NotificationForegroundService.Companion.isNotificationServiceRunning
import de.theskyscout.zap.signIn.GoogleAuthClient
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.ZapNotificationManager


class MainActivity : CodexActivity() {

    //Screen elements
    private lateinit var frameLayout: FrameLayout
    private lateinit var navBar : BottomNavigationView
    private lateinit var registerPopup: RegisterPopup

    companion object {
        lateinit var notificationManager: ZapNotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        notificationManager = ZapNotificationManager(this)
        UserCache.cacheRefresh() {
            registerPopup = RegisterPopup(this)
            //Check if the user is logged in
            val user = GoogleAuthClient.user
            if (user == null) {
                swapToActivity(LoginActivity::class.java)
                return@cacheRefresh
            }
            val userData = UserCache.currentUser
            if (userData == null) {
                registerPopup.show()
                return@cacheRefresh
            }

            if (!isMyServiceRunning(NotificationForegroundService::class.java)) {
                Log.d("Main", "Starting service")
                startForegroundService(Intent(this, NotificationForegroundService::class.java))
            }

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //Initialize screen elements
            frameLayout = findViewById(R.id.flChats)
            navBar = findViewById(R.id.bnvNavBar)

            //Set up the bottom navigation bar
            swapToFragment(ChatsFragment(this))
            setIcons(navBar, R.id.nav_chats, R.drawable.ic_chat_filled)

            navBar.setOnItemSelectedListener {
                when(it.itemId) {
                    R.id.nav_chats -> {
                        swapToFragment(ChatsFragment(this))
                        setIcons(navBar, R.id.nav_chats, R.drawable.ic_chat_filled)
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_community -> {

                        setIcons(navBar, R.id.nav_community, R.drawable.ic_community_filled)
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_calls-> {

                        setIcons(navBar, R.id.nav_calls, R.drawable.ic_calls_filled)
                        return@setOnItemSelectedListener true
                    }
                    R.id.nav_account -> {
                        googleAuthClient.signOut()
                        swapToActivity(LoginActivity::class.java)
                        setIcons(navBar, R.id.nav_account, R.drawable.ic_account_filled)
                        return@setOnItemSelectedListener true
                    }
                    else -> {
                        return@setOnItemSelectedListener false
                    }
                }
            }
        }

    }

    private fun checkPermissions() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.getName() == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun swapToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flChats, fragment)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setIcons(navBar: BottomNavigationView, filledID: Int, filledDrawable: Int) {
        if (filledID != R.id.nav_chats) navBar.menu.findItem(R.id.nav_chats)
            .setIcon(R.drawable.ic_chat)
        if (filledID != R.id.nav_community) navBar.menu.findItem(R.id.nav_community)
            .setIcon(R.drawable.ic_community)
        if (filledID != R.id.nav_account) navBar.menu.findItem(R.id.nav_account)
            .setIcon(R.drawable.ic_account)
        if (filledID != R.id.nav_calls) navBar.menu.findItem(R.id.nav_calls)
            .setIcon(R.drawable.ic_calls)
        navBar.menu.findItem(filledID).setIcon(filledDrawable)
    }
}