package com.example.hellothere.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hellothere.R
import com.example.hellothere.models.ChatBot

class ChatBotAdapter(
    private val chatbotList: List<ChatBot>,
    private val onSendMessage: (chatbotId: String, message: String) -> Unit
) : RecyclerView.Adapter<ChatBotAdapter.ChatbotViewHolder>() {

    inner class ChatbotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText = view.findViewById<TextView>(R.id.chatbotName)
        val latestMessage = view.findViewById<TextView>(R.id.latestMessage)
        val expandIcon = view.findViewById<ImageView>(R.id.expandIcon)
        val chatArea = view.findViewById<LinearLayout>(R.id.chatArea)
        val messageView = view.findViewById<LinearLayout>(R.id.messageView)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)
        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val headerLayout = view.findViewById<LinearLayout>(R.id.headerLayout)
        val chatScrollView: ScrollView = itemView.findViewById(R.id.chatScrollView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatbotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatbotViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatbotViewHolder, position: Int) {
        val chatbot = chatbotList[position]

        holder.nameText.text = chatbot.name
        holder.latestMessage.text =
            if (chatbot.latestMessage.isBlank()) "No messages" else chatbot.latestMessage
        holder.chatArea.visibility = if (chatbot.isExpanded) View.VISIBLE else View.GONE
        holder.latestMessage.visibility = if (chatbot.isExpanded) View.GONE else View.VISIBLE
        holder.expandIcon.rotation = if (chatbot.isExpanded) 180f else 0f


        holder.messageView.removeAllViews()
        chatbot.messages.forEach { msg ->
            val messageView = TextView(holder.itemView.context).apply {
                if (msg.isQueued) {
                    text =
                        if (msg.isUserMessage) "You: ${msg.text} (queued)" else "Bot: ${msg.text}"
                } else {
                    text = if (msg.isUserMessage) "You: ${msg.text}" else "Bot: ${msg.text}"
                }
                setPadding(4, 4, 4, 4)
                setTextColor(Color.BLACK)
            }
            holder.messageView.addView(messageView)
        }

        holder.chatScrollView.post {
            holder.chatScrollView.fullScroll(View.FOCUS_DOWN)
        }


        holder.headerLayout.setOnClickListener {
            val wasExpanded = chatbot.isExpanded
            chatbotList.forEach { it.isExpanded = false }
            chatbot.isExpanded = !wasExpanded
            notifyDataSetChanged()
        }

        holder.sendButton.setOnClickListener {
            val messageText = holder.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                onSendMessage(chatbot.botId, messageText)
                holder.messageInput.text.clear()
            }
        }
    }

    override fun getItemCount(): Int = chatbotList.size
}
