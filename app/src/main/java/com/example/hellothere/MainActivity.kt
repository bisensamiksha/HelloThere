package com.example.hellothere

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hellothere.adapters.ChatBotAdapter
import com.example.hellothere.models.ChatBot
import com.example.hellothere.models.Message

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatBotAdapter: ChatBotAdapter
    private val chatBotList = mutableListOf<ChatBot>()
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.chatsListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        initDummyChatBots()

        socketManager = SocketManager(
            onMessageReceived = { botId, message ->
                runOnUiThread {
                    val bot = chatBotList.find { it.botId == botId } ?: return@runOnUiThread
                    bot.messages.add(Message(message, isUserMessage = false))
                    bot.latestMessage = message
                    chatBotAdapter.notifyDataSetChanged()
                }
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
        )
        socketManager.connect()

        chatBotAdapter = ChatBotAdapter(chatBotList) { botId, message ->
            handleSendMessage(botId, message)
        }

        recyclerView.adapter = chatBotAdapter

    }

    private fun initDummyChatBots() {
        chatBotList.add(ChatBot("cb1", "CB1"))
        chatBotList.add(ChatBot("cb2", "CB2"))
        chatBotList.add(ChatBot("cb3", "CB3"))
        chatBotList.add(ChatBot("cb4", "CB4"))
        chatBotList.add(ChatBot("cb5", "CB5"))
    }

    private fun handleSendMessage(botId: String, message: String){
        val bot = chatBotList.find { it.botId == botId } ?: return
        bot.messages.add(Message(message, isUserMessage = true))
        bot.latestMessage = message
        chatBotAdapter.notifyDataSetChanged()

        socketManager.sendMessage(botId, message)

    }

}