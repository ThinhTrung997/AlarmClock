package com.example.alarmclock.ui.stopwatch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.alarmclock.R
import java.util.Locale

class StopwatchService : Service() {

    enum class StopwatchState {
        STOPPED,
        RUNNING,
        PAUSED
    }

    inner class LocalBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }

    private val binder = LocalBinder()

    var currentState = StopwatchState.STOPPED
        private set

    var startTime = 0L
        private set

    var elapsedTime = 0L
        private set

    var lastLapTotalElapsed = 0L

    val lapList = mutableListOf<com.example.alarmclock.model.StopwatchLap>()

    private val handler = Handler(Looper.getMainLooper())
    private val notificationUpdateRunnable = object : Runnable {
        override fun run() {
            if (currentState == StopwatchState.RUNNING) {
                updateNotification()
                handler.postDelayed(this, 1000)
            }
        }
    }

    companion object {
        const val ACTION_START = "com.example.alarmclock.STOPWATCH_START"
        const val ACTION_PAUSE = "com.example.alarmclock.STOPWATCH_PAUSE"
        const val ACTION_RESUME = "com.example.alarmclock.STOPWATCH_RESUME"
        const val ACTION_STOP = "com.example.alarmclock.STOPWATCH_STOP"
        const val ACTION_STATE_CHANGED = "com.example.alarmclock.STOPWATCH_STATE_CHANGED"

        const val CHANNEL_ID = "stopwatch_service_channel"
        const val NOTIFICATION_ID = 8888
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_START -> startStopwatch()
            ACTION_PAUSE -> pauseStopwatch()
            ACTION_RESUME -> resumeStopwatch()
            ACTION_STOP -> stopStopwatch()
        }

        return START_STICKY
    }

    fun startStopwatch() {
        if (currentState == StopwatchState.RUNNING) return
        startTime = SystemClock.elapsedRealtime()
        elapsedTime = 0L
        lastLapTotalElapsed = 0L
        lapList.clear()
        currentState = StopwatchState.RUNNING

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        handler.removeCallbacks(notificationUpdateRunnable)
        handler.post(notificationUpdateRunnable)

        broadcastStateChange()
    }

    fun pauseStopwatch() {
        if (currentState != StopwatchState.RUNNING) return
        elapsedTime = SystemClock.elapsedRealtime() - startTime
        currentState = StopwatchState.PAUSED

        handler.removeCallbacks(notificationUpdateRunnable)
        updateNotification()

        broadcastStateChange()
    }

    fun resumeStopwatch() {
        if (currentState != StopwatchState.PAUSED) return
        startTime = SystemClock.elapsedRealtime() - elapsedTime
        currentState = StopwatchState.RUNNING

        handler.removeCallbacks(notificationUpdateRunnable)
        handler.post(notificationUpdateRunnable)

        updateNotification()
        broadcastStateChange()
    }

    fun stopStopwatch() {
        handler.removeCallbacks(notificationUpdateRunnable)
        currentState = StopwatchState.STOPPED
        elapsedTime = 0L
        startTime = 0L
        lastLapTotalElapsed = 0L
        lapList.clear()

        broadcastStateChange()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun recordLap(): com.example.alarmclock.model.StopwatchLap? {
        if (currentState != StopwatchState.RUNNING) return null
        val currentElapsed = getCurrentElapsed()
        val lapTime = currentElapsed - lastLapTotalElapsed
        lastLapTotalElapsed = currentElapsed

        val nextLapNumber = lapList.size + 1
        val lap = com.example.alarmclock.model.StopwatchLap(
            lapNumber = nextLapNumber,
            lapTimeMillis = lapTime,
            totalTimeMillis = currentElapsed
        )
        lapList.add(0, lap)
        return lap
    }

    fun getCurrentElapsed(): Long {
        return when (currentState) {
            StopwatchState.RUNNING -> SystemClock.elapsedRealtime() - startTime
            StopwatchState.PAUSED -> elapsedTime
            StopwatchState.STOPPED -> 0L
        }
    }

    private fun broadcastStateChange() {
        val intent = Intent(ACTION_STATE_CHANGED)
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.title_stopwatch),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Stopwatch notification channel"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val activityIntent = Intent(this, StopwatchActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalMillis = getCurrentElapsed()
        val hours = totalMillis / 3_600_000
        val minutes = (totalMillis / 60_000) % 60
        val seconds = (totalMillis / 1_000) % 60
        val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

        val statusText = if (currentState == StopwatchState.RUNNING) {
            getString(R.string.stopwatch_running)
        } else {
            getString(R.string.stopwatch_paused)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_alarm)
            .setContentTitle(getString(R.string.title_stopwatch))
            .setContentText("$timeString • $statusText")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)

        if (currentState == StopwatchState.RUNNING) {
            val pauseIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_PAUSE }
            val pausePendingIntent = PendingIntent.getService(
                this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, getString(R.string.pause), pausePendingIntent)
        } else if (currentState == StopwatchState.PAUSED) {
            val resumeIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_RESUME }
            val resumePendingIntent = PendingIntent.getService(
                this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, getString(R.string.resume), resumePendingIntent)
        }

        val stopIntent = Intent(this, StopwatchService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(0, getString(R.string.stop), stopPendingIntent)

        return builder.build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        handler.removeCallbacks(notificationUpdateRunnable)
        super.onDestroy()
    }
}
