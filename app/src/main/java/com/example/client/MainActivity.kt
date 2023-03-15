package com.example.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var value = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val incrementButton = findViewById<Button>(R.id.increment_button)
        val valueTextView = findViewById<TextView>(R.id.value_text_view)

        incrementButton.setOnClickListener {
            value++
            valueTextView.text = value.toString()
        }
    }
}
