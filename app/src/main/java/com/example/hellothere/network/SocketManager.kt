package com.example.hellothere.network

import android.util.Log
import com.example.hellothere.managers.MessageQueueManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class SocketManager(
    private val onMessageReceived: (botId: String, message: String) -> Unit,
    private val onError: (String) -> Unit
) {

    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket
    private val request = Request.Builder()
        .url("wss://s14919.blr1.piesocket.com/v3/1?api_key=fmNK3AScPYMuoIO9oOP3Q7O4jAtmBZJsYvrMvlIs&notify_self=0")
        .build()

    fun connect() {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS", "Socket connected")
                MessageQueueManager.flushQueue { botId, message ->
                    sendMessage(botId, message)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val botId = json.optString("botId")
                    val message = json.optString("message")
                    onMessageReceived(botId, message)
                } catch (e: Exception) {
                    onError("Parse error: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onError("Socket failed: ${t.localizedMessage}")
            }
        })
    }

    fun sendMessage(botId: String, message: String) {
        val json = JSONObject()
        json.put("botId", botId)
        json.put("message", message)
        webSocket.send(json.toString())
    }

    fun isConnected(): Boolean {
        return this::webSocket.isInitialized
    }

    fun disconnect() {
        webSocket.close(1000, "App closed")
    }
}