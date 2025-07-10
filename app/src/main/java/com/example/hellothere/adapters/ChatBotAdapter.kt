package com.example.hellothere.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
        val previewText = view.findViewById<TextView>(R.id.latestMessage)
        val expandIcon = view.findViewById<ImageView>(R.id.expandIcon)
        val chatArea = view.findViewById<LinearLayout>(R.id.chatArea)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)
        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val headerLayout = view.findViewById<LinearLayout>(R.id.headerLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatbotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatbotViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatbotViewHolder, position: Int) {
        val chatbot = chatbotList[position]

        holder.nameText.text = chatbot.name
        holder.previewText.text = chatbot.latestMessage
        holder.chatArea.visibility = if (chatbot.isExpanded) View.VISIBLE else View.GONE
        holder.expandIcon.rotation = if (chatbot.isExpanded) 180f else 0f


        // Expand/collapse on header click
        holder.headerLayout.setOnClickListener {
            chatbotList.forEach { it.isExpanded = false } // collapse all
            chatbot.isExpanded = !chatbot.isExpanded     // toggle current
            notifyDataSetChanged()
        }

        // Send button logic
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
