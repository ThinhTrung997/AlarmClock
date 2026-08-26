package com.example.alarmclock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<Alarm>()

    // Launcher nhận kết quả trả về từ AddAlarmActivity (Thêm mới hoặc Sửa)
    private val alarmActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val position = data.getIntExtra(AddAlarmActivity.EXTRA_POSITION, -1)
            val title = data.getStringExtra(AddAlarmActivity.EXTRA_TITLE) ?: "Alarm"
            val time = data.getStringExtra(AddAlarmActivity.EXTRA_TIME) ?: "06:30"
            val amPm = data.getStringExtra(AddAlarmActivity.EXTRA_AM_PM) ?: "AM"
            val repeat = data.getStringExtra(AddAlarmActivity.EXTRA_REPEAT) ?: "Everyday"
            val isEnabled = data.getBooleanExtra(AddAlarmActivity.EXTRA_IS_ENABLED, true)

            val updatedAlarm = Alarm(
                title = title,
                time = time,
                amPm = amPm,
                repeat = repeat,
                isEnabled = isEnabled
            )

            if (position >= 0 && position < alarmList.size) {
                // 1. Chế độ SỬA: Cập nhật item tại vị trí được chọn
                alarmList[position] = updatedAlarm
                alarmAdapter.notifyItemChanged(position)
            } else {
                // 2. Chế độ THÊM MỚI: Thêm vào danh sách và cuộn đến cuối
                alarmList.add(updatedAlarm)
                alarmAdapter.notifyItemInserted(alarmList.size - 1)
                recyclerView.smoothScrollToPosition(alarmList.size - 1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewAlarm)

        // Dữ liệu mẫu ban đầu
        if (alarmList.isEmpty()) {
            alarmList.addAll(
                listOf(
                    Alarm(
                        title = "Morning Workout",
                        time = "07:30",
                        amPm = "AM",
                        repeat = "Everyday",
                        isEnabled = true
                    ),
                    Alarm(
                        title = "Office Meeting",
                        time = "12:30",
                        amPm = "PM",
                        repeat = "Mon, Tue, Wed, Thu, Fri",
                        isEnabled = false
                    ),
                    Alarm(
                        title = "Sleep Time",
                        time = "02:00",
                        amPm = "PM",
                        repeat = "Mon, Tue, Wed, Thu, Fri",
                        isEnabled = false
                    )
                )
            )
        }

        // Khởi tạo Adapter với 2 callback: Sửa (onItemClick) và Bật/Tắt switch (onAlarmToggle)
        alarmAdapter = AlarmAdapter(
            alarmList = alarmList,
            onItemClick = { position, alarm ->
                // 👉 Bấm vào một thời gian bất kỳ -> Mở AddAlarmActivity để SỬA
                val intent = Intent(this, AddAlarmActivity::class.java).apply {
                    putExtra(AddAlarmActivity.EXTRA_POSITION, position)
                    putExtra(AddAlarmActivity.EXTRA_TITLE, alarm.title)
                    putExtra(AddAlarmActivity.EXTRA_TIME, alarm.time)
                    putExtra(AddAlarmActivity.EXTRA_AM_PM, alarm.amPm)
                    putExtra(AddAlarmActivity.EXTRA_REPEAT, alarm.repeat)
                    putExtra(AddAlarmActivity.EXTRA_IS_ENABLED, alarm.isEnabled)
                }
                alarmActivityResultLauncher.launch(intent)
            },
            onAlarmToggle = { position, isEnabled ->
                // Xử lý khi người dùng gạt switch
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = alarmAdapter

        // 👉 Bấm nút (+) FloatingActionButton -> Mở AddAlarmActivity để THÊM MỚI
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddAlarmActivity::class.java).apply {
                putExtra(AddAlarmActivity.EXTRA_POSITION, -1) // -1 nghĩa là thêm mới
            }
            alarmActivityResultLauncher.launch(intent)
        }
    }
}