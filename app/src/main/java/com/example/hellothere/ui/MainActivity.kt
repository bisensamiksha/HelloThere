package com.example.hellothere.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hellothere.R
import com.example.hellothere.adapters.ChatBotAdapter
import com.example.hellothere.managers.MessageQueueManager
import com.example.hellothere.network.SocketManager
import com.example.hellothere.utils.NetworkUtils
import com.example.hellothere.viewmodel.ChatViewModel
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private var simulateOffline = false
    private lateinit var offlineToggle: MaterialSwitch
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatBotAdapter: ChatBotAdapter
    private lateinit var socketManager: SocketManager
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        initUI()

        connectSocket()
    }

    private fun connectSocket() {
        socketManager = SocketManager(
            onMessageReceived = { botId, message ->
                runOnUiThread {
                    chatViewModel.receiveMessage(botId, message)
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
    }

    private fun initUI() {

        offlineToggle = findViewById(R.id.offlineToggle)
        recyclerView = findViewById(R.id.chatsListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
        }


        chatViewModel.chatbotList.observe(this) { bots ->
            chatBotAdapter = ChatBotAdapter(bots.toMutableList()) { botId, message ->
                handleSendMessage(botId, message)
            }
            recyclerView.adapter = chatBotAdapter
        }

        offlineToggle.setOnCheckedChangeListener { _, isChecked ->
            simulateOffline = isChecked

            if (isChecked) {
                Toast.makeText(this, "Simulated offline ON", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            } else {
                Toast.makeText(this, "Simulated offline OFF. Flushing queue...", Toast.LENGTH_SHORT)
                    .show()

                if (::socketManager.isInitialized && socketManager.isConnected()) {
                    MessageQueueManager.flushQueue { botId, message ->
                        socketManager.sendMessage(botId, message)
                        chatViewModel.clearQueuedFlags()
                        runOnUiThread {
                            chatBotAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Toast.makeText(this, "Still offline. Will retry later.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        chatViewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSendMessage(botId: String, message: String) {
        chatViewModel.sendMessage(
            botId = botId,
            message = message,
            simulateOffline = simulateOffline,
            socketManager = socketManager
        ) {
            chatBotAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }

}