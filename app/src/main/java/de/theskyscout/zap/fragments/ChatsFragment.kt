package de.theskyscout.zap.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.AddChatActivity
import de.theskyscout.zap.activities.ChatActivity
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.database.LiveDatabase
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.utils.Cache
import de.theskyscout.zap.utils.MessageManager
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.UserManager
import de.theskyscout.zap.utils.adapter.RecycleChatsAdapter
import de.theskyscout.zap.utils.adapter.RecycleChatOnTouchListener
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import kotlinx.coroutines.runBlocking

class ChatsFragment(
    private val activity: CodexActivity
) : Fragment() {

    companion object {
        var opendChat: Chat? = null
    }

    //Screen elements
    private lateinit var recyclerView: RecyclerView
    private lateinit var blurView: BlurView
    private lateinit var btnAdd: BlurView
    private lateinit var tvNoChats: TextView

    //Listeners
    private lateinit var messageListener : ValueEventListener

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
        btnAdd = view.findViewById(R.id.btnAddChat)
        tvNoChats = view.findViewById(R.id.tvNoChats)

        //Set up the blur effect

        btnAdd.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurRadius(15f)
            .setBlurEnabled(true)
        btnAdd.outlineProvider = ViewOutlineProvider.BACKGROUND
        btnAdd.clipToOutline = true

        blurView.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurRadius(15f)
            .setBlurEnabled(true)

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val chats = UserCache.currentUser!!.chats
        val chats_array = arrayListOf<Chat>()
        chats_array.addAll(chats)
        if (chats.isEmpty()) {
            tvNoChats.visibility = View.VISIBLE
        }
        val adapter = RecycleChatsAdapter(chats_array, activity)
        recyclerView.adapter = adapter


        btnAdd.setOnClickListener {
            activity.swapToActivity(AddChatActivity::class.java)
        }

        messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.getValue(Message::class.java)
                val user = UserCache.currentUser
                if (message != null) {
                    if (message.receiver_id!! == user!!.owner_id) {
                        val chat = chats.find { it.sender_id == message.sender_id || it.receiver_id == message.sender_id }
                        if (chat != null) {
                            if (chat.messages.find { it.id == message.id } == null) {
                                chat.messages.add(message)
                                UserManager(user).updateChat(chat)
                                val chatIndex = chats.indexOf(chat)
                                adapter.notifyItemChanged(chatIndex)
                                snapshot.ref.removeValue()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatActivity", "Failed to read value.", error.toException())
            }

        }

        LiveDatabase.messages.addValueEventListener(messageListener)
    }

    override fun onDestroy() {
        LiveDatabase.messages.removeEventListener(messageListener)
        super.onDestroy()
    }
}