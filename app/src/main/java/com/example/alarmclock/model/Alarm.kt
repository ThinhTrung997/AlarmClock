package com.example.alarmclock.model

import android.content.Context
import com.example.alarmclock.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Alarm(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val time: String,          // e.g. "07:30"
    val hour: Int,             // 1..12
    val minute: Int,           // 0..59
    val amPm: String,          // "AM" or "PM"
    val repeat: String,        // display label
    val repeatDays: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6), // 0=Mon … 6=Sun
    val isEnabled: Boolean = true,
    val isVibrate: Boolean = true,
    val snoozeMinutes: Int = 5,
    val ringtoneUri: String? = null,   // null = device default alarm ringtone
    val dateMillis: Long? = null
) : Serializable {

    fun getFormattedAmPm(context: Context): String {
        val isAm = amPm.equals("AM", ignoreCase = true) || amPm.equals("SA", ignoreCase = true)
        return if (isAm) context.getString(R.string.time_am) else context.getString(R.string.time_pm)
    }

    fun getFormattedRepeat(context: Context): String {
        if (dateMillis != null && dateMillis > 0) {
            val fmt = SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault())
            return fmt.format(Date(dateMillis))
        }
        val dayNames = listOf(
            context.getString(R.string.day_mon),
            context.getString(R.string.day_tue),
            context.getString(R.string.day_wed),
            context.getString(R.string.day_thu),
            context.getString(R.string.day_fri),
            context.getString(R.string.day_sat),
            context.getString(R.string.day_sun)
        )
        return when {
            repeatDays.size == 7 -> context.getString(R.string.repeat_everyday)
            repeatDays.size == 5 && repeatDays.containsAll(listOf(0, 1, 2, 3, 4)) -> dayNames.subList(0, 5).joinToString(", ")
            repeatDays.size == 2 && repeatDays.containsAll(listOf(5, 6)) -> context.getString(R.string.repeat_weekends)
            repeatDays.isEmpty() -> context.getString(R.string.repeat_never)
            else -> repeatDays.sorted().joinToString(", ") { dayNames[it] }
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("title", title)
        json.put("time", time)
        json.put("hour", hour)
        json.put("minute", minute)
        json.put("amPm", amPm)
        json.put("repeat", repeat)
        val daysArray = JSONArray()
        repeatDays.forEach { daysArray.put(it) }
        json.put("repeatDays", daysArray)
        json.put("isEnabled", isEnabled)
        json.put("isVibrate", isVibrate)
        json.put("snoozeMinutes", snoozeMinutes)
        if (!ringtoneUri.isNullOrEmpty()) json.put("ringtoneUri", ringtoneUri)
        if (dateMillis != null && dateMillis > 0) json.put("dateMillis", dateMillis)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): Alarm {
            val daysList = mutableListOf<Int>()
            val daysArray = json.optJSONArray("repeatDays")
            if (daysArray != null) {
                for (i in 0 until daysArray.length()) {
                    daysList.add(daysArray.getInt(i))
                }
            } else {
                daysList.addAll(listOf(0, 1, 2, 3, 4, 5, 6))
            }

            val hour = json.optInt("hour", 7)
            val minute = json.optInt("minute", 30)
            val time = json.optString("time", String.format("%02d:%02d", hour, minute))
            val dateMillis = if (json.has("dateMillis")) json.optLong("dateMillis") else null

            return Alarm(
                id = json.optLong("id", System.currentTimeMillis()),
                title = json.optString("title", "Alarm"),
                time = time,
                hour = hour,
                minute = minute,
                amPm = json.optString("amPm", "AM"),
                repeat = json.optString("repeat", "Everyday"),
                repeatDays = daysList,
                isEnabled = json.optBoolean("isEnabled", true),
                isVibrate = json.optBoolean("isVibrate", true),
                snoozeMinutes = json.optInt("snoozeMinutes", 5),
                ringtoneUri = json.optString("ringtoneUri", "").ifEmpty { null },
                dateMillis = dateMillis
            )
        }
    }
}

