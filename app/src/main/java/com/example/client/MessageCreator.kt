package com.example.client

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object MessageCreator {
    private fun createUniversalMessage(type: String, content: String, timestamp: String): JSONObject {
        val message = JSONObject()
        message.put("type", type)
        message.put("content", content)
        message.put("timestamp", timestamp)
        return message
    }

    fun createClientMessage(type: String, content: String): String {
        val timestamp = getTimestampString()
        val message = createUniversalMessage(type, content, timestamp)
        message.put("sender", "Client")
        return message.toString()
    }

    fun getTimestampString(): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Bratislava")
        return dateFormat.format(Date())
    }

    fun createServerMessage(type: String, content: String, timestamp: String): String {
        val message = createUniversalMessage(type, content, timestamp)
        message.put("sender", "Server")
        return message.toString()
    }
}