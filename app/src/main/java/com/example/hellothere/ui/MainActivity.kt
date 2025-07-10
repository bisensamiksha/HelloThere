package com.example.hellothere.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hellothere.managers.MessageQueueManager
import com.example.hellothere.R
import com.example.hellothere.network.SocketManager
import com.example.hellothere.adapters.ChatBotAdapter
import com.example.hellothere.models.ChatBot
import com.example.hellothere.models.Message
import com.example.hellothere.utils.NetworkUtils
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private var simulateOffline = false
    private lateinit var offlineToggle: MaterialSwitch
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatBotAdapter: ChatBotAdapter
    private val chatBotList = mutableListOf<ChatBot>()
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        offlineToggle = findViewById(R.id.offlineToggle)
        recyclerView = findViewById(R.id.chatsListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }

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
            sendMessage(botId, message)
        }

        recyclerView.adapter = chatBotAdapter

        offlineToggle.setOnCheckedChangeListener { _, isChecked ->
            simulateOffline = isChecked

            if (isChecked) {
                Toast.makeText(this, "Simulated offline ON", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            if (!isChecked) {
                Toast.makeText(this, "Simulated offline OFF. Flushing queue...", Toast.LENGTH_SHORT)
                    .show()

                if (::socketManager.isInitialized && socketManager.isConnected()) {
                    MessageQueueManager.flushQueue { botId, message ->
                        socketManager.sendMessage(botId, message)
                        clearIsQueueFlag()
                        runOnUiThread {
                            chatBotAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Toast.makeText(this, "Still offline. Will retry later.", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "Simulated offline ON", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun clearIsQueueFlag() {
        chatBotList.forEach { bot ->
            bot.messages.forEach { msg ->
                if (msg.isUserMessage) msg.isQueued = false
            }
        }
    }

    private fun initDummyChatBots() {
        chatBotList.add(ChatBot("cb1", "Bot 1"))
        chatBotList.add(ChatBot("cb2", "Bot 2"))
        chatBotList.add(ChatBot("cb3", "Bot 3"))
        chatBotList.add(ChatBot("cb4", "Bot 4"))
        chatBotList.add(ChatBot("cb5", "Bot 5"))
    }

    private fun sendMessage(botId: String, message: String) {
        val bot = chatBotList.find { it.botId == botId } ?: return
        bot.latestMessage = message

        if (!simulateOffline && socketManager.isConnected()) {
            bot.messages.add(Message(message, isUserMessage = true))
            socketManager.sendMessage(botId, message)
        } else {
            bot.messages.add(Message(message, isUserMessage = true, isQueued = true))
            MessageQueueManager.queueMessage(botId, message)
        }
        chatBotAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }

}