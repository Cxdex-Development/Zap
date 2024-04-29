package de.theskyscout.zap.utils.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.utils.Cache
import de.theskyscout.zap.utils.UserCache
import kotlinx.coroutines.runBlocking


class RecycleMessagesAdapter(private val dataList: ArrayList<Message>, private val chat: Chat, private val activity: CodexActivity):
    RecyclerView.Adapter<RecycleMessagesAdapter.ViewHolder>(){
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val chatMessage = itemView.findViewById<TextView>(R.id.tvChatMessage)
        val chatTime = itemView.findViewById<TextView>(R.id.tvChatMessageTime)
        val bubbleTail = itemView.findViewById<ShapeableImageView>(R.id.ivChatMessageTail)
        val chatMessageStatus = itemView.findViewById<TextView>(R.id.tvChatMessageStatus)
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataList[position]

        if (isLastMessageInRow(message)) {
            holder.bubbleTail.visibility = View.VISIBLE
        } else {
            holder.bubbleTail.visibility = View.GONE
        }
        val me = UserCache.currentUser
        if (isLastMessage(message) && message.sender_id == me!!.owner_id) {
            val status = message.status?.text ?: ""
            holder.chatMessageStatus.text = status
            holder.chatMessageStatus.visibility = View.VISIBLE
        } else {
            holder.chatMessageStatus.visibility = View.GONE
        }

        if (isMessageEmoji(message.message) && message.message.length <= 3) {
            holder.chatMessage.textSize = 35f
        } else {
            holder.chatMessage.textSize = 12f
        }
        val messageText = message.message
        holder.chatMessage.text = formatMessage(messageText)
        holder.chatTime.text = message.time
    }

    private fun formatMessage(message: String): String {
        val words = message.split(" ")
        var formattedMessage = ""
        var currentLine = ""
        for (word in words) {
            if (currentLine.length + word.length > 30) {
                formattedMessage += currentLine + "\n"
                currentLine = ""
            }else if (word.length > 20) {
                formattedMessage += currentLine + "\n"
                currentLine = ""
                for (i in word.indices step 20) {
                    currentLine += if (i + 20 > word.length)
                        word.substring(i, word.length) + "\n"
                    else
                        word.substring(i, i + 20) + "\n"
                }
                continue
            }
            currentLine += "$word "
        }
        formattedMessage += currentLine
        return formattedMessage
    }

    private fun isLastMessageInRow(message: Message): Boolean {
        val index = dataList.indexOf(message)
        if (index == dataList.size - 1) return true
        if (dataList[index + 1].sender_id != message.sender_id) return true
        return false
    }

    private fun isLastMessage(message: Message): Boolean {
        val index = dataList.indexOf(message)
        return index == dataList.size - 1
    }

    private fun isMessageEmoji(message: String): Boolean {
        return message.matches(".*[\\uD83C-\\uDBFF\\uDC00-\\uDFFF].*".toRegex())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_in, parent, false)
                ViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_out, parent, false)
                ViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val me = UserCache.currentUser
        val user = UserCache.users.find { it.owner_id == dataList[position].sender_id }
        return if(user!!.owner_id == me!!.owner_id) 1 else 0

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}