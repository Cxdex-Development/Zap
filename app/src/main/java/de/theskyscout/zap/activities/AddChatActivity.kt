package de.theskyscout.zap.activities

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.databinding.ActivityAddChatBinding
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.adapter.RecycleAddChatAdapter
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur

class AddChatActivity : CodexActivity() {

    //Screen elements
    private lateinit var recyclerView: RecyclerView
    private lateinit var blurView: BlurView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("AddChat:create", "Started")

        binding = ActivityAddChatBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_add_chat)

        //Initialize screen elements
        recyclerView = findViewById(R.id.rvAddChats)
        blurView = findViewById(R.id.bvAddChatsHeader)

        //Set up blur view
        blurView.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurEnabled(true)
            .setBlurRadius(15f)

        //Set up recycler view
        val users = UserCache.users.filter { it.owner_id != UserCache.currentUser!!.owner_id}
        val usersArrayList = ArrayList(users)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RecycleAddChatAdapter(usersArrayList, this)
        recyclerView.adapter = adapter

    }

    override fun onBackPressed() {
        super.onBackPressed()
        swapToActivity(MainActivity::class.java)
    }

}