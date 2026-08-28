package com.example.alarmclock.ui.worldclock

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.data.WorldClockStorage
import com.example.alarmclock.model.WorldClockCity
import com.example.alarmclock.ui.adapter.WorldClockAdapter
import com.example.alarmclock.util.NavigationHelper
import com.example.alarmclock.util.NavigationTab
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.alarmclock.data.SettingsStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class WorldClockActivity : AppCompatActivity() {

    private lateinit var tvLocalCity: TextView
    private lateinit var tvLocalTime: TextView
    private lateinit var tvLocalAmPm: TextView
    private lateinit var rvWorldClock: RecyclerView
    private lateinit var worldClockAdapter: WorldClockAdapter
    private val cityList = mutableListOf<WorldClockCity>()

    private val handler = Handler(Looper.getMainLooper())
    private val clockRunnable = object : Runnable {
        override fun run() {
            updateLocalTime()
            worldClockAdapter.notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    private val addCityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            refreshCities()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_world_clock)

        NavigationHelper.setupBottomNavigation(this, NavigationTab.WORLD_CLOCK)
        initViews()
        loadCities()
    }

    override fun onResume() {
        super.onResume()
        NavigationHelper.setupBottomNavigation(this, NavigationTab.WORLD_CLOCK)
        refreshCities()
        handler.post(clockRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockRunnable)
    }

    private fun initViews() {
        tvLocalCity = findViewById(R.id.tvLocalCity)
        tvLocalTime = findViewById(R.id.tvLocalTime)
        tvLocalAmPm = findViewById(R.id.tvLocalAmPm)
        rvWorldClock = findViewById(R.id.rvWorldClock)


        val btnMoreHeader = findViewById<ImageView>(R.id.btnMoreHeader)
        btnMoreHeader?.setOnClickListener {
            val intent = Intent(this, com.example.alarmclock.ui.settings.SettingsActivity::class.java)
            startActivity(intent)
        }

        val btnSearchHeader = findViewById<ImageView>(R.id.btnSearchHeader)
        btnSearchHeader.setOnClickListener {
            val intent = Intent(this, AddCityActivity::class.java)
            addCityLauncher.launch(intent)
        }

        // Determine friendly local timezone / city name
        val localTimeZone = TimeZone.getDefault()
        val localTzId = localTimeZone.id
        val displayCity = localTzId.substringAfterLast("/").replace("_", " ")
        tvLocalCity.text = "$displayCity (${localTimeZone.getDisplayName(false, TimeZone.SHORT)})"

        worldClockAdapter = WorldClockAdapter(
            cityList = cityList,
            onItemClick = { _, _ ->
                // Optional click feedback
            },
            onItemLongClick = { position, city ->
                showDeleteDialog(position, city)
            }
        )

        rvWorldClock.layoutManager = LinearLayoutManager(this)
        rvWorldClock.adapter = worldClockAdapter

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddWorldClock)
        fabAdd.setOnClickListener {
            val intent = Intent(this, AddCityActivity::class.java)
            addCityLauncher.launch(intent)
        }
    }

    private fun updateLocalTime() {
        val now = Date()
        val is24h = SettingsStorage.getTimeFormat(this) == SettingsStorage.TIME_FORMAT_24H
        val pattern = if (is24h) "HH:mm" else "hh:mm"
        val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())

        tvLocalTime.text = timeFormat.format(now)
        if (is24h) {
            tvLocalAmPm.text = ""
            tvLocalAmPm.visibility = android.view.View.GONE
        } else {
            val amPmFormat = SimpleDateFormat("a", Locale.US)
            tvLocalAmPm.text = amPmFormat.format(now).uppercase()
            tvLocalAmPm.visibility = android.view.View.VISIBLE
        }
    }

    private fun loadCities() {
        val saved = WorldClockStorage.getSavedCities(this)
        cityList.clear()
        cityList.addAll(saved)
        worldClockAdapter.notifyDataSetChanged()
        updateLocalTime()
    }

    private fun refreshCities() {
        val saved = WorldClockStorage.getSavedCities(this)
        cityList.clear()
        cityList.addAll(saved)
        worldClockAdapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(position: Int, city: WorldClockCity) {
        AlertDialog.Builder(this)
            .setTitle("Xóa thành phố")
            .setMessage("Bạn có muốn xóa ${city.cityName} khỏi danh sách đồng hồ thế giới?")
            .setPositiveButton("Xóa") { _, _ ->
                WorldClockStorage.deleteCity(this, city.id)
                if (position in 0 until cityList.size) {
                    cityList.removeAt(position)
                    worldClockAdapter.notifyItemRemoved(position)
                }
                Toast.makeText(this, "Đã xóa ${city.cityName}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
