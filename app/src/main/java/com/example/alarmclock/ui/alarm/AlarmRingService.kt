package com.example.alarmclock.ui.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.alarmclock.R
import com.example.alarmclock.data.AlarmStorage
import com.example.alarmclock.util.AlarmScheduler

class AlarmRingService : Service() {

    companion object {
        const val ACTION_START_RING = "com.example.alarmclock.ACTION_START_RING"
        const val ACTION_SNOOZE = "com.example.alarmclock.ACTION_SNOOZE"
        const val ACTION_DISMISS = "com.example.alarmclock.ACTION_DISMISS"

        const val CHANNEL_ID = "alarm_clock_channel_high"
        const val CHANNEL_NAME = "Alarm Ring Channel"
        const val NOTIFICATION_ID = 9999
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private var alarmId: Long = -1L
    private var alarmTitle: String = "Alarm"
    private var alarmTime: String = ""
    private var isVibrate: Boolean = true
    private var ringtoneUriString: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_START_RING -> handleStartRing(intent)
            ACTION_SNOOZE -> handleSnooze()
            ACTION_DISMISS -> handleDismiss()
        }

        return START_STICKY
    }

    private fun handleStartRing(intent: Intent) {
        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        val rawTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE)
        alarmTitle = if (rawTitle.isNullOrBlank() || rawTitle.equals("Alarm", true)) {
            getString(R.string.tab_alarm)
        } else {
            rawTitle
        }
        alarmTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""
        isVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_VIBRATE, true)
        ringtoneUriString = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI)

        val cal = java.util.Calendar.getInstance()
        val isAm = cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM
        val amPmStr = if (isAm) getString(R.string.time_am) else getString(R.string.time_pm)
        val timeDisplay = if (alarmTime.isNotEmpty()) "$alarmTime $amPmStr" else ""

        acquireWakeLock()
        createNotificationChannel()

        val ringIntent = Intent(this, AlarmRingActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, alarmTitle)
            putExtra(AlarmScheduler.EXTRA_ALARM_TIME, alarmTime)
            putExtra(AlarmScheduler.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUriString)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            ringIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmRingService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_TITLE, alarmTitle)
            putExtra(AlarmScheduler.EXTRA_ALARM_TIME, alarmTime)
            putExtra(AlarmScheduler.EXTRA_IS_VIBRATE, isVibrate)
            putExtra(AlarmScheduler.EXTRA_RINGTONE_URI, ringtoneUriString)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            (alarmId.toInt() * 10) + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmRingService::class.java).apply {
            action = ACTION_DISMISS
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            (alarmId.toInt() * 10) + 2,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_alarm)
            .setContentTitle(alarmTitle)
            .setContentText(if (timeDisplay.isNotEmpty()) "$timeDisplay • ${getString(R.string.alarm_ringing)}" else getString(R.string.alarm_ringing))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(0, getString(R.string.snooze_later), snoozePendingIntent)
            .addAction(0, getString(R.string.dismiss), dismissPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startAlarmSound()
        if (isVibrate) startVibration()
    }

    private fun handleSnooze() {
        stopSoundAndVibration()
        AlarmScheduler.scheduleSnooze(
            context = this,
            alarmId = alarmId,
            title = alarmTitle,
            timeFormatted = alarmTime,
            snoozeMinutes = 5,
            isVibrate = isVibrate,
            ringtoneUri = ringtoneUriString
        )
        Toast.makeText(this, getString(R.string.alarm_snoozed), Toast.LENGTH_SHORT).show()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleDismiss() {
        stopSoundAndVibration()
        if (alarmId != -1L) {
            val alarm = AlarmStorage.getAlarms(this).find { it.id == alarmId }
            if (alarm != null && alarm.repeatDays.isEmpty()) {
                AlarmStorage.updateAlarm(this, alarm.copy(isEnabled = false))
            }
        }
        Toast.makeText(this, getString(R.string.alarm_dismissed), Toast.LENGTH_SHORT).show()
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "AlarmClock:AlarmRingServiceWakeLock"
            )
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
    }

    private fun startAlarmSound() {
        try {
            val uri: Uri = resolveRingtoneUri()
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val targetVol = (maxVol * 0.85).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVol, 0)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(applicationContext, uri)
                isLooping = true
                setOnPreparedListener { mp -> mp.start() }
                setOnErrorListener { _, _, _ ->
                    tryFallbackRingtone()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tryFallbackRingtone()
        }
    }

    private fun resolveRingtoneUri(): Uri {
        if (!ringtoneUriString.isNullOrEmpty()) {
            return Uri.parse(ringtoneUriString)
        }
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?.let { return it }
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)?.let { return it }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    private fun tryFallbackRingtone() {
        try {
            val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: return

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(applicationContext, fallbackUri)
                isLooping = true
                setOnPreparedListener { mp -> mp.start() }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        try {
            val vib = getVibrator()
            val pattern = longArrayOf(0L, 600L, 400L, 600L, 400L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, 0)
            }
            vibrator = vib
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun stopSoundAndVibration() {
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null

        try { vibrator?.cancel() } catch (_: Exception) {}
        vibrator = null
    }

    override fun onDestroy() {
        stopSoundAndVibration()
        releaseWakeLock()
        super.onDestroy()
    }
}
