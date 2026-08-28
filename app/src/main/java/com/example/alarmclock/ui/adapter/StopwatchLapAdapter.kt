package com.example.alarmclock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.StopwatchLap
import java.util.Locale

class StopwatchLapAdapter(
    private val lapList: MutableList<StopwatchLap>
) : RecyclerView.Adapter<StopwatchLapAdapter.LapViewHolder>() {

    class LapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLapNumber: TextView = itemView.findViewById(R.id.tvLapNumber)
        val tvLapTime: TextView = itemView.findViewById(R.id.tvLapTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stopwatch_lap, parent, false)
        return LapViewHolder(view)
    }

    override fun onBindViewHolder(holder: LapViewHolder, position: Int) {
        val lap = lapList[position]
        holder.tvLapNumber.text = "Lap ${lap.lapNumber}"
        holder.tvLapTime.text = formatLapTime(lap.lapTimeMillis)
    }

    override fun getItemCount(): Int = lapList.size

    private fun formatLapTime(millis: Long): String {
        val minutes = (millis / 60_000) % 60
        val seconds = (millis / 1_000) % 60
        val centiseconds = (millis % 1_000) / 10
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centiseconds)
    }
}
