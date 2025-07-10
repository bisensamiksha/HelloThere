package com.example.hellothere.models

data class ChatBot(
    val botId: String,
    val name: String,
    var latestMessage: String = "",
    var isExpanded: Boolean = false,
    val messages: MutableList<Message> = mutableListOf()
)
