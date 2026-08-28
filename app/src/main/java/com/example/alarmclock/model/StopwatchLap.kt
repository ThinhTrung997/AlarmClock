package com.example.alarmclock.model

data class StopwatchLap(
    val lapNumber: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long
)
