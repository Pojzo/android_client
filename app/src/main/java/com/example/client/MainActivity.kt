package com.example.client

//noinspection SuspiciousImport
import android.annotation.SuppressLint
import android.content.Context
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
    private lateinit var connectBtn: Button
    private lateinit var settingsBtn: Button
    private lateinit var ipStatusText: TextView
    private lateinit var portStatusText: TextView
    private lateinit var connectionStatusText: TextView


    private var defaultIP: String = "0.0.0.0"
    private var defaultPort: Int = 4444

    private lateinit var socket: Socket
    private lateinit var socketWriter: PrintWriter
    private lateinit var socketReader: BufferedReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendMsgButton = findViewById(R.id.send_msg_btn)
        sendMsgInput = findViewById(R.id.send_msg_input_id)
        recvMsgText = findViewById(R.id.recv_msg_text_id)
        connectBtn = findViewById(R.id.connect_btn_id)
        settingsBtn = findViewById(R.id.settings_btn_id)
        connectionStatusText = findViewById(R.id.connection_status_text_id)
        ipStatusText = findViewById(R.id.ip_status_text_id)
        portStatusText = findViewById(R.id.port_status_text_id)

        initSharedPreferences()
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
            val sharedPreference = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE)
            val ip = sharedPreference.getString("IP", "0.0.0.0")
            val port = sharedPreference.getInt("PORT", 44444)
            socket = Socket(ip, port)
            if (!socket.isConnected) {
                return@Thread
            }
            socketWriter = PrintWriter(socket.getOutputStream(), true)
            socketReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            connectionStatusText.text = "Connected to $ip"

            runSocket()
        }
        thread.start()
    }

    private fun initSharedPreferences() {
        val sharedPreference = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        if (!sharedPreference.contains("IP")) {
            editor.putString("IP", defaultIP)
        }
        if (!sharedPreference.contains("PORT")) {
            editor.putInt("PORT", defaultPort)
        }
        //
        editor.commit()

        updateStatusIpPort(defaultIP, defaultPort)
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

    override fun onResume() {
        super.onResume()
        val sharedPreference = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("IP", defaultIP)
        val port = sharedPreference.getInt("PORT", defaultPort)
        println("A ked sa to resumlo: ".plus(ip))
        updateStatusIpPort(ip, port)
    }

    private fun updateStatusIpPort(ip: String?, port: Int) {
        val ipString: String = if (ip.isNullOrBlank()) {
            "IP: ".plus(defaultIP)
        } else {
            "IP: ".plus(ip)
        }
        val portString = "PORT :".plus(port.toString())
        ipStatusText.text = ipString
        portStatusText.text = portString
    }
}


