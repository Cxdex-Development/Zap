package de.theskyscout.zap.utils.adapter

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.ChatActivity
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.MessageStatus
import de.theskyscout.zap.fragments.ChatsFragment
import de.theskyscout.zap.utils.Cache
import de.theskyscout.zap.utils.SelectListener
import de.theskyscout.zap.utils.UserCache
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL


class RecycleChatsAdapter(private val dataList: ArrayList<Chat>, private val activity: CodexActivity):
    RecyclerView.Adapter<RecycleChatsAdapter.ViewHolder>() {


    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val chatName: TextView = itemView.findViewById<TextView>(R.id.tvChatItemName)
        val chatLastMessage: TextView = itemView.findViewById<TextView>(R.id.tvChatItemLastMessage)
        val chatTime: TextView = itemView.findViewById<TextView>(R.id.tvChatItemTime)
        val badge: TextView = itemView.findViewById<TextView>(R.id.tvChatItemBadge)
        val linearLayout: View = itemView.findViewById<View>(R.id.llChatItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = UserCache.getUser(dataList[position].receiver_id!!)
        holder.chatName.text = user!!.name

        val lastMessage = dataList[position].messages.last()

        holder.chatTime.text = lastMessage.time
        holder.chatLastMessage.text = formatLastMesaage(lastMessage.message)

        val me = UserCache.currentUser

        val missedMessages =
            dataList[position].messages.filter { it.status != MessageStatus.READ && it.sender_id != me!!.owner_id }.size
        if (missedMessages == 0) {
            holder.badge.visibility = View.GONE
        } else {
            holder.badge.visibility = View.VISIBLE
        }
        holder.badge.text = missedMessages.toString()

        holder.linearLayout.setOnClickListener {
            val chats = UserCache.currentUser!!.chats
            val chat = chats[position]
            ChatsFragment.opendChat = chat
            activity.swapToActivity(ChatActivity::class.java)
        }

    }

    private fun formatLastMesaage(message: String): String {
        // If the message is longer than 30 characters, it will be cut off but only at spaces
        val words = message.split(" ")
        var formattedMessage = ""
        var currentLine = ""
        for (word in words) {
            if (currentLine.length + word.length > 30) {
                formattedMessage += "$currentLine..."
                return formattedMessage
            }
            currentLine += "$word "
        }
        formattedMessage += currentLine
        return formattedMessage
    }
}

class RecycleChatOnTouchListener(
    private val onTouchItem: (View) -> Unit
): RecyclerView.OnItemTouchListener {

    var viewBefore: View? = null
    var yBefore: Float? = null

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val view = rv.findChildViewUnder(e.x, e.y)

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                yBefore = e.y
                viewBefore = view
            }
            MotionEvent.ACTION_UP -> {
                if (view == null) return false
                if (view == viewBefore) {
                    onTouchItem(view)
                }
            }
            else -> {
                viewBefore = view
                val yDiff = Math.abs(e.y - yBefore!!)
                if (yDiff > 50) {
                    viewBefore = null
                    return false
                }
                return false
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}