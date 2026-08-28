package com.example.alarmclock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.WorldClockCity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class WorldClockAdapter(
    private val cityList: MutableList<WorldClockCity>,
    private val onItemClick: ((position: Int, city: WorldClockCity) -> Unit)? = null,
    private val onItemLongClick: ((position: Int, city: WorldClockCity) -> Unit)? = null
) : RecyclerView.Adapter<WorldClockAdapter.WorldClockViewHolder>() {

    class WorldClockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCityName: TextView = itemView.findViewById(R.id.tvCityName)
        val tvCountryName: TextView = itemView.findViewById(R.id.tvCountryName)
        val tvTimeOffset: TextView = itemView.findViewById(R.id.tvTimeOffset)
        val tvCityTime: TextView = itemView.findViewById(R.id.tvCityTime)
        val tvCityAmPm: TextView = itemView.findViewById(R.id.tvCityAmPm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorldClockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_world_clock, parent, false)
        return WorldClockViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorldClockViewHolder, position: Int) {
        val city = cityList[position]
        holder.tvCityName.text = city.cityName
        holder.tvCountryName.text = city.countryName

        val targetTimeZone = TimeZone.getTimeZone(city.timeZoneId)
        val localTimeZone = TimeZone.getDefault()
        val now = Date()

        // Format Time
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault()).apply {
            timeZone = targetTimeZone
        }
        val amPmFormat = SimpleDateFormat("a", Locale.US).apply {
            timeZone = targetTimeZone
        }

        holder.tvCityTime.text = timeFormat.format(now)
        holder.tvCityAmPm.text = amPmFormat.format(now).uppercase()

        // Calculate offset difference
        val localOffset = localTimeZone.getOffset(now.time)
        val targetOffset = targetTimeZone.getOffset(now.time)
        val diffMillis = targetOffset - localOffset
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis.toLong())
        val diffMinutesRemainder = TimeUnit.MILLISECONDS.toMinutes(diffMillis.toLong()) % 60

        val offsetText = when {
            diffMillis == 0 -> "Local time"
            diffMinutesRemainder != 0L -> {
                val sign = if (diffMillis > 0) "+" else ""
                String.format(Locale.getDefault(), "%s%d:%02d hrs", sign, diffHours, Math.abs(diffMinutesRemainder))
            }
            else -> {
                val sign = if (diffHours > 0) "+" else ""
                "$sign$diffHours hrs"
            }
        }
        holder.tvTimeOffset.text = offsetText

        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemClick?.invoke(pos, city)
            }
        }

        holder.itemView.setOnLongClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onItemLongClick?.invoke(pos, city)
            }
            true
        }
    }

    override fun getItemCount(): Int = cityList.size

    fun updateData(newCities: List<WorldClockCity>) {
        cityList.clear()
        cityList.addAll(newCities)
        notifyDataSetChanged()
    }
}
