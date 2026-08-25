package com.example.alarmclock

data class Alarm(
    val title: String,
    val time: String,
    val amPm: String,
    val repeat: String,
    val isEnabled: Boolean
)