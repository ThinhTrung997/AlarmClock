package com.example.alarmclock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.Alarm

class AlarmAdapter(
    private val alarmList: MutableList<Alarm>,
    private val onItemClick: ((position: Int, alarm: Alarm) -> Unit)? = null,
    private val onAlarmToggle: ((position: Int, isEnabled: Boolean, alarm: Alarm) -> Unit)? = null,
    private val onAlarmLongClick: ((position: Int, alarm: Alarm) -> Unit)? = null
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvAmPm: TextView = itemView.findViewById(R.id.tvAmPm)
        val tvRepeat: TextView = itemView.findViewById(R.id.tvRepeat)
        val switchAlarm: SwitchCompat = itemView.findViewById(R.id.switchAlarm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarmList[position]

        holder.tvTitle.text = alarm.title
        holder.tvTime.text = alarm.time
        holder.tvAmPm.text = alarm.amPm
        holder.tvRepeat.text = alarm.repeat

        updateCardAppearance(holder, alarm.isEnabled)

        // Click on card to Edit
        holder.itemView.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < alarmList.size) {
                onItemClick?.invoke(currentPos, alarmList[currentPos])
            }
        }

        // Long click on card to Delete
        holder.itemView.setOnLongClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < alarmList.size) {
                onAlarmLongClick?.invoke(currentPos, alarmList[currentPos])
                true
            } else {
                false
            }
        }

        // Toggle Switch
        holder.switchAlarm.setOnCheckedChangeListener(null)
        holder.switchAlarm.isChecked = alarm.isEnabled

        holder.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION && currentPos < alarmList.size) {
                val updatedAlarm = alarmList[currentPos].copy(isEnabled = isChecked)
                alarmList[currentPos] = updatedAlarm
                updateCardAppearance(holder, isChecked)
                onAlarmToggle?.invoke(currentPos, isChecked, updatedAlarm)
            }
        }
    }

    private fun updateCardAppearance(holder: AlarmViewHolder, isEnabled: Boolean) {
        val context = holder.itemView.context
        if (isEnabled) {
            val colorPrimary = ContextCompat.getColor(context, R.color.text_primary)
            val colorSecondary = ContextCompat.getColor(context, R.color.text_secondary)
            val colorAccent = ContextCompat.getColor(context, R.color.accent_blue)

            holder.tvTime.setTextColor(colorPrimary)
            holder.tvAmPm.setTextColor(colorPrimary)
            holder.tvTitle.setTextColor(colorSecondary)
            holder.tvRepeat.setTextColor(colorAccent)
            holder.itemView.alpha = 1.0f
        } else {
            val colorDisabled = ContextCompat.getColor(context, R.color.text_disabled)

            holder.tvTime.setTextColor(colorDisabled)
            holder.tvAmPm.setTextColor(colorDisabled)
            holder.tvTitle.setTextColor(colorDisabled)
            holder.tvRepeat.setTextColor(colorDisabled)
            holder.itemView.alpha = 0.6f
        }
    }

    override fun getItemCount(): Int = alarmList.size
}
