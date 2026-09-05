package com.example.alarmclock.ui.stopwatch

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.StopwatchLap
import com.example.alarmclock.ui.adapter.StopwatchLapAdapter
import com.example.alarmclock.util.NavigationHelper
import com.example.alarmclock.util.NavigationTab
import java.util.Locale

class StopwatchActivity : AppCompatActivity() {

    private lateinit var tvMainTime: TextView
    private lateinit var tvMillis: TextView
    private lateinit var btnLapReset: FrameLayout
    private lateinit var tvLapReset: TextView
    private lateinit var btnStartPauseStop: FrameLayout
    private lateinit var ivStartPauseStop: ImageView
    private lateinit var rvLapTimes: RecyclerView
    private lateinit var lapAdapter: StopwatchLapAdapter

    private val lapList = mutableListOf<StopwatchLap>()

    private var stopwatchService: StopwatchService? = null
    private var isBound = false

    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            val service = stopwatchService
            if (service != null && service.currentState == StopwatchService.StopwatchState.RUNNING) {
                updateTimeDisplay(service.getCurrentElapsed())
                handler.postDelayed(this, 30) // ~30fps for smooth centiseconds
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as StopwatchService.LocalBinder
            stopwatchService = localBinder.getService()
            isBound = true
            syncWithService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            stopwatchService = null
            isBound = false
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == StopwatchService.ACTION_STATE_CHANGED) {
                syncWithService()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stopwatch)

        NavigationHelper.setupBottomNavigation(this, NavigationTab.STOPWATCH)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, StopwatchService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        val filter = IntentFilter(StopwatchService.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stateReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        try {
            unregisterReceiver(stateReceiver)
        } catch (_: Exception) {}
        handler.removeCallbacks(timerRunnable)
    }

    override fun onResume() {
        super.onResume()
        NavigationHelper.setupBottomNavigation(this, NavigationTab.STOPWATCH)
        syncWithService()
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
            val service = stopwatchService ?: return@setOnClickListener
            when (service.currentState) {
                StopwatchService.StopwatchState.STOPPED -> startTimer()
                StopwatchService.StopwatchState.RUNNING -> pauseTimer()
                StopwatchService.StopwatchState.PAUSED -> resumeTimer()
            }
        }

        btnLapReset.setOnClickListener {
            val service = stopwatchService ?: return@setOnClickListener
            when (service.currentState) {
                StopwatchService.StopwatchState.RUNNING -> recordLap()
                StopwatchService.StopwatchState.PAUSED -> resetTimer()
                StopwatchService.StopwatchState.STOPPED -> { /* No-op */ }
            }
        }
    }

    private fun syncWithService() {
        val service = stopwatchService ?: return
        lapList.clear()
        lapList.addAll(service.lapList)
        lapAdapter.notifyDataSetChanged()

        updateTimeDisplay(service.getCurrentElapsed())
        updateUIState(service.currentState)

        handler.removeCallbacks(timerRunnable)
        if (service.currentState == StopwatchService.StopwatchState.RUNNING) {
            handler.post(timerRunnable)
        }
    }

    private fun startTimer() {
        val intent = Intent(this, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_START
        }
        ContextCompat.startForegroundService(this, intent)
        handler.post(timerRunnable)
    }

    private fun pauseTimer() {
        val intent = Intent(this, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_PAUSE
        }
        startService(intent)
        handler.removeCallbacks(timerRunnable)
    }

    private fun resumeTimer() {
        val intent = Intent(this, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_RESUME
        }
        startService(intent)
        handler.post(timerRunnable)
    }

    private fun resetTimer() {
        handler.removeCallbacks(timerRunnable)
        val intent = Intent(this, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_STOP
        }
        startService(intent)
    }

    private fun recordLap() {
        val service = stopwatchService ?: return
        val lap = service.recordLap()
        if (lap != null) {
            lapList.add(0, lap)
            lapAdapter.notifyItemInserted(0)
            rvLapTimes.scrollToPosition(0)
        }
    }

    private fun updateTimeDisplay(millis: Long) {
        val hours = millis / 3_600_000
        val minutes = (millis / 60_000) % 60
        val seconds = (millis / 1_000) % 60
        val centiseconds = (millis % 1_000) / 10

        tvMainTime.text = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        tvMillis.text = String.format(Locale.US, ".%02d", centiseconds)
    }

    private fun updateUIState(state: StopwatchService.StopwatchState) {
        when (state) {
            StopwatchService.StopwatchState.STOPPED -> {
                ivStartPauseStop.setImageResource(R.drawable.ic_play)
                btnLapReset.alpha = 0.4f
                btnLapReset.isEnabled = false
                tvLapReset.text = getString(R.string.lap)
            }
            StopwatchService.StopwatchState.RUNNING -> {
                ivStartPauseStop.setImageResource(R.drawable.ic_stop)
                btnLapReset.alpha = 1.0f
                btnLapReset.isEnabled = true
                tvLapReset.text = getString(R.string.lap)
            }
            StopwatchService.StopwatchState.PAUSED -> {
                ivStartPauseStop.setImageResource(R.drawable.ic_play)
                btnLapReset.alpha = 1.0f
                btnLapReset.isEnabled = true
                tvLapReset.text = getString(R.string.reset)
            }
        }
    }
}

