package com.example.alarmclock

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StopwatchActivity : AppCompatActivity() {

    private lateinit var stopwatchTime: TextView

    private val handler = Handler(Looper.getMainLooper())

    private var startTime = 0L
    private var elapsedTime = 0L
    private var isRunning = false

    private val stopwatchRunnable = object : Runnable {
        override fun run() {

            if (isRunning) {

                elapsedTime =
                    SystemClock.elapsedRealtime() - startTime

                updateTime()

                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        // Navigation
        val tabAlarm = findViewById<LinearLayout>(R.id.tabAlarm)
        val tabWorldClock = findViewById<LinearLayout>(R.id.tabWorldClock)

        tabAlarm.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        tabWorldClock.setOnClickListener {
            startActivity(Intent(this, WorldClockActivity::class.java))
        }


        // Stopwatch
        stopwatchTime = findViewById(R.id.stopwatchTime)

        val btnStartStop = findViewById<Button>(R.id.btnStartStop)

        btnStartStop.setOnClickListener {

            if (!isRunning) {

                startTime =
                    SystemClock.elapsedRealtime() - elapsedTime

                isRunning = true

                btnStartStop.text = "Stop"

                handler.post(stopwatchRunnable)

            } else {

                isRunning = false

                btnStartStop.text = "Start"

                handler.removeCallbacks(stopwatchRunnable)
            }
        }
    }

    private fun updateTime() {

        val hours = elapsedTime / 3_600_000
        val minutes = (elapsedTime / 60_000) % 60
        val seconds = (elapsedTime / 1_000) % 60

        stopwatchTime.text = String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(stopwatchRunnable)
    }
}