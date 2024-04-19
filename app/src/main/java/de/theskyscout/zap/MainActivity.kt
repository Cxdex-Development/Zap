package de.theskyscout.zap

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.databinding.ActivityMainBinding
import de.theskyscout.zap.fragments.ChatsFragment

class MainActivity : CodexActivity() {

    //Screen elements
    private lateinit var frameLayout: FrameLayout
    private lateinit var navBar : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    setIcons(navBar, R.id.nav_account, R.drawable.ic_account_filled)
                    return@setOnItemSelectedListener true
                }
                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }
    }


    private fun swapToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.flChats, fragment)
        transaction.commit()
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