package com.example.client

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

object SocketManager {
    private lateinit var context: Context
    private lateinit var socket: Socket
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
        return socket.isConnected
    }

    fun getSocketWriter(): PrintWriter {
        return socketWriter
    }

    fun getSocketReader(): BufferedReader {
        return socketReader
    }

    fun getHost(): String {
        return socket.inetAddress.hostAddress
    }

    fun getPort(): Int {
        return socket.port
    }

    fun close() {
        socket.close()
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

        if (!socket.isConnected) {
            return
        }
        socketWriter = PrintWriter(socket.getOutputStream(), true)
        socketReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        //connectionStatusText.text = "Connected to $ip"

        // update the connection status
        callback?.onConnectionStatusUpdated("Connected")
        runSocket()
    }

    private suspend fun runSocket() {
        println("Sem som sa dostal")
        while (true) {
            if (!socket.isConnected) {
                callback?.onConnectionStatusUpdated("Disconnected by the server")
            }
            var serverMsg: String = ""
            try {
                serverMsg = socketReader.readLine()
            }
            catch (e: Exception) {
                println(e)
            }
            callback?.onMessageReceived(serverMsg)
            //recvMsgText.text = serverMsg
        }
        socketWriter.close()
        socketReader.close()
        socket.close()
    }

    suspend fun sendMessage(message: String) {
        if (!socket.isConnected) {
            return
        }
        socketWriter.println(message)
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