package com.example.alarmclock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewAlarm)

        val alarmList = mutableListOf(
            Alarm(
                title = "Morning Workout",
                time = "07:30",
                amPm = "AM",
                repeat = "Everyday",
                isEnabled = true
            ),
            Alarm(
                title = "Office Meeting",
                time = "12:30",
                amPm = "PM",
                repeat = "Mon, Tue, Wed, Thu, Fri",
                isEnabled = false
            ),
            Alarm(
                title = "Sleep Time",
                time = "02:00",
                amPm = "PM",
                repeat = "Mon, Tue, Wed, Thu, Fri",
                isEnabled = false
            )
        )

        alarmAdapter = AlarmAdapter(alarmList) { position, isEnabled ->
            // Callback when user toggles alarm switch
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = alarmAdapter
    }
}