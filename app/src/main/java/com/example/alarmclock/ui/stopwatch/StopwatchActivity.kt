package com.example.alarmclock.ui.stopwatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.StopwatchLap
import com.example.alarmclock.ui.adapter.StopwatchLapAdapter
import com.example.alarmclock.util.NavigationHelper
import com.example.alarmclock.util.NavigationTab
import java.util.Locale

class StopwatchActivity : AppCompatActivity() {

    private enum class StopwatchState {
        STOPPED,
        RUNNING,
        PAUSED
    }

    private lateinit var tvMainTime: TextView
    private lateinit var tvMillis: TextView
    private lateinit var btnLapReset: FrameLayout
    private lateinit var tvLapReset: TextView
    private lateinit var btnStartPauseStop: FrameLayout
    private lateinit var ivStartPauseStop: ImageView
    private lateinit var rvLapTimes: RecyclerView
    private lateinit var lapAdapter: StopwatchLapAdapter

    private val lapList = mutableListOf<StopwatchLap>()

    private var currentState = StopwatchState.STOPPED
    private var startTime = 0L
    private var elapsedTime = 0L
    private var lastLapTotalElapsed = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (currentState == StopwatchState.RUNNING) {
                val currentElapsed = SystemClock.elapsedRealtime() - startTime
                updateTimeDisplay(currentElapsed)
                handler.postDelayed(this, 30) // ~30fps for smooth centiseconds
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        NavigationHelper.setupBottomNavigation(this, NavigationTab.STOPWATCH)
        initViews()
        updateUIState()
    }

    override fun onResume() {
        super.onResume()
        NavigationHelper.setupBottomNavigation(this, NavigationTab.STOPWATCH)
    }

    private fun initViews() {
        tvMainTime = findViewById(R.id.tvStopwatchMainTime)
        tvMillis = findViewById(R.id.tvStopwatchMillis)
        btnLapReset = findViewById(R.id.btnLapReset)
        tvLapReset = findViewById(R.id.tvLapReset)
        btnStartPauseStop = findViewById(R.id.btnStartPauseStop)
        ivStartPauseStop = findViewById(R.id.ivStartPauseStop)
        rvLapTimes = findViewById(R.id.rvLapTimes)

        lapAdapter = StopwatchLapAdapter(lapList)
        rvLapTimes.layoutManager = LinearLayoutManager(this)
        rvLapTimes.adapter = lapAdapter

        btnStartPauseStop.setOnClickListener {
            when (currentState) {
                StopwatchState.STOPPED -> startTimer()
                StopwatchState.RUNNING -> pauseTimer()
                StopwatchState.PAUSED -> resumeTimer()
            }
        }

        btnLapReset.setOnClickListener {
            when (currentState) {
                StopwatchState.RUNNING -> recordLap()
                StopwatchState.PAUSED -> resetTimer()
                StopwatchState.STOPPED -> { /* No-op */ }
            }
        }
    }

    private fun startTimer() {
        startTime = SystemClock.elapsedRealtime()
        currentState = StopwatchState.RUNNING
        lastLapTotalElapsed = 0L
        handler.post(timerRunnable)
        updateUIState()
    }

    private fun pauseTimer() {
        elapsedTime = SystemClock.elapsedRealtime() - startTime
        currentState = StopwatchState.PAUSED
        handler.removeCallbacks(timerRunnable)
        updateTimeDisplay(elapsedTime)
        updateUIState()
    }

    private fun resumeTimer() {
        startTime = SystemClock.elapsedRealtime() - elapsedTime
        currentState = StopwatchState.RUNNING
        handler.post(timerRunnable)
        updateUIState()
    }

    private fun resetTimer() {
        handler.removeCallbacks(timerRunnable)
        currentState = StopwatchState.STOPPED
        elapsedTime = 0L
        startTime = 0L
        lastLapTotalElapsed = 0L
        updateTimeDisplay(0L)
        lapList.clear()
        lapAdapter.notifyDataSetChanged()
        updateUIState()
    }

    private fun recordLap() {
        val currentElapsed = SystemClock.elapsedRealtime() - startTime
        val lapTime = currentElapsed - lastLapTotalElapsed
        lastLapTotalElapsed = currentElapsed

        val nextLapNumber = lapList.size + 1
        val lap = StopwatchLap(
            lapNumber = nextLapNumber,
            lapTimeMillis = lapTime,
            totalTimeMillis = currentElapsed
        )
        // Add to top of list as shown in mockups
        lapList.add(0, lap)
        lapAdapter.notifyItemInserted(0)
        rvLapTimes.scrollToPosition(0)
    }

    private fun updateTimeDisplay(millis: Long) {
        val hours = millis / 3_600_000
        val minutes = (millis / 60_000) % 60
        val seconds = (millis / 1_000) % 60
        val centiseconds = (millis % 1_000) / 10

        tvMainTime.text = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        tvMillis.text = String.format(Locale.US, ".%02d", centiseconds)
    }

    private fun updateUIState() {
        when (currentState) {
            StopwatchState.STOPPED -> {
                ivStartPauseStop.setImageResource(R.drawable.ic_play)
                btnLapReset.alpha = 0.4f
                btnLapReset.isEnabled = false
                tvLapReset.text = "Lap"
            }
            StopwatchState.RUNNING -> {
                ivStartPauseStop.setImageResource(R.drawable.ic_stop)
                btnLapReset.alpha = 1.0f
                btnLapReset.isEnabled = true
                tvLapReset.text = "Lap"
            }
            StopwatchState.PAUSED -> {
                ivStartPauseStop.setImageResource(R.drawable.ic_play)
                btnLapReset.alpha = 1.0f
                btnLapReset.isEnabled = true
                tvLapReset.text = "Reset"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }
}
