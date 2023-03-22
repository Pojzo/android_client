package com.example.client

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var ipInput: EditText
    private lateinit var portInput: EditText
    private lateinit var saveChangesBtn: Button
    private lateinit var backBtn: Button

    private var sharedPreference = getSharedPreferences("NetworkSettings", Context.MODE_PRIVATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        ipInput = findViewById(R.id.settings_ip_input)
        portInput = findViewById(R.id.settings_port_input)
        saveChangesBtn = findViewById(R.id.settings_save_changes_btn)
        backBtn = findViewById(R.id.back_btn)

        ipInput.text = Editable.Factory.getInstance().newEditable(sharedPreference.getString("IP", "0.0.0.0"))
        portInput.text = Editable.Factory.getInstance().newEditable(sharedPreference.getInt("PORT", 44444).toString())

        saveChangesBtn.setOnClickListener {
            updateSharedPreferences()
        }

        backBtn.setOnClickListener {
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateSharedPreferences() {
        val ip = ipInput.text.toString()
        val port = portInput.text.toString().toInt()

        val editor = sharedPreference.edit()
        editor.putString("IP", ip)
        editor.putInt("PORT", port)
        editor.apply()
    }
}
