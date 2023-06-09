package com.example.client

//noinspection SuspiciousImport
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

interface SocketCallback {
    fun onMessageReceived(
        type: String, content: String, timestamp:
        String
    )
    fun onConnectionStatusUpdated(data: String)
}

class MainActivity : AppCompatActivity(), SocketCallback {

    private lateinit var sendMsgButton: Button
    private lateinit var sendMsgInput: TextInputEditText
    private lateinit var recvMsgText: TextView
    private lateinit var recvMsgScrollView: ScrollView
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
        recvMsgScrollView = findViewById(R.id.recv_msg_scroll_view_id)
        connectBtn = findViewById(R.id.connect_btn_id)
        settingsBtn = findViewById(R.id.settings_btn_id)
        connectionStatusText = findViewById(R.id.connection_status_text_id)
        ipStatusText = findViewById(R.id.ip_status_text_id)
        portStatusText = findViewById(R.id.port_status_text_id)
        updateRecvText()
        initSharedPreferences()
        setUpBtnListeners()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            SocketManager.disconnect()
        }
    }
    override fun onResume() {
        super.onResume()
        updateConnectionStatus()
    }
    override fun onDestroy() {
        super.onDestroy()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            SocketManager.disconnect()
        }
    }

    // method is called upon receiving a message from the server
    override fun onMessageReceived(type: String, content: String, timestamp: String) {
        /*val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = dateFormat.parse(timestamp)
         */
        val db = DBHelper(this, null)
        db.addMessage(type, "Server", content, timestamp)
        updateRecvText()
    }

    @SuppressLint("SetTextI18n")
    override fun onConnectionStatusUpdated(data: String) {
        println(data)
        connectionStatusText.text = data

        if (data == "Connected") {
            connectBtn.text = "Disconnect"
        }
        else {
            connectBtn.text = "Connect"
        }
    }

    private fun setUpBtnListeners() {
        // button to open settings
        settingsBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        // create connection to the server
        connectBtn.setOnClickListener {
            connectBntPressed()

        }
        // send a message to the client
        sendMsgButton.setOnClickListener {
            sendBtnPressed()
        }
    }

    private fun sendBtnPressed() {
        val scope = CoroutineScope(Dispatchers.IO)

        val clientMsg = sendMsgInput.text.toString()
        scope.launch {
            SocketManager.sendMessage(type="message", content=clientMsg)
        }
        val db = DBHelper(this, null)
        db.addMessage("message", "Client", clientMsg, MessageCreator.getTimestampString())
        updateRecvText()
    }
    private fun connectBntPressed() {
        val scope = CoroutineScope(Dispatchers.IO)

        if (connectBtn.text == "Connect") {
            scope.launch {
                SocketManager.init(applicationContext)
                SocketManager.setSocketCallback(this@MainActivity)
                SocketManager.connect()
            }
            return
        }
        // disconnect from the server
        scope.launch { SocketManager.disconnect() }
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
        editor.apply()
        val hostIP = sharedPreference.getString("IP", defaultIP)
        val port = sharedPreference.getInt("PORT", defaultPort)
        updateStatusIpPort(hostIP, port)
    }


    private fun updateConnectionStatus() {
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

    private fun updateRecvText() {
        val db = DBHelper(this, null)
        val cursor = db.getName()
        recvMsgText.text = ""
        cursor!!.moveToFirst()
        while (cursor.moveToNext()) {
            val type = cursor.getString(cursor.run { getColumnIndex(DBHelper.TYPE_COL) })
            val sender = cursor.getString(cursor.run { getColumnIndex(DBHelper.SENDER_COL) })
            val content = cursor.getString(cursor.run { getColumnIndex(DBHelper.CONTENT_COL) })
            val timestamp = cursor.getString(cursor.run { getColumnIndex(DBHelper.TIMESTAMP_COL) })
            recvMsgText.text = recvMsgText.text.toString() + ("$sender\t$timestamp $content \n")
        }
        recvMsgScrollView.post { recvMsgScrollView.fullScroll(View.FOCUS_DOWN) }

        cursor.close()
    }

}