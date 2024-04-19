package de.theskyscout.zap.utils.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import de.theskyscout.zap.R
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.Message
import de.theskyscout.zap.fragments.ChatsFragment


class RecycleMessagesAdapter(private val dataList: ArrayList<Message>, private val chat: Chat):
    RecyclerView.Adapter<RecycleMessagesAdapter.ViewHolder>(){
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val chatCardView = itemView.findViewById<CardView>(R.id.cvChatMessage)
        val chatLinearLayout = itemView.findViewById<LinearLayout>(R.id.llChatMessage)
        val chatMessage = itemView.findViewById<TextView>(R.id.tvChatMessage)
        val chatTime = itemView.findViewById<TextView>(R.id.tvChatMessageTime)
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataList[position]

        if(message.sender != ChatsFragment.me) {
            val color = holder.itemView.resources.getColor(R.color.background_darker).toColor()
            holder.chatCardView.setCardBackgroundColor(color.toArgb())
            holder.chatLinearLayout.gravity = Gravity.START
        } else {
            val color = holder.itemView.resources.getColor(R.color.primary).toColor()
            holder.chatCardView.setCardBackgroundColor(color.toArgb())
            holder.chatLinearLayout.gravity = Gravity.END
        }
        val messageText = message.message
        holder.chatMessage.text = formatMessage(messageText)
        holder.chatTime.text = message.time
    }

    fun formatMessage(message: String): String {
        // Split the message every 30 characters
        // but only at spaces
        // But when a word is longer than 30 characters it will be split
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}