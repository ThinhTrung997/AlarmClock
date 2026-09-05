package com.example.alarmclock.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.alarmclock.R
import com.example.alarmclock.util.AlarmScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingActivity : AppCompatActivity() {

    private var alarmId: Long = -1L
    private var alarmTitle: String = "Alarm"
    private var alarmTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeAndUnlockScreen()
        setContentView(R.layout.activity_alarm_ring)

        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val rawTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE)
        alarmTitle = if (rawTitle.isNullOrBlank() || rawTitle.equals("Alarm", true)) {
            getString(R.string.tab_alarm)
        } else {
            rawTitle
        }
        alarmTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""

        if (alarmTime.isEmpty()) {
            alarmTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
        }

        setupViews()
    }

    // ─── Wake screen even if locked ──────────────────────────────────────────

    private fun wakeAndUnlockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)
                ?.requestDismissKeyguard(this, null)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
    }

    // ─── Views ───────────────────────────────────────────────────────────────

    private fun setupViews() {
        val cal = java.util.Calendar.getInstance()
        val isAm = cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM
        val amPmStr = if (isAm) getString(R.string.time_am) else getString(R.string.time_pm)

        findViewById<TextView>(R.id.tvRingTime).text = "$alarmTime $amPmStr"
        findViewById<TextView>(R.id.tvRingTitle).text = alarmTitle

        findViewById<LinearLayout>(R.id.btnSnooze).setOnClickListener { onSnoozeClicked() }
        findViewById<LinearLayout>(R.id.btnDismiss).setOnClickListener { onDismissClicked() }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private fun onSnoozeClicked() {
        val serviceIntent = Intent(this, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_SNOOZE
        }
        try {
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (_: Exception) {}
        finish()
    }

    private fun onDismissClicked() {
        val serviceIntent = Intent(this, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_DISMISS
        }
        try {
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (_: Exception) {}
        finish()
    }

    /** Prevent back-press from dismissing the alarm silently */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing – user must explicitly snooze or dismiss
    }
}

