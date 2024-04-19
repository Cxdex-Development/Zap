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
import de.theskyscout.zap.database.models.Chat
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL


class RecycleChatsAdapter(private val dataList: ArrayList<Chat>):
    RecyclerView.Adapter<RecycleChatsAdapter.ViewHolder>(){
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var chatName = itemView.findViewById<TextView>(R.id.tvChatItemName)
        var chatImage = itemView.findViewById<ShapeableImageView>(R.id.ivChatItem)
        var chatLastMessage = itemView.findViewById<TextView>(R.id.tvChatItemLastMessage)
        var chatTime = itemView.findViewById<TextView>(R.id.tvChatItemTime)
        var badge = itemView.findViewById<TextView>(R.id.tvChatItemBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chatName.text = dataList[position].receiver!!.name

        holder.chatTime.text = dataList[position].messages.last().time
        holder.chatLastMessage.text = dataList[position].messages.last().message

        val missedMessages = dataList[position].messages.filter { !it.read }.size
        if (missedMessages == 0) {
            holder.badge.visibility = View.GONE
        } else {
            holder.badge.visibility = View.VISIBLE
        }
        holder.badge.text = missedMessages.toString()
    }

    private fun getImageBitmap(url: String): Bitmap? {
        var bm: Bitmap? = null
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            val aURL = URL(url)
            val conn = aURL.openConnection()
            conn.connect()
            val `is` = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error getting bitmap", e)
        }
        return bm
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