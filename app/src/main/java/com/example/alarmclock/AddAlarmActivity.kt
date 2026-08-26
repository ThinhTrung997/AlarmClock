package com.example.alarmclock

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton

class AddAlarmActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POSITION = "EXTRA_POSITION"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_TIME = "EXTRA_TIME"
        const val EXTRA_AM_PM = "EXTRA_AM_PM"
        const val EXTRA_REPEAT = "EXTRA_REPEAT"
        const val EXTRA_IS_ENABLED = "EXTRA_IS_ENABLED"
    }

    private var editPosition = -1
    private var currentHour = 6     // 1..12
    private var currentMinute = 30  // 0..59
    private var isAm = true
    private val selectedDays = mutableSetOf(0, 1, 2, 3, 4) // Default Mon-Fri
    private val dayButtons = mutableListOf<TextView>()

    private lateinit var tvHourPrev: TextView
    private lateinit var tvHourCurrent: TextView
    private lateinit var tvHourNext: TextView
    private lateinit var tvMinutePrev: TextView
    private lateinit var tvMinuteCurrent: TextView
    private lateinit var tvMinuteNext: TextView
    private lateinit var btnAm: TextView
    private lateinit var btnPm: TextView
    private lateinit var etLabel: EditText
    private lateinit var tvRepeatSummary: TextView
    private lateinit var switchVibration: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_alarm)

        initViews()
        loadDataFromIntent()
        setupHeader()
        setupTimePickerInteractions()
        setupAmPmToggle()
        setupDayToggles()
        setupSaveActions()
    }

    private fun initViews() {
        tvHourPrev = findViewById(R.id.tvHourPrev)
        tvHourCurrent = findViewById(R.id.tvHourCurrent)
        tvHourNext = findViewById(R.id.tvHourNext)
        tvMinutePrev = findViewById(R.id.tvMinutePrev)
        tvMinuteCurrent = findViewById(R.id.tvMinuteCurrent)
        tvMinuteNext = findViewById(R.id.tvMinuteNext)
        btnAm = findViewById(R.id.btnAm)
        btnPm = findViewById(R.id.btnPm)
        etLabel = findViewById(R.id.etLabel)
        tvRepeatSummary = findViewById(R.id.tvRepeatSummary)
        switchVibration = findViewById(R.id.switchVibration)
    }

    private fun loadDataFromIntent() {
        editPosition = intent.getIntExtra(EXTRA_POSITION, -1)

        val tvHeaderTitle = findViewById<TextView>(R.id.tvHeaderTitle)
        if (editPosition >= 0) {
            // Chế độ Sửa (Edit)
            tvHeaderTitle.text = "Edit Alarm"
            val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
            val time = intent.getStringExtra(EXTRA_TIME) ?: "06:30"
            val amPm = intent.getStringExtra(EXTRA_AM_PM) ?: "AM"
            val repeat = intent.getStringExtra(EXTRA_REPEAT) ?: "Everyday"

            etLabel.setText(title)
            isAm = amPm.equals("AM", ignoreCase = true)

            parseTime(time)
            parseRepeat(repeat)
        } else {
            // Chế độ Thêm mới (Add)
            tvHeaderTitle.text = "Add Alarm"
            updateTimeDisplay()
        }
    }

    private fun parseTime(timeStr: String) {
        val parts = timeStr.split(":")
        if (parts.size == 2) {
            currentHour = parts[0].toIntOrNull() ?: 6
            currentMinute = parts[1].toIntOrNull() ?: 30
        }
        updateTimeDisplay()
    }

    private fun parseRepeat(repeatStr: String) {
        selectedDays.clear()
        when {
            repeatStr.equals("Everyday", ignoreCase = true) -> {
                selectedDays.addAll(0..6)
            }
            repeatStr.contains("Mon", ignoreCase = true) || repeatStr.contains("Tue", ignoreCase = true) -> {
                if (repeatStr.contains("Mon")) selectedDays.add(0)
                if (repeatStr.contains("Tue")) selectedDays.add(1)
                if (repeatStr.contains("Wed")) selectedDays.add(2)
                if (repeatStr.contains("Thu")) selectedDays.add(3)
                if (repeatStr.contains("Fri")) selectedDays.add(4)
                if (repeatStr.contains("Sat")) selectedDays.add(5)
                if (repeatStr.contains("Sun")) selectedDays.add(6)
            }
            else -> {
                selectedDays.addAll(listOf(0, 1, 2, 3, 4))
            }
        }
    }

    private fun updateTimeDisplay() {
        tvHourCurrent.text = String.format("%02d", currentHour)
        val prevHour = if (currentHour == 1) 12 else currentHour - 1
        val nextHour = if (currentHour == 12) 1 else currentHour + 1
        tvHourPrev.text = String.format("%02d", prevHour)
        tvHourNext.text = String.format("%02d", nextHour)

        tvMinuteCurrent.text = String.format("%02d", currentMinute)
        val prevMinute = if (currentMinute == 0) 59 else currentMinute - 1
        val nextMinute = if (currentMinute == 59) 0 else currentMinute + 1
        tvMinutePrev.text = String.format("%02d", prevMinute)
        tvMinuteNext.text = String.format("%02d", nextMinute)
    }

    private fun setupHeader() {
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun setupTimePickerInteractions() {
        val openPicker = {
            var hour24 = if (currentHour == 12) 0 else currentHour
            if (!isAm) hour24 += 12

            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour24, selectedMinute ->
                    isAm = selectedHour24 < 12
                    currentHour = when {
                        selectedHour24 == 0 -> 12
                        selectedHour24 > 12 -> selectedHour24 - 12
                        else -> selectedHour24
                    }
                    currentMinute = selectedMinute
                    updateTimeDisplay()
                    updateAmPmUi()
                },
                hour24,
                currentMinute,
                false
            )
            timePickerDialog.show()
        }

        findViewById<LinearLayout>(R.id.layoutTimePicker).setOnClickListener { openPicker() }
        findViewById<LinearLayout>(R.id.boxHours).setOnClickListener { openPicker() }
        findViewById<LinearLayout>(R.id.boxMinutes).setOnClickListener { openPicker() }
    }

    private fun setupAmPmToggle() {
        updateAmPmUi()

        btnAm.setOnClickListener {
            isAm = true
            updateAmPmUi()
        }

        btnPm.setOnClickListener {
            isAm = false
            updateAmPmUi()
        }
    }

    private fun updateAmPmUi() {
        if (isAm) {
            btnAm.setBackgroundResource(R.drawable.bg_ampm_selected)
            btnAm.setTextColor(Color.parseColor("#0D0096"))
            btnPm.setBackgroundColor(Color.TRANSPARENT)
            btnPm.setTextColor(Color.parseColor("#8E8E93"))
        } else {
            btnPm.setBackgroundResource(R.drawable.bg_ampm_selected)
            btnPm.setTextColor(Color.parseColor("#0D0096"))
            btnAm.setBackgroundColor(Color.TRANSPARENT)
            btnAm.setTextColor(Color.parseColor("#8E8E93"))
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
                    selectedDays.remove(index)
                    updateDayButtonUi(textView, false)
                } else {
                    selectedDays.add(index)
                    updateDayButtonUi(textView, true)
                }
                updateRepeatSummary()
            }
        }
        updateRepeatSummary()
    }

    private fun updateDayButtonUi(textView: TextView, isSelected: Boolean) {
        if (isSelected) {
            textView.setBackgroundResource(R.drawable.bg_day_selected)
            textView.setTextColor(Color.parseColor("#0D0096"))
        } else {
            textView.setBackgroundResource(R.drawable.bg_day_unselected)
            textView.setTextColor(Color.parseColor("#8E8E93"))
        }
    }

    private fun updateRepeatSummary() {
        tvRepeatSummary.text = when {
            selectedDays.size == 7 -> "Everyday"
            selectedDays.size == 5 && selectedDays.containsAll(listOf(0, 1, 2, 3, 4)) -> "Mon, Tue, Wed, Thu, Fri"
            selectedDays.size == 2 && selectedDays.containsAll(listOf(5, 6)) -> "Weekends"
            selectedDays.isEmpty() -> "Never"
            else -> {
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                selectedDays.sorted().joinToString(", ") { dayNames[it] }
            }
        }
    }

    private fun setupSaveActions() {
        val btnHeaderSave = findViewById<TextView>(R.id.btnHeaderSave)
        val btnSaveAlarm = findViewById<MaterialButton>(R.id.btnSaveAlarm)

        val saveListener = {
            val label = etLabel.text.toString().trim().ifBlank { "Alarm" }
            val time = String.format("%02d:%02d", currentHour, currentMinute)
            val amPm = if (isAm) "AM" else "PM"
            val repeat = tvRepeatSummary.text.toString()

            val resultIntent = Intent().apply {
                putExtra(EXTRA_POSITION, editPosition)
                putExtra(EXTRA_TITLE, label)
                putExtra(EXTRA_TIME, time)
                putExtra(EXTRA_AM_PM, amPm)
                putExtra(EXTRA_REPEAT, repeat)
                putExtra(EXTRA_IS_ENABLED, true)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        btnHeaderSave.setOnClickListener { saveListener() }
        btnSaveAlarm.setOnClickListener { saveListener() }
    }
}
