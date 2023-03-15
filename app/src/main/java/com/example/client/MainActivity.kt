package com.example.client

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var sendMsgButton: Button
    private lateinit var sendMsgInput: TextInputEditText
    private lateinit var recvMsgText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sendMsgButton = findViewById(R.id.send_msg_btn)
        sendMsgInput = findViewById(R.id.send_msg_input)
        recvMsgText = findViewById(R.id.recv_msg_text)

        Thread {
            try {
                createSocketConnection()
            } catch (e: Exception) {
                Log.e("SocketError", "Error occurred: ${e.message}")
            }
        }.start()
    }
}

private fun createSocketConnection() {
    val destinationAddress = "192.168.100.7"
    val port = 12345
    val socket = Socket(destinationAddress, port)
    val writer = PrintWriter(socket.getOutputStream(), true)
    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

    for (i in generateSequence(0) { it }) {
        writer.println("Hello")
        SystemClock.sleep(1000)
    }

    socket.close()

}