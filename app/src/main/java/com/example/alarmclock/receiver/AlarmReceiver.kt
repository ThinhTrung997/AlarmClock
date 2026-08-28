package com.example.alarmclock.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.alarmclock.R
import com.example.alarmclock.ui.alarm.AlarmRingActivity
import com.example.alarmclock.util.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_clock_channel_high"
        const val CHANNEL_NAME = "Alarm Ring Channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "Alarm"
        val time = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""
        val isVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_VIBRATE, true)

        // Acquire a temporary partial wake lock to ensure the screen turns on
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "AlarmClock:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(10000L)

        // Intent to launch the AlarmRingActivity
        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_ALARM_TIME, time)
            putExtra(AlarmScheduler.EXTRA_IS_VIBRATE, isVibrate)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            ringIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification for ringing alarms"
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_alarm)
            .setContentTitle(title)
            .setContentText(time.ifEmpty { "Alarm is ringing!" })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)

        // Start activity directly
        try {
            context.startActivity(ringIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
