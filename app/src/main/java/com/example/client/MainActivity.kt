package com.example.client

//noinspection SuspiciousImport
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface SocketCallback {
    fun onMessageReceived(data: String)
    fun onConnectionStatusUpdated(data: String)
}

class MainActivity : AppCompatActivity(), SocketCallback {

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

    override fun onMessageReceived(data: String) {
        recvMsgText.text = data
    }

    override fun onConnectionStatusUpdated(data: String) {
        connectionStatusText.text = data
    }


    private fun setUpBtnListeners() {
        // button to open settings
        settingsBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        // create connection to the server
        val scope = CoroutineScope(Dispatchers.IO)
        connectBtn.setOnClickListener {
            scope.launch {
                SocketManager.init(applicationContext)
                SocketManager.setSocketCallback(this@MainActivity)
                SocketManager.connect()
            }
        }
        // send a message to the client
        sendMsgButton.setOnClickListener {
            val clientMsg = sendMsgInput.text.toString()
            scope.launch {
                SocketManager.sendMessage(clientMsg)
            }
        }
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
    override fun onResume() {
        super.onResume()
        val sharedPreference = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE)
        val ip = sharedPreference.getString("IP", defaultIP)
        val port = sharedPreference.getInt("PORT", defaultPort)
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
    override fun onDestroy() {
        super.onDestroy()
        if (SocketManager.isConnected()) {
            onConnectionStatusUpdated("Connected")
            val hostIP = SocketManager.getHost()
            val port = SocketManager.getPort()
            updateStatusIpPort(hostIP, port)
        }
        else {
            onConnectionStatusUpdated("Disconnected")
        }
    }
}


