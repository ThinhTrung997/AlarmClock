package com.example.alarmclock.ui.alarm

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.example.alarmclock.R
import com.example.alarmclock.data.AlarmStorage
import com.example.alarmclock.model.Alarm
import com.example.alarmclock.util.AlarmScheduler
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddAlarmActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALARM_ID = "EXTRA_ALARM_ID"
        const val EXTRA_POSITION = "EXTRA_POSITION"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_AM_PM = "EXTRA_AM_PM"
        const val EXTRA_REPEAT = "EXTRA_REPEAT"
        const val EXTRA_IS_ENABLED = "EXTRA_IS_ENABLED"
        const val EXTRA_IS_VIBRATE = "EXTRA_IS_VIBRATE"
    }

    private var editAlarmId: Long = -1L
    private var editPosition: Int = -1
    private var isAm = true
    private val selectedDays = mutableSetOf(0, 1, 2, 3, 4)
    private val dayButtons = mutableListOf<TextView>()

    // Date selection: null = everyday (no specific date)
    private var selectedDateCalendar: Calendar? = null

    // Ringtone: null means "use device default"
    private var selectedRingtoneUri: Uri? = null
    private var selectedRingtoneName: String = "Default Alarm"

    private lateinit var npHour: NumberPicker
    private lateinit var npMinute: NumberPicker
    private lateinit var btnAm: TextView
    private lateinit var btnPm: TextView
    private lateinit var etLabel: EditText
    private lateinit var tvRepeatSummary: TextView
    private lateinit var switchVibration: SwitchCompat
    private lateinit var tvSoundName: TextView
    private lateinit var tvSelectedDate: TextView

    // Ringtone picker launcher
    private val ringtoneLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                selectedRingtoneUri = uri
                selectedRingtoneName = RingtoneManager
                    .getRingtone(this, uri)
                    ?.getTitle(this) ?: uri.lastPathSegment ?: "Custom"
            } else {
                selectedRingtoneUri = null
                selectedRingtoneName = "Default Alarm"
            }
            tvSoundName.text = selectedRingtoneName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_alarm)

        initViews()
        setupNumberPickers()
        loadDataFromIntent()
        setupHeader()
        setupAmPmToggle()
        setupDayToggles()
        setupDatePicker()
        setupSoundPicker()
        setupSaveActions()
    }

    private fun initViews() {
        npHour = findViewById(R.id.npHour)
        npMinute = findViewById(R.id.npMinute)
        btnAm = findViewById(R.id.btnAm)
        btnPm = findViewById(R.id.btnPm)
        etLabel = findViewById(R.id.etLabel)
        tvRepeatSummary = findViewById(R.id.tvRepeatSummary)
        switchVibration = findViewById(R.id.switchVibration)
        tvSoundName = findViewById(R.id.tvSoundName)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
    }

    private fun setupNumberPickers() {
        val twoDigitFormatter = NumberPicker.Formatter { v -> String.format("%02d", v) }

        npHour.minValue = 1
        npHour.maxValue = 12
        npHour.setFormatter(twoDigitFormatter)
        npHour.wrapSelectorWheel = true

        npMinute.minValue = 0
        npMinute.maxValue = 59
        npMinute.setFormatter(twoDigitFormatter)
        npMinute.wrapSelectorWheel = true
    }

    private fun loadDataFromIntent() {
        editAlarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        editPosition = intent.getIntExtra(EXTRA_POSITION, -1)

        val tvHeaderTitle = findViewById<TextView>(R.id.tvHeaderTitle)

        if (editAlarmId != -1L || editPosition >= 0) {
            tvHeaderTitle.text = "Edit Alarm"
            val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
            val time = intent.getStringExtra(EXTRA_TIME) ?: "06:30"
            val amPm = intent.getStringExtra(EXTRA_AM_PM) ?: "AM"
            val repeat = intent.getStringExtra(EXTRA_REPEAT) ?: "Everyday"
            val isVibrate = intent.getBooleanExtra(EXTRA_IS_VIBRATE, true)

            if (editAlarmId != -1L) {
                val stored = AlarmStorage.getAlarms(this).find { it.id == editAlarmId }
                if (stored?.ringtoneUri != null) {
                    selectedRingtoneUri = Uri.parse(stored.ringtoneUri)
                    selectedRingtoneName = RingtoneManager
                        .getRingtone(this, selectedRingtoneUri)
                        ?.getTitle(this) ?: "Custom"
                }
            }

            etLabel.setText(title)
            isAm = amPm.equals("AM", ignoreCase = true)
            switchVibration.isChecked = isVibrate

            parseTime(time)
            parseRepeat(repeat)
        } else {
            tvHeaderTitle.text = "Add Alarm"
            val now = Calendar.getInstance()
            val hour24 = now.get(Calendar.HOUR_OF_DAY)
            val minute = now.get(Calendar.MINUTE)
            isAm = hour24 < 12
            val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12

            npHour.value = hour12
            npMinute.value = minute
            updateAmPmUi()
        }

        tvSoundName.text = selectedRingtoneName
    }

    private fun parseTime(timeStr: String) {
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 6
            val minute = parts[1].toIntOrNull() ?: 30
            npHour.value = if (hour in 1..12) hour else 6
            npMinute.value = if (minute in 0..59) minute else 30
        }
        updateAmPmUi()
    }

    private fun parseRepeat(repeatStr: String) {
        selectedDays.clear()
        when {
            repeatStr.equals("Everyday", ignoreCase = true) -> selectedDays.addAll(0..6)
            repeatStr.equals("Weekends", ignoreCase = true) -> selectedDays.addAll(listOf(5, 6))
            repeatStr.equals("Never", ignoreCase = true) -> { /* empty */ }
            repeatStr.contains("Mon", ignoreCase = true) ||
            repeatStr.contains("Tue", ignoreCase = true) ||
            repeatStr.contains("Wed", ignoreCase = true) ||
            repeatStr.contains("Thu", ignoreCase = true) ||
            repeatStr.contains("Fri", ignoreCase = true) ||
            repeatStr.contains("Sat", ignoreCase = true) ||
            repeatStr.contains("Sun", ignoreCase = true) -> {
                if (repeatStr.contains("Mon")) selectedDays.add(0)
                if (repeatStr.contains("Tue")) selectedDays.add(1)
                if (repeatStr.contains("Wed")) selectedDays.add(2)
                if (repeatStr.contains("Thu")) selectedDays.add(3)
                if (repeatStr.contains("Fri")) selectedDays.add(4)
                if (repeatStr.contains("Sat")) selectedDays.add(5)
                if (repeatStr.contains("Sun")) selectedDays.add(6)
            }
            else -> selectedDays.addAll(listOf(0, 1, 2, 3, 4))
        }
    }

    private fun setupHeader() {
        findViewById<ImageView>(R.id.btnClose).setOnClickListener { finish() }
    }

    private fun setupAmPmToggle() {
        updateAmPmUi()
        btnAm.setOnClickListener { isAm = true; updateAmPmUi() }
        btnPm.setOnClickListener { isAm = false; updateAmPmUi() }
    }

    private fun updateAmPmUi() {
        val colorOnPrimary = ContextCompat.getColor(this, R.color.colorOnPrimary)
        val colorSecondary = ContextCompat.getColor(this, R.color.text_secondary)

        if (isAm) {
            btnAm.setBackgroundResource(R.drawable.bg_ampm_selected)
            btnAm.setTextColor(colorOnPrimary)
            btnPm.background = null
            btnPm.setTextColor(colorSecondary)
        } else {
            btnPm.setBackgroundResource(R.drawable.bg_ampm_selected)
            btnPm.setTextColor(colorOnPrimary)
            btnAm.background = null
            btnAm.setTextColor(colorSecondary)
        }
    }

    private fun setupDayToggles() {
        dayButtons.clear()
        dayButtons.addAll(
            listOf(
                findViewById(R.id.btnDayMon),
                findViewById(R.id.btnDayTue),
                findViewById(R.id.btnDayWed),
                findViewById(R.id.btnDayThu),
                findViewById(R.id.btnDayFri),
                findViewById(R.id.btnDaySat),
                findViewById(R.id.btnDaySun)
            )
        )

        dayButtons.forEachIndexed { index, textView ->
            updateDayButtonUi(textView, selectedDays.contains(index))
            textView.setOnClickListener {
                if (selectedDays.contains(index)) {
                    selectedDays.remove(index); updateDayButtonUi(textView, false)
                } else {
                    selectedDays.add(index); updateDayButtonUi(textView, true)
                }
                updateRepeatSummary()
            }
        }
        updateRepeatSummary()
    }

    private fun updateDayButtonUi(textView: TextView, isSelected: Boolean) {
        val colorOnPrimary = ContextCompat.getColor(this, R.color.colorOnPrimary)
        val colorSecondary = ContextCompat.getColor(this, R.color.text_secondary)

        if (isSelected) {
            textView.setBackgroundResource(R.drawable.bg_day_selected)
            textView.setTextColor(colorOnPrimary)
        } else {
            textView.setBackgroundResource(R.drawable.bg_day_unselected)
            textView.setTextColor(colorSecondary)
        }
    }

    private fun updateRepeatSummary() {
        tvRepeatSummary.text = when {
            selectedDays.size == 7 -> "Everyday"
            selectedDays.size == 5 && selectedDays.containsAll(listOf(0, 1, 2, 3, 4)) -> "Mon, Tue, Wed, Thu, Fri"
            selectedDays.size == 2 && selectedDays.containsAll(listOf(5, 6)) -> "Weekends"
            selectedDays.isEmpty() -> "Never"
            else -> {
                val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                selectedDays.sorted().joinToString(", ") { names[it] }
            }
        }
    }

    // ─── Date Picker ─────────────────────────────────────────────────────────

    private fun setupDatePicker() {
        val cardDate = findViewById<ConstraintLayout>(R.id.cardDate)
        cardDate.setOnClickListener {
            showDatePickerDialog()
        }
        updateDateDisplay()
    }

    private fun showDatePickerDialog() {
        val cal = selectedDateCalendar ?: Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val dialog = DatePickerDialog(this, { _, y, m, d ->
            val picked = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m)
                set(Calendar.DAY_OF_MONTH, d)
            }
            selectedDateCalendar = picked
            updateDateDisplay()
        }, year, month, day)

        // Option to clear date (go back to everyday)
        dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear Date") { _, _ ->
            selectedDateCalendar = null
            updateDateDisplay()
        }

        dialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        dialog.show()
    }

    private fun updateDateDisplay() {
        if (selectedDateCalendar == null) {
            tvSelectedDate.text = "Everyday (No specific date)"
        } else {
            val fmt = SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault())
            tvSelectedDate.text = fmt.format(selectedDateCalendar!!.time)
        }
    }

    // ─── Ringtone Picker ─────────────────────────────────────────────────────

    private fun setupSoundPicker() {
        findViewById<ConstraintLayout>(R.id.cardSound)
            .setOnClickListener { openRingtonePicker() }
    }

    private fun openRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Chọn nhạc báo thức")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedRingtoneUri)
        }
        ringtoneLauncher.launch(intent)
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    private fun setupSaveActions() {
        val saveListener = {
            val label = etLabel.text.toString().trim().ifBlank { "Alarm" }
            val hour = npHour.value
            val minute = npMinute.value
            val time = String.format("%02d:%02d", hour, minute)
            val amPm = if (isAm) "AM" else "PM"
            val repeat = tvRepeatSummary.text.toString()
            val isVibrate = switchVibration.isChecked

            val isDuplicate = AlarmStorage.isDuplicateTime(
                context = this,
                hour = hour,
                minute = minute,
                amPm = amPm,
                excludeId = if (editAlarmId != -1L) editAlarmId else null
            )

            if (isDuplicate) {
                Toast.makeText(this, "Đã có báo thức vào lúc $time $amPm!", Toast.LENGTH_SHORT).show()
            } else {
                val targetId = if (editAlarmId != -1L) editAlarmId else System.currentTimeMillis()
                val alarm = Alarm(
                    id = targetId,
                    title = label,
                    time = time,
                    hour = hour,
                    minute = minute,
                    amPm = amPm,
                    repeat = repeat,
                    repeatDays = selectedDays.sorted(),
                    isEnabled = true,
                    isVibrate = isVibrate,
                    ringtoneUri = selectedRingtoneUri?.toString()
                )

                if (editAlarmId != -1L || editPosition >= 0) {
                    AlarmStorage.updateAlarm(this, alarm)
                } else {
                    AlarmStorage.addAlarm(this, alarm)
                }

                AlarmScheduler.scheduleAlarm(this, alarm)

                val resultIntent = Intent().apply {
                    putExtra(EXTRA_ALARM_ID, alarm.id)
                    putExtra(EXTRA_POSITION, editPosition)
                    putExtra(EXTRA_TITLE, label)
                    putExtra(EXTRA_TIME, time)
                    putExtra(EXTRA_AM_PM, amPm)
                    putExtra(EXTRA_REPEAT, repeat)
                    putExtra(EXTRA_IS_ENABLED, true)
                    putExtra(EXTRA_IS_VIBRATE, isVibrate)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        findViewById<TextView>(R.id.btnHeaderSave).setOnClickListener { saveListener() }
        findViewById<MaterialButton>(R.id.btnSaveAlarm).setOnClickListener { saveListener() }
    }
}
