package com.example.alarmclock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.WorldClockCity
import com.example.alarmclock.data.SettingsStorage
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

        // Read time format preference
        val is24h = SettingsStorage.getTimeFormat(holder.itemView.context) == SettingsStorage.TIME_FORMAT_24H
        val pattern = if (is24h) "HH:mm" else "hh:mm"

        // Format Time
        val timeFormat = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = targetTimeZone
        }

        holder.tvCityTime.text = timeFormat.format(now)
        if (is24h) {
            holder.tvCityAmPm.text = ""
            holder.tvCityAmPm.visibility = android.view.View.GONE
        } else {
            val cal = java.util.Calendar.getInstance(targetTimeZone)
            val isAm = cal.get(java.util.Calendar.AM_PM) == java.util.Calendar.AM
            holder.tvCityAmPm.text = if (isAm) holder.itemView.context.getString(R.string.time_am) else holder.itemView.context.getString(R.string.time_pm)
            holder.tvCityAmPm.visibility = android.view.View.VISIBLE
        }

        // Calculate offset difference
        val localOffset = localTimeZone.getOffset(now.time)
        val targetOffset = targetTimeZone.getOffset(now.time)
        val diffMillis = targetOffset - localOffset
        val diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis.toLong())
        val diffMinutesRemainder = TimeUnit.MILLISECONDS.toMinutes(diffMillis.toLong()) % 60

        val offsetText = when {
            diffMillis == 0 -> holder.itemView.context.getString(R.string.local_time)
            diffMinutesRemainder != 0L -> {
                val sign = if (diffMillis > 0) "+" else ""
                holder.itemView.context.getString(R.string.hours_minutes_offset, sign, diffHours, Math.abs(diffMinutesRemainder))
            }
            else -> {
                val sign = if (diffHours > 0) "+" else ""
                holder.itemView.context.getString(R.string.hours_offset, sign, diffHours)
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
