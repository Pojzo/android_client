package com.example.client

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket


class MainActivity : AppCompatActivity() {

    private lateinit var sendMsgButton: Button
    private lateinit var sendMsgInput: TextInputEditText
    private lateinit var recvMsgText: TextView
    private lateinit var connectionStatusText: TextView
    private lateinit var connectBtn: Button

    private val lock = Object()
    private val destinationAddress = "192.168.100.7"
    private val port = 12345

    private lateinit var socket: Socket
    private lateinit var socketWriter: PrintWriter
    private lateinit var socketReader: BufferedReader

    private var msg_out_ready: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendMsgButton = findViewById(R.id.send_msg_btn)
        sendMsgInput = findViewById(R.id.send_msg_input)
        recvMsgText = findViewById(R.id.recv_msg_text)
        connectionStatusText = findViewById(R.id.connection_status_text)
        connectBtn = findViewById(R.id.connect_btn)

        connectBtn.setOnClickListener {
            connect()
        }

        sendMsgButton.setOnClickListener {
            sendButtonClicked()
        }
    }

    private fun sendButtonClicked() {
        val clientMsg = sendMsgInput.text
        Thread {
            socketWriter.println(clientMsg)
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun connect() {
        val thread = Thread {
            socket = Socket(destinationAddress, port)
            if (!socket.isConnected()) {
                return@Thread
            }
            val is_connected = socket.isConnected()
            println("Should be connected: $is_connected")
            socketWriter = PrintWriter(socket.getOutputStream(), true)
            socketReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            connectionStatusText.text = "Connected to $destinationAddress"

            runSocket()
        }
        thread.start()
    }
    private fun runSocket() {
        while (true) {
            val serverMsg = socketReader.readLine()
            recvMsgText.text = serverMsg

            if (serverMsg == "BYE") {
                socketWriter.println("BYE")
                break
            }
        }
        socketReader.close()
        socket.close()
    }
}

