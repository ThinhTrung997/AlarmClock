package com.example.alarmclock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val alarmList: MutableList<Alarm>,
    private val onAlarmToggle: ((position: Int, isEnabled: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvAmPm: TextView = itemView.findViewById(R.id.tvAmPm)
        val tvRepeat: TextView = itemView.findViewById(R.id.tvRepeat)
        val switchAlarm: Switch = itemView.findViewById(R.id.switchAlarm)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlarmViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)

        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: AlarmViewHolder,
        position: Int
    ) {

        val alarm = alarmList[position]

        holder.tvTitle.text = alarm.title
        holder.tvTime.text = alarm.time
        holder.tvAmPm.text = alarm.amPm
        holder.tvRepeat.text = alarm.repeat

        // Remove listener before setting isChecked to avoid triggering unwanted callbacks
        holder.switchAlarm.setOnCheckedChangeListener(null)
        holder.switchAlarm.isChecked = alarm.isEnabled

        holder.switchAlarm.setOnCheckedChangeListener { _, isChecked ->
            alarmList[position] = alarm.copy(isEnabled = isChecked)
            onAlarmToggle?.invoke(position, isChecked)
        }
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }
}