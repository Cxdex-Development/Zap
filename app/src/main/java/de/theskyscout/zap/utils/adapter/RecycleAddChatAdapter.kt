package de.theskyscout.zap.utils.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import de.theskyscout.zap.R
import de.theskyscout.zap.activities.ChatActivity
import de.theskyscout.zap.activities.CodexActivity
import de.theskyscout.zap.database.models.Chat
import de.theskyscout.zap.database.models.User
import de.theskyscout.zap.fragments.ChatsFragment
import de.theskyscout.zap.utils.Cache
import de.theskyscout.zap.utils.UserCache


class RecycleAddChatAdapter(private val dataList: ArrayList<User>, private val activity: CodexActivity):
    RecyclerView.Adapter<RecycleAddChatAdapter.ViewHolderAdd>(){
    class ViewHolderAdd(itemView: View): RecyclerView.ViewHolder(itemView) {
        var linearLayout = itemView.findViewById<View>(R.id.llAddChatItem)
        var chatName = itemView.findViewById<TextView>(R.id.tvAddChatName)
        var chatBio = itemView.findViewById<TextView>(R.id.tvAddChatBio)
        val chatIcon = itemView.findViewById<ShapeableImageView>(R.id.ivAddChatItemAdd)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAdd {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_add_chat, parent, false)
        return ViewHolderAdd(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolderAdd, position: Int) {
        val user = dataList[position]
        holder.chatName.text = user.name
        val currentUser = UserCache.currentUser
        if (currentUser!!.chats.any { it.receiver_id == user.owner_id }) {
            holder.chatIcon.setImageDrawable(activity.getDrawable(R.drawable.ic_check))
        }
        holder.chatBio.text = formatBioMessage(user.bio) ?: "No bio available"
        holder.linearLayout.setOnClickListener {
            if (currentUser.chats.any { it.receiver_id == user.owner_id }) return@setOnClickListener
            val chat = Chat().apply {
                receiver_id = user.owner_id
                sender_id = UserCache.currentUser!!.owner_id
                messages = ArrayList()
            }
            ChatsFragment.opendChat = chat
            activity.dbMain.updateChatForBothUsers(chat)
            activity.swapToActivity(ChatActivity::class.java)

        }
    }


    private fun formatBioMessage(message: String?): String? {
        if (message == null) return null
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