package com.example.hellothere.managers

object MessageQueueManager {
    private val messageQueue = mutableListOf<Pair<String, String>>() // botId to message

    fun queueMessage(botId: String, message: String) {
        messageQueue.add(Pair(botId, message))
    }

    fun flushQueue(sendFunc: (botId: String, message: String) -> Unit) {
        val iterator = messageQueue.iterator()
        while (iterator.hasNext()) {
            val (botId, message) = iterator.next()
            sendFunc(botId, message)
            iterator.remove()
        }
    }

}
