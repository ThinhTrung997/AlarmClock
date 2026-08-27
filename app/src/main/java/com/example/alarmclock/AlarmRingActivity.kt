package com.example.alarmclock

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmRingActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private var alarmId: Long = -1L
    private var alarmTitle: String = "Alarm"
    private var alarmTime: String = ""
    private var isVibrate: Boolean = true
    private var ringtoneUriString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeAndUnlockScreen()
        setContentView(R.layout.activity_alarm_ring)

        alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        alarmTitle = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TITLE) ?: "Alarm"
        alarmTime = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TIME) ?: ""
        isVibrate = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_VIBRATE, true)
        ringtoneUriString = intent.getStringExtra(AlarmScheduler.EXTRA_RINGTONE_URI)

        if (alarmTime.isEmpty()) {
            alarmTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
        }

        setupViews()
        startAlarmSound()
        if (isVibrate) startVibration()
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
        findViewById<TextView>(R.id.tvRingTime).text = alarmTime
        findViewById<TextView>(R.id.tvRingTitle).text = alarmTitle

        findViewById<LinearLayout>(R.id.btnSnooze).setOnClickListener { onSnoozeClicked() }
        findViewById<LinearLayout>(R.id.btnDismiss).setOnClickListener { onDismissClicked() }
    }

    // ─── Sound ───────────────────────────────────────────────────────────────

    private fun startAlarmSound() {
        try {
            // Resolve URI: user-chosen ringtone → default alarm → default ringtone
            val uri: Uri = resolveRingtoneUri()

            // Force alarm stream volume to a reasonable level
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
                    // fallback: try system default alarm
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
        // 1. User-selected ringtone stored in the alarm
        if (!ringtoneUriString.isNullOrEmpty()) {
            return Uri.parse(ringtoneUriString)
        }
        // 2. Device default alarm sound
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?.let { return it }
        // 3. Ringtone fallback
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)?.let { return it }
        // 4. Notification fallback
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

    // ─── Vibration ───────────────────────────────────────────────────────────

    private fun startVibration() {
        try {
            val vib = getVibrator()
            // Pattern: wait 0ms, vibrate 600ms, pause 400ms – repeat from index 0
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

    // ─── Stop everything ─────────────────────────────────────────────────────

    private fun stopAll() {
        try { mediaPlayer?.stop() } catch (_: Exception) {}
        try { mediaPlayer?.release() } catch (_: Exception) {}
        mediaPlayer = null

        try { vibrator?.cancel() } catch (_: Exception) {}
        vibrator = null

        // Cancel the ongoing notification
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(alarmId.toInt())
        } catch (_: Exception) {}
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private fun onSnoozeClicked() {
        stopAll()
        AlarmScheduler.scheduleSnooze(
            context       = this,
            alarmId       = alarmId,
            title         = alarmTitle,
            timeFormatted = alarmTime,
            snoozeMinutes = 10,
            isVibrate     = isVibrate,
            ringtoneUri   = ringtoneUriString
        )
        Toast.makeText(this, "Báo thức sẽ nhắc lại sau 10 phút", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onDismissClicked() {
        stopAll()

        // Turn off one-time alarms automatically
        if (alarmId != -1L) {
            val alarm = AlarmStorage.getAlarms(this).find { it.id == alarmId }
            if (alarm != null && alarm.repeatDays.isEmpty()) {
                AlarmStorage.updateAlarm(this, alarm.copy(isEnabled = false))
            }
        }

        Toast.makeText(this, "Đã tắt báo thức", Toast.LENGTH_SHORT).show()
        finish()
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    override fun onDestroy() {
        stopAll()
        super.onDestroy()
    }

    /** Prevent back-press from dismissing the alarm silently */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing – user must explicitly snooze or dismiss
    }
}
