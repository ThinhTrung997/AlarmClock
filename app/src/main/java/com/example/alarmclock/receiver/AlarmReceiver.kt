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
import androidx.core.content.ContextCompat
import com.example.alarmclock.R
import com.example.alarmclock.ui.alarm.AlarmRingActivity
import com.example.alarmclock.ui.alarm.AlarmRingService
import com.example.alarmclock.util.AlarmScheduler

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_clock_channel_high"
        const val CHANNEL_NAME = "Alarm Ring Channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val rawTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE)
        val title = if (rawTitle.isNullOrBlank() || rawTitle.equals("Alarm", true)) {
            context.getString(R.string.tab_alarm)
        } else {
            rawTitle
        }
        val time = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""
        val isVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_VIBRATE, true)
        val ringtoneUri = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI)

        // 1. Acquire WakeLock IMMEDIATELY so device stays awake even if locked/asleep
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "AlarmClock:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(30000L) // 30s wake lock

        // 2. Post notification directly
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

        val ringIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_ALARM_TIME, time)
            putExtra(AlarmScheduler.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
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

        val snoozeIntent = Intent(context, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_ALARM_TIME, time)
            putExtra(AlarmScheduler.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
        }
        val snoozePendingIntent = PendingIntent.getService(
            context,
            (alarmId.toInt() * 10) + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            context,
            (alarmId.toInt() * 10) + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = java.util.Calendar.getInstance()
        val isAm = cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM
        val amPmStr = if (isAm) context.getString(R.string.time_am) else context.getString(R.string.time_pm)
        val timeDisplay = if (time.isNotEmpty()) "$time $amPmStr" else ""

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_alarm)
            .setContentTitle(title)
            .setContentText(if (timeDisplay.isNotEmpty()) "$timeDisplay • ${context.getString(R.string.alarm_ringing)}" else context.getString(R.string.alarm_ringing))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, context.getString(R.string.snooze_later), snoozePendingIntent)
            .addAction(0, context.getString(R.string.dismiss), dismissPendingIntent)
            .build()

        notificationManager.notify(AlarmRingService.NOTIFICATION_ID, notification)

        // 3. Start Foreground Service to play sound and handle notification
        val serviceIntent = Intent(context, AlarmRingService::class.java).apply {
            action = AlarmRingService.ACTION_START_RING
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, title)
            putExtra(AlarmScheduler.EXTRA_ALARM_TIME, time)
            putExtra(AlarmScheduler.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUri)
        }

        try {
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Also launch activity directly if screen is unlocked / allowed
        try {
            context.startActivity(ringIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
