package com.example.alarmclock.model

data class LanguageItem(
    val code: String,
    val displayName: String,
    val flagResId: Int,
    var isSelected: Boolean = false
)
