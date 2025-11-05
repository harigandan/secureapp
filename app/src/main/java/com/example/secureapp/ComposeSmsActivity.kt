package com.example.secureapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.widget.LinearLayout

class ComposeSmsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply { text = "Compose SMS Activity" }
        val layout = LinearLayout(this).apply { addView(tv) }
        setContentView(layout)
    }
}

