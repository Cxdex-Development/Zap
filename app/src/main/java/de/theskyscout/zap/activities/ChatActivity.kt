package de.theskyscout.zap.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.database.LiveDatabase
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.database.models.MessageStatus
import de.theskyscout.zap.database.models.MessageStatusChange
import de.theskyscout.zap.database.models.User
import de.theskyscout.zap.databinding.ActivityChatBinding
import de.theskyscout.zap.fragments.ChatsFragment
import de.theskyscout.zap.listener.SwipeListener
import de.theskyscout.zap.utils.ChatManager
import de.theskyscout.zap.utils.MessageManager
import de.theskyscout.zap.utils.UserCache
import de.theskyscout.zap.utils.adapter.RecycleMessagesAdapter
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.util.Calendar
import java.util.UUID

class ChatActivity : CodexActivity() {

    //Screen elements
    private lateinit var chatName: TextView
    private lateinit var chatImage: ShapeableImageView
    private lateinit var chatBack: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var blurView: BlurView
    private lateinit var blurViewMessage: BlurView
    private lateinit var blurViewSend: BlurView
    private lateinit var chatInputLayout: TextInputLayout
    private lateinit var chatInput: TextInputEditText
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var swipeListener: SwipeListener

    private lateinit var view: View

    private lateinit var statusListener: ValueEventListener
    private lateinit var messageListener: ValueEventListener

    val chat
        get() = UserCache.currentUser?.chats?.find { it.receiver_id == ChatsFragment.opendChat?.receiver_id && it.sender_id == ChatsFragment.opendChat?.sender_id}!!


    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_chat)

        view = binding.root

        if(chat == null) {
            swapToActivity(MainActivity::class.java)
            return
        }

        readMessages(chat)


        //Initialize screen elements
        chatName = findViewById(R.id.tvChatChatName)
        chatImage = findViewById(R.id.ivChatChatProfileImage)
        recyclerView = findViewById(R.id.rvChatMessages)
        blurView = findViewById(R.id.bvChatHeader)
        blurViewMessage = findViewById(R.id.bvChatChatMessage)
        blurViewSend = findViewById(R.id.bvChatChatSend)
        chatBack = findViewById(R.id.cvChatBack)
        chatInput = findViewById(R.id.etChatChatMessageInput)
        chatInputLayout = findViewById(R.id.etChatChatMessage)
        coordinatorLayout = findViewById(R.id.clChatChat)

        blurView.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurRadius(15f)
            .setBlurEnabled(true)

        blurViewMessage.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurRadius(15f)
            .setBlurEnabled(true)
        blurViewMessage.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurViewMessage.clipToOutline = true


        blurViewSend.setupWith(recyclerView, RenderEffectBlur())
            .setBlurAutoUpdate(true)
            .setBlurRadius(15f)
            .setBlurEnabled(true)
        blurViewSend.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurViewSend.clipToOutline = true

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val dataList = arrayListOf<Message>()
        dataList.addAll(chat.messages)
        val adapter = RecycleMessagesAdapter(dataList, chat, this)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(dataList.size - 1)


        val user = UserCache.getUser(chat.receiver_id!!)
        chatName.text = user!!.name ?: "Unknown"

        //Live listeners
        messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    if (message.receiver_id == chat.sender_id && message.sender_id == chat.receiver_id) {
                        Log.d("ChatActivity", "Message received: ${message.message}")
                        if(dataList.map { it.id }.contains(message.id)) return
                        chat.messages.add(message)
                        dbMain.updateChatForBothUsers(chat)

                        //Sending message status
                        MessageManager(message).read()

                        //Adding message to the list
                        dataList.add(message)
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(dataList.size - 1)
                        UserCache.getUser(chat.sender_id!!)!!.chats.find { it.sender_id == chat.receiver_id && it.receiver_id == chat.sender_id }?.let {
                            it.messages.add(message)
                        }
                        snapshot.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatActivity", "Failed to read value.", error.toException())
            }

        }
        LiveDatabase.messages.addValueEventListener(messageListener)

        statusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val statusChange = snapshot.getValue(MessageStatusChange::class.java)
                if (statusChange != null) {
                    if (statusChange.receiver_id == chat.receiver_id && statusChange.sender_id == chat.sender_id) {
                        val message = dataList.find { it.id == statusChange.message_id }
                        if (message != null) {
                            val index = dataList.indexOf(message)
                            dataList.get(index).status = statusChange.status
                            adapter.notifyDataSetChanged()
                        }
                        snapshot.ref.removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatActivity", "Failed to read value.", error.toException())
            }

        }

        LiveDatabase.messsageStatus.addValueEventListener(statusListener)


        chatBack.setOnClickListener {
            swapToActivity(MainActivity::class.java)
        }

        blurViewSend.setOnClickListener {
            val time = getTime()
            val message = chatInput.text.toString()
            if (message.isNotEmpty()) {
                val newMessage = Message().apply {
                    this.id = UUID.randomUUID().toString()
                    this.message = message
                    this.time = time
                    this.sender_id = chat.sender_id
                    this.receiver_id = chat.receiver_id
                    this.status = MessageStatus.SENT
                }
                chat.messages.add(newMessage)

                //Uploading message to database
                activity.dbMain.updateChatForBothUsers(chat)
                LiveDatabase.writeNewMessage(newMessage)

                //Adding message to the list
                dataList.add(newMessage)
                adapter.notifyDataSetChanged()
                UserCache.getUser(chat.receiver_id!!)?.let { user ->
                    user.chats.find { it.sender_id == chat.sender_id && it.receiver_id == chat.receiver_id }?.let {
                        it.messages.add(newMessage)
                    }
                }
                chatInput.setText("")
                recyclerView.scrollToPosition(dataList.size - 1)
            }
        }

        chatInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerView.scrollToPosition(dataList.size - 1)
            }
        }

        KeyboardVisibilityEvent.setEventListener(this) { isOpen ->
            if (isOpen) {
                recyclerView.scrollToPosition(dataList.size - 1)
            } else {
                chatInput.clearFocus()

            }
        }

        swipeListener = SwipeListener(this).apply {
            onSwipeLeft = {
                if (chatInput.isFocused) {
                    chatInput.clearFocus()
                    // Show keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(chatInput.windowToken, 0)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LiveDatabase.messages.removeEventListener(messageListener)
        LiveDatabase.messsageStatus.removeEventListener(statusListener)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        swipeListener.onTouch(view, event)
        super.dispatchTouchEvent(event)
        return false
    }


    private fun readMessages(chat: Chat) {
    chat.messages.filter { it.status != MessageStatus.READ  && it.receiver_id!! == chat.sender_id}.forEach {
            it.status = MessageStatus.READ
            val messageStatusChange = MessageStatusChange().apply {
                this.message_id = it.id
                this.receiver_id = it.receiver_id
                this.sender_id = it.sender_id
                this.status = MessageStatus.READ
            }

            LiveDatabase.writeMessageStatus(messageStatusChange)
        }
        dbMain.updateChatForBothUsers(chat)
    }

    private fun getTime(): String {
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY).toString()
        var minute = calendar.get(Calendar.MINUTE).toString()
        if (hour.length == 1) hour = "0$hour"
        if (minute.length == 1) minute = "0$minute"
        return "$hour:$minute"
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        swapToActivity(MainActivity::class.java)
    }


}