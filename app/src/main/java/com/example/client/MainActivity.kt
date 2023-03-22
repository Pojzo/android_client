package com.example.client

//noinspection SuspiciousImport
import android.annotation.SuppressLint
import android.content.Intent
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
    private lateinit var settingsBtn: Button

    private val destinationAddress = "147.229.186.51"
    private val port = 44444

    private lateinit var socket: Socket
    private lateinit var socketWriter: PrintWriter
    private lateinit var socketReader: BufferedReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendMsgButton = findViewById(R.id.send_msg_btn)
        sendMsgInput = findViewById(R.id.send_msg_input)
        recvMsgText = findViewById(R.id.recv_msg_text)
        connectionStatusText = findViewById(R.id.connection_status_text)
        connectBtn = findViewById(R.id.connect_btn)
        settingsBtn = findViewById(R.id.settings_btn)

        setUpBtnListeners()
    }

    private fun setUpBtnListeners() {
        // button to open settings
        settingsBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        // create connection to the server
        connectBtn.setOnClickListener {
            connect()
        }
        // send a message to the client
        sendMsgButton.setOnClickListener {
            sendButtonClicked()
        }
    }

    private fun sendButtonClicked() {
        if (!::socket.isInitialized) {
            return
        }
        if (!socket.isConnected) {
            return
        }
        val clientMsg = sendMsgInput.text
        Thread {
            socketWriter.println(clientMsg)
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun connect() {
        val thread = Thread {
            socket = Socket(destinationAddress, port)
            if (!socket.isConnected) {
                return@Thread
            }
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

