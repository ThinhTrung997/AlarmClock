package com.example.alarmclock.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.alarmclock.data.AlarmStorage
import com.example.alarmclock.model.Alarm
import com.example.alarmclock.receiver.AlarmReceiver
import com.example.alarmclock.ui.alarm.MainActivity
import java.util.Calendar

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"
    const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
    const val EXTRA_ALARM_TITLE = "EXTRA_ALARM_TITLE"
    const val EXTRA_ALARM_TIME = "EXTRA_ALARM_TIME"
    const val EXTRA_IS_VIBRATE = "EXTRA_IS_VIBRATE"
    const val EXTRA_RINGTONE_URI = "EXTRA_RINGTONE_URI"

    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.isEnabled) {
            cancelAlarm(context, alarm.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = calculateNextTriggerTime(alarm)

        val intent = buildReceiverIntent(context, alarm.id, alarm.title, alarm.time, alarm.isVibrate, alarm.ringtoneUri)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val showIntent = Intent(context, MainActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                alarm.id.toInt(),
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
                    pendingIntent
                )
            } catch (se: SecurityException) {
                Log.w(TAG, "setAlarmClock SecurityException, falling back: ${se.message}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
            Log.d(TAG, "Scheduled alarm ID: ${alarm.id} at millis: $triggerTime")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}")
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled alarm ID: $alarmId")
    }

    fun scheduleSnooze(
        context: Context,
        alarmId: Long,
        title: String,
        timeFormatted: String,
        snoozeMinutes: Int = 10,
        isVibrate: Boolean = true,
        ringtoneUri: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        val intent = buildReceiverIntent(context, alarmId, title, timeFormatted, isVibrate, ringtoneUri)

        // Use a unique request code for snooze so it doesn't overwrite the main alarm PendingIntent
        val snoozeRequestCode = (alarmId.toInt() xor 0x00FFFF00)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            snoozeRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val showIntent = Intent(context, MainActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context,
                alarmId.toInt(),
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
                    pendingIntent
                )
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent),
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled snooze for ID: $alarmId in $snoozeMinutes min")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling snooze: ${e.message}")
        }
    }

    fun rescheduleAllActiveAlarms(context: Context) {
        val alarms = AlarmStorage.getAlarms(context)
        for (alarm in alarms) {
            if (alarm.isEnabled) {
                scheduleAlarm(context, alarm)
            }
        }
    }

    private fun buildReceiverIntent(
        context: Context,
        alarmId: Long,
        title: String,
        time: String,
        isVibrate: Boolean,
        ringtoneUri: String?
    ): Intent {
        return Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_TITLE, title)
            putExtra(EXTRA_ALARM_TIME, time)
            putExtra(EXTRA_IS_VIBRATE, isVibrate)
            if (!ringtoneUri.isNullOrEmpty()) {
                putExtra(EXTRA_RINGTONE_URI, ringtoneUri)
            }
        }
    }

    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        val isPm = alarm.amPm.equals("PM", ignoreCase = true) ||
                   alarm.amPm.equals("CH", ignoreCase = true) ||
                   alarm.amPm.contains("Chiều", ignoreCase = true) ||
                   alarm.amPm.contains("Tối", ignoreCase = true)

        var hour24 = if (alarm.hour == 12) 0 else alarm.hour
        if (isPm) {
            hour24 += 12
        }

        val now = Calendar.getInstance()

        // 1. Specific date selected
        if (alarm.dateMillis != null && alarm.dateMillis > 0) {
            val target = Calendar.getInstance().apply {
                timeInMillis = alarm.dateMillis
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis > now.timeInMillis) {
                return target.timeInMillis
            }
        }

        // 2. No repeat days (one-time alarm for next occurrence: today if future, else tomorrow)
        if (alarm.repeatDays.isEmpty()) {
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        // 3. Repeat days
        val dayMapping = mapOf(
            0 to Calendar.MONDAY,
            1 to Calendar.TUESDAY,
            2 to Calendar.WEDNESDAY,
            3 to Calendar.THURSDAY,
            4 to Calendar.FRIDAY,
            5 to Calendar.SATURDAY,
            6 to Calendar.SUNDAY
        )

        var closestTargetTime = 0L
        for (dayIndex in alarm.repeatDays) {
            val targetDayOfWeek = dayMapping[dayIndex] ?: continue
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            var daysToAdd = (targetDayOfWeek - target.get(Calendar.DAY_OF_WEEK) + 7) % 7
            if (daysToAdd == 0 && target.timeInMillis <= now.timeInMillis) {
                daysToAdd = 7
            }
            target.add(Calendar.DAY_OF_YEAR, daysToAdd)

            if (closestTargetTime == 0L || target.timeInMillis < closestTargetTime) {
                closestTargetTime = target.timeInMillis
            }
        }

        return if (closestTargetTime != 0L) closestTargetTime else (now.timeInMillis + 60000)
    }
}
