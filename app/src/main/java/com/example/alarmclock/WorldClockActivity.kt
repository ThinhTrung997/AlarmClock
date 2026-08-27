package com.example.alarmclock

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class WorldClockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_world_clock)
        val tabAlarm = findViewById<LinearLayout>(R.id.tabAlarm)
        val tabStopwatch = findViewById<LinearLayout>(R.id.tabStopwatch)

        tabAlarm.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        tabStopwatch.setOnClickListener {
            startActivity(Intent(this, StopwatchActivity::class.java))
        }
    }
}