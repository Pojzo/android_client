package com.example.client

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

enum class MessageType {
    TXT
}

object SocketManager {
    private lateinit var context: Context
    private var socket: Socket? = null
    private lateinit var socketWriter: PrintWriter
    private lateinit var socketReader: BufferedReader
    private var callback: SocketCallback? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun setSocketCallback(callback: SocketCallback) {
        this.callback = callback
    }

    fun isConnected(): Boolean {
        return socket?.isConnected ?: false
    }

    fun getSocketWriter(): PrintWriter {
        return socketWriter
    }

    fun getSocketReader(): BufferedReader {
        return socketReader
    }

    fun getHost(): String {
        return socket?.inetAddress?.hostAddress ?: "-1.-1.-1.-1"
    }

    fun getPort(): Int {
        return socket?.port ?: -99999
    }

    fun close() {
        socket?.close()
    }

    suspend fun connect() {
        val sharedPreference = this.context.getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("IP", "0.0.0.0")
        val port = sharedPreference.getInt("PORT", 44444)
        println("Trying to connect to $ip with port $port")
        try {
            socket = Socket(ip, port)
        } catch (e: Exception) {
            println(e)
            println("failed")
            return
        }

        if (!socket?.isConnected!!) {
            return
        }
        socketWriter = PrintWriter(socket?.getOutputStream()!!, true)
        socketReader = BufferedReader(InputStreamReader(socket?.getInputStream()))
        //connectionStatusText.text = "Connected to $ip"

        // update the connection status
        withContext(Dispatchers.Main) {
            callback?.onConnectionStatusUpdated("Connected")
        }

        runSocket()
    }

    private suspend fun runSocket() {
        println("Sem som sa dostal")
            while (true) {
                println("This is running")
                if (!isConnected()) {
                    callback?.onConnectionStatusUpdated("Disconnected by the server")
                    break
                }
                var serverMsg: String
                try {
                    serverMsg = socketReader.readLine()
                } catch (e: Exception) {
                    println("Ukoncujem to brasko")
                    println(e)
                    break
                }
                val jsonMessage = JSONObject(serverMsg)
                val messageType = jsonMessage.getString("type")
                val messageContent = jsonMessage.getString("content")
                val messageTimestamp = jsonMessage.getString("timestamp")
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss'Z'")
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val timestamp = dateFormat.format(Date())

                withContext(Dispatchers.Main) {
                    callback?.onMessageReceived(messageType, messageContent, messageTimestamp)
                }
            }
        socketWriter.close()
        socketReader.close()

        socket!!.close()
        callback?.onConnectionStatusUpdated("Disconnected")
    }

    suspend fun sendMessage(message: String) {
        if (socket == null) return
        if (!socket!!.isConnected) {
            return
        }
        socketWriter.println(message)
    }

    suspend fun disconnect() {
        if (socket != null) {
            socketWriter.close()
            socketReader.close()
            socket!!.close()
            println("Teraz by som mal zavolat callback")
        }
        withContext(Dispatchers.Main) {
            callback?.onConnectionStatusUpdated("Disconnected")
        }
    }
}


/*
class ClientSocket{
    private var hostIP: String? = null
    private var port: Int? = null
    private var initialized: Boolean = false
    private var connected: Boolean = false
    private lateinit var socket: Socket

    private fun setIP(host: String): Boolean {
        val reg0To255 = ("(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])")
        // regex 0 To 255 followed by a dot, 4 times repeat
        // validation an IP address.
        val regex = (reg0To255 + "\\."
                + reg0To255 + "\\."
                + reg0To255 + "\\."
                + reg0To255)
        val p = Pattern.compile(regex)
        val m = p.matcher(host)
        if (m.matches()) {
            hostIP = host
            return true
        }
        return false
    }

    // port should always be entered second
    private fun setPORT(port: Int): Boolean {
        // I'll change this later
        if (port in 1025..49999) {
            this.port = port
            return true
        }
        return false
    }

    fun initialize(host: String, port: Int): Boolean {
        val setupOK = setIP(host) && setPORT(port)
        if (!setupOK) {
            this.hostIP = null
            this.port = null
            return false
        }
        initialized = true
        return true
    }

    fun connect(): Boolean {
        println("Dostal som sa vobec sem")
        if (!initialized) return false

        Thread {
            try {
                this.socket = port?.let { Socket(hostIP, it) } ?: return@Thread
            } catch (e: Exception) {
                println("Nepodarilo sa pripojit")
                return@Thread
            }

            connected = socket.isConnected
        }.start()
        return connected
    }
}

 */