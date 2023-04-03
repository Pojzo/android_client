package com.example.client

import org.json.JSONObject

open class MessageCreator {
    open fun createUniversalMessage(type: String, content: String, timestamp: String): JSONObject {
        val message = JSONObject()
        message.put("type", type)
        message.put("content", content)
        message.put("timestamp", timestamp)
        return message
    }
}

class ClientMessageCreator : MessageCreator() {
    fun createMessage(type: String, content: String, timestamp: String): String {
        val message = super.createUniversalMessage(type, content, timestamp)
        message.put("sender", "Client")

        return message.toString()
    }
}

class ServerMessageCreator : MessageCreator() {
    fun createMessage(type: String, content: String, timestamp: String): String {
        val message = super.createUniversalMessage(type, content, timestamp)
        message.put("sender", "Server")

        return message.toString()
    }
}