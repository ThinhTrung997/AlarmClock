package com.example.alarmclock.util

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.alarmclock.R
import com.example.alarmclock.ui.alarm.MainActivity
import com.example.alarmclock.ui.stopwatch.StopwatchActivity
import com.example.alarmclock.ui.worldclock.WorldClockActivity

enum class NavigationTab {
    ALARM,
    WORLD_CLOCK,
    STOPWATCH
}

object NavigationHelper {

    fun setupBottomNavigation(activity: Activity, currentTab: NavigationTab) {
        val tabAlarm = activity.findViewById<LinearLayout?>(R.id.tabAlarm) ?: return
        val tabWorldClock = activity.findViewById<LinearLayout?>(R.id.tabWorldClock) ?: return
        val tabStopwatch = activity.findViewById<LinearLayout?>(R.id.tabStopwatch) ?: return

        val ivTabAlarm = activity.findViewById<ImageView?>(R.id.ivTabAlarm)
        val tvTabAlarm = activity.findViewById<TextView?>(R.id.tvTabAlarm)

        val ivTabWorldClock = activity.findViewById<ImageView?>(R.id.ivTabWorldClock)
        val tvTabWorldClock = activity.findViewById<TextView?>(R.id.tvTabWorldClock)

        val ivTabStopwatch = activity.findViewById<ImageView?>(R.id.ivTabStopwatch)
        val tvTabStopwatch = activity.findViewById<TextView?>(R.id.tvTabStopwatch)

        val activeTextColor = ContextCompat.getColor(activity, R.color.bottom_nav_active_text)
        val activeBgRes = R.drawable.bg_bottom_nav_active

        val inactiveTextColor = ContextCompat.getColor(activity, R.color.bottom_nav_inactive)
        val inactiveIconColor = ContextCompat.getColor(activity, R.color.bottom_nav_inactive)

        // Style Tab: ALARM
        if (currentTab == NavigationTab.ALARM) {
            tabAlarm.setBackgroundResource(activeBgRes)
            tvTabAlarm?.setTextColor(activeTextColor)
            ivTabAlarm?.colorFilter = PorterDuffColorFilter(activeTextColor, PorterDuff.Mode.SRC_IN)
        } else {
            tabAlarm.background = null
            tvTabAlarm?.setTextColor(inactiveTextColor)
            ivTabAlarm?.colorFilter = PorterDuffColorFilter(inactiveIconColor, PorterDuff.Mode.SRC_IN)
        }

        // Style Tab: WORLD CLOCK
        if (currentTab == NavigationTab.WORLD_CLOCK) {
            tabWorldClock.setBackgroundResource(activeBgRes)
            tvTabWorldClock?.setTextColor(activeTextColor)
            ivTabWorldClock?.colorFilter = PorterDuffColorFilter(activeTextColor, PorterDuff.Mode.SRC_IN)
        } else {
            tabWorldClock.background = null
            tvTabWorldClock?.setTextColor(inactiveTextColor)
            ivTabWorldClock?.colorFilter = PorterDuffColorFilter(inactiveIconColor, PorterDuff.Mode.SRC_IN)
        }

        // Style Tab: STOPWATCH
        if (currentTab == NavigationTab.STOPWATCH) {
            tabStopwatch.setBackgroundResource(activeBgRes)
            tvTabStopwatch?.setTextColor(activeTextColor)
            ivTabStopwatch?.colorFilter = PorterDuffColorFilter(activeTextColor, PorterDuff.Mode.SRC_IN)
        } else {
            tabStopwatch.background = null
            tvTabStopwatch?.setTextColor(inactiveTextColor)
            ivTabStopwatch?.colorFilter = PorterDuffColorFilter(inactiveIconColor, PorterDuff.Mode.SRC_IN)
        }

        // Navigation click listeners
        tabAlarm.setOnClickListener {
            if (currentTab != NavigationTab.ALARM) {
                val intent = Intent(activity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                activity.startActivity(intent)
                @Suppress("DEPRECATION")
                activity.overridePendingTransition(0, 0)
            }
        }

        tabWorldClock.setOnClickListener {
            if (currentTab != NavigationTab.WORLD_CLOCK) {
                val intent = Intent(activity, WorldClockActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                activity.startActivity(intent)
                @Suppress("DEPRECATION")
                activity.overridePendingTransition(0, 0)
            }
        }

        tabStopwatch.setOnClickListener {
            if (currentTab != NavigationTab.STOPWATCH) {
                val intent = Intent(activity, StopwatchActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                activity.startActivity(intent)
                @Suppress("DEPRECATION")
                activity.overridePendingTransition(0, 0)
            }
        }
    }
}
