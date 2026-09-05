package com.example.alarmclock.ui.alarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.data.AlarmStorage
import com.example.alarmclock.model.Alarm
import com.example.alarmclock.ui.adapter.AlarmAdapter
import com.example.alarmclock.util.AlarmScheduler
import com.example.alarmclock.util.NavigationHelper
import com.example.alarmclock.util.NavigationTab
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageView
import com.example.alarmclock.data.SettingsStorage
import com.example.alarmclock.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<Alarm>()

    // Notification permission launcher for Android 13+
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, getString(R.string.notification_permission_rationale), Toast.LENGTH_LONG).show()
        }
    }

    // Launcher nhận kết quả trả về từ AddAlarmActivity
    private val alarmActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshAlarmList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        SettingsStorage.initAppTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NavigationHelper.setupBottomNavigation(this, NavigationTab.ALARM)
        checkPermissions()
        initViews()
        loadAlarms()
    }

    override fun onResume() {
        super.onResume()
        NavigationHelper.setupBottomNavigation(this, NavigationTab.ALARM)
        refreshAlarmList()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewAlarm)

        val iconRight = findViewById<ImageView>(R.id.iconRight)
        iconRight?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        alarmAdapter = AlarmAdapter(
            alarmList = alarmList,
            onItemClick = { position, alarm ->
                // Mở AddAlarmActivity để SỬA
                val intent = Intent(this, AddAlarmActivity::class.java).apply {
                    putExtra(AddAlarmActivity.EXTRA_ALARM_ID, alarm.id)
                    putExtra(AddAlarmActivity.EXTRA_POSITION, position)
                    putExtra(AddAlarmActivity.EXTRA_TITLE, alarm.title)
                    putExtra(AddAlarmActivity.EXTRA_TIME, alarm.time)
                    putExtra(AddAlarmActivity.EXTRA_AM_PM, alarm.amPm)
                    putExtra(AddAlarmActivity.EXTRA_REPEAT, alarm.repeat)
                    putExtra(AddAlarmActivity.EXTRA_IS_ENABLED, alarm.isEnabled)
                    putExtra(AddAlarmActivity.EXTRA_IS_VIBRATE, alarm.isVibrate)
                }
                alarmActivityResultLauncher.launch(intent)
            },
            onAlarmToggle = { _, _, updatedAlarm ->
                // Lưu vào SharedPreferences và cập nhật AlarmManager
                AlarmStorage.updateAlarm(this, updatedAlarm)
                AlarmScheduler.scheduleAlarm(this, updatedAlarm)
            },
            onAlarmLongClick = { position, alarm ->
                // Hiển thị dialog xác nhận xóa báo thức
                showDeleteAlarmDialog(position, alarm)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = alarmAdapter

        // Bấm nút (+) FloatingActionButton -> Mở AddAlarmActivity để THÊM MỚI
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java).apply {
                putExtra(AddAlarmActivity.EXTRA_ALARM_ID, -1L)
                putExtra(AddAlarmActivity.EXTRA_POSITION, -1)
            }
            alarmActivityResultLauncher.launch(intent)
        }
    }

    private fun loadAlarms() {
        val storedAlarms = AlarmStorage.getAlarms(this)
        alarmList.clear()
        alarmList.addAll(storedAlarms)
        alarmAdapter.notifyDataSetChanged()

        // Reschedule active alarms
        AlarmScheduler.rescheduleAllActiveAlarms(this)
    }

    private fun refreshAlarmList() {
        val storedAlarms = AlarmStorage.getAlarms(this)
        alarmList.clear()
        alarmList.addAll(storedAlarms)
        alarmAdapter.notifyDataSetChanged()
    }

    private fun showDeleteAlarmDialog(position: Int, alarm: Alarm) {
        val displayTitle = if (alarm.title.isBlank() || alarm.title.equals("Alarm", true)) {
            getString(R.string.tab_alarm)
        } else {
            alarm.title
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_alarm)
            .setMessage(getString(R.string.delete_confirm, displayTitle, alarm.time, alarm.getFormattedAmPm(this)))
            .setPositiveButton(R.string.delete) { _, _ ->
                AlarmStorage.deleteAlarm(this, alarm.id)
                AlarmScheduler.cancelAlarm(this, alarm.id)
                if (position in 0 until alarmList.size) {
                    alarmList.removeAt(position)
                    alarmAdapter.notifyItemRemoved(position)
                }
                Toast.makeText(this, getString(R.string.alarm_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
