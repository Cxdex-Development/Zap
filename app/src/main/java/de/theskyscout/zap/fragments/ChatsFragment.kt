package de.theskyscout.zap.fragments

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.ChatActivity
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.database.models.User
import de.theskyscout.zap.utils.adapter.RecycleChatsAdapter
import de.theskyscout.zap.utils.adapter.RecycleChatOnTouchListener
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur
import io.realm.kotlin.ext.realmListOf
import java.sql.Time

class ChatsFragment(
    private val activity: CodexActivity
) : Fragment() {

    companion object {
        var opendChat = Chat()
        val user = User().apply {
            name = "John Doe"
            profilePictureURI = "https://t3.ftcdn.net/jpg/03/64/62/36/360_F_364623643_58jOINqUIeYmkrH7go1smPaiYujiyqit.jpg"
        }
        val me = User().apply {
            name = "Jane Doe"
            profilePictureURI = "https://t3.ftcdn.net/jpg/03/64/62/36/360_F_364623643_58jOINqUIeYmkrH7go1smPaiYujiyqit.jpg"
        }
    }

    //Screen elements
    private lateinit var recyclerView: RecyclerView
    private lateinit var blurView: BlurView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize screen elements
        recyclerView = view.findViewById(R.id.rvChats)
        blurView = view.findViewById(R.id.blurViewChatsHeader)

        blurView.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurRadius(20f)
            .setBlurEnabled(true)


        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val message1 = Message().apply {
            message = "Hello there!"
            time = "12:00"
            read = false
            receiver = me
            sender = user
        }
        val message2 = Message().apply {
            message = "General Kenobi!"
            time = "12:22"
            read = false
            receiver = user
            sender = me
        }
        val chat = Chat().apply {
            receiver = user
            messages = realmListOf(message1, message2)
        }
        val chats = arrayListOf(
            Chat().apply {
                receiver = user
                sender = me
                messages = realmListOf(message1, message2, message1, message1, message2, message1, message1, message2, message1, message1, message2, message1, message1, message2, message1, message1, message2, message1, )
            }, chat, chat, chat, chat, chat, chat, chat, chat, chat, chat, chat


        )
        val adapter = RecycleChatsAdapter(chats)
        recyclerView.adapter = adapter
        val listener = RecycleChatOnTouchListener {
            val position = recyclerView.getChildAdapterPosition(it)
            opendChat = chats[position]
            activity.swapToActivity(ChatActivity::class.java)
        }
        recyclerView.addOnItemTouchListener(listener)
    }
}