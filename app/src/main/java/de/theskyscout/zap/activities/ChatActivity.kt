package de.theskyscout.zap.activities

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import de.theskyscout.zap.MainActivity
import de.theskyscout.zap.R
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.databinding.ActivityChatBinding
import de.theskyscout.zap.fragments.ChatsFragment
import de.theskyscout.zap.listener.SwipeListener
import de.theskyscout.zap.utils.adapter.RecycleMessagesAdapter
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderEffectBlur
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import java.sql.Time
import java.util.Calendar

class ChatActivity : CodexActivity() {

    //Screen elements
    private lateinit var chatName: TextView
    private lateinit var chatImage: ShapeableImageView
    private lateinit var chatBack: ShapeableImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var blurView: BlurView
    private lateinit var blurViewMessage: BlurView
    private lateinit var blurViewSend: BlurView
    private lateinit var chatInputLayout: TextInputLayout
    private lateinit var chatInput: TextInputEditText
    private lateinit var coordinatorLayout: CoordinatorLayout

    private lateinit var swipeListener: SwipeListener

    private lateinit var view: View

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_chat)

        view = binding.root

        val chat = ChatsFragment.opendChat

        //Initialize screen elements
        chatName = findViewById(R.id.tvChatChatName)
        chatImage = findViewById(R.id.ivChatChatProfileImage)
        recyclerView = findViewById(R.id.rvChatMessages)
        blurView = findViewById(R.id.bvChatHeader)
        blurViewMessage = findViewById(R.id.bvChatChatMessage)
        blurViewSend = findViewById(R.id.bvChatChatSend)
        chatBack = findViewById(R.id.ivChatBack)
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
        val adapter = RecycleMessagesAdapter(dataList, chat)
        recyclerView.adapter = adapter
        recyclerView.scrollToPosition(dataList.size - 1)

        chatName.text = chat.receiver!!.name

        chatBack.setOnClickListener {
            swapToActivity(MainActivity::class.java)
        }

        blurViewSend.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val message = chatInput.text.toString()
            if (message.isNotEmpty()) {
                val messageObj = Message().apply {
                    this.message = message
                    this.time = "$hour:$minute"
                    this.read = false
                    this.receiver = chat.receiver
                    this.sender = ChatsFragment.me
                }
                dataList.add(messageObj)
                adapter.notifyDataSetChanged()
                chatInput.text?.clear()
                recyclerView.scrollToPosition(dataList.size - 1)
            }
        }

        chatInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerView.scrollToPosition(dataList.size - 1)
            }
        }

        chatInput.setOnEditorActionListener { _, _, _ ->
            blurView.performClick()
            true
        }

        KeyboardVisibilityEvent.setEventListener(this) { isOpen ->
            if (isOpen) {
                recyclerView.scrollToPosition(dataList.size - 1)
            } else {
                recyclerView.scrollToPosition(dataList.size - 1)
                chatInput.clearFocus()

            }
        }

        swipeListener = SwipeListener(this).apply {
            onSwipeDown = {
                if (chatInput.isFocused) {
                    chatInput.clearFocus()
                    // Show keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(chatInput.windowToken, 0)
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false
        swipeListener.onTouch(view, event)
        super.dispatchTouchEvent(event)
        return false
    }



    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        swapToActivity(MainActivity::class.java)
    }


}