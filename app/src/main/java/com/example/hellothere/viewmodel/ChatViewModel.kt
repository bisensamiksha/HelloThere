package com.example.hellothere.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hellothere.models.ChatBot
import com.example.hellothere.models.Message
import com.example.hellothere.managers.MessageQueueManager
import com.example.hellothere.network.SocketManager

class ChatViewModel : ViewModel() {
    private val _chatbotList = MutableLiveData<List<ChatBot>>(emptyList())
    val chatbotList: LiveData<List<ChatBot>> = _chatbotList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    var simulateOffline = false
    private lateinit var socketManager: SocketManager

    init {
        initDummyChatBots()
        connectSocket()
    }

    private fun initDummyChatBots() {
        val bots = listOf(
            ChatBot("cb1", "Bot 1"),
            ChatBot("cb2", "Bot 2"),
            ChatBot("cb3", "Bot 3"),
            ChatBot("cb4", "Bot 4"),
            ChatBot("cb5", "Bot 5")
        )
        _chatbotList.value = bots
    }

    private fun connectSocket() {
        socketManager = SocketManager(
            onMessageReceived = { botId, message -> updateMessages(botId, message) },
            onError = { error ->
                _errorMessage.value = error
            }
        )
        socketManager.connect()
    }

    fun sendMessage(
        botId: String,
        message: String,
        simulateOffline: Boolean,
        socketManager: SocketManager,
        onQueueUpdated: () -> Unit
    ) {
        val bots = _chatbotList.value?.toMutableList() ?: return
        val bot = bots.find { it.botId == botId } ?: return
        bot.messages.add(Message(message, isUserMessage = true, isQueued = simulateOffline))
        bot.latestMessage = message
        if (!simulateOffline && socketManager.isConnected()) {
            socketManager.sendMessage(botId, message)
        } else {
            MessageQueueManager.queueMessage(botId, message)
        }
        _chatbotList.value = bots
        onQueueUpdated()
    }

    fun receiveMessage(botId: String, message: String) {
        val bots = _chatbotList.value?.toMutableList() ?: return
        val bot = bots.find { it.botId == botId } ?: return

        bot.messages.add(Message(message, isUserMessage = false))
        bot.latestMessage = message

        _chatbotList.value = bots
    }


    private fun updateMessages(botId: String, message: String) {
        val currentList = _chatbotList.value?.toMutableList() ?: return
        val bot = currentList.find { it.botId == botId } ?: return

        bot.messages.add(Message(message, isUserMessage = false))
        bot.latestMessage = message
        _chatbotList.postValue(currentList)
    }

    fun clearQueuedFlags() {
        val bots = _chatbotList.value?.toMutableList() ?: return
        bots.forEach { bot ->
            bot.messages.forEach { msg ->
                if (msg.isUserMessage) msg.isQueued = false
            }
        }
        _chatbotList.value = bots
    }
}
