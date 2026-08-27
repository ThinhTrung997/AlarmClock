package com.example.alarmclock

import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

data class Alarm(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val time: String,          // e.g. "07:30"
    val hour: Int,             // 1..12
    val minute: Int,           // 0..59
    val amPm: String,          // "AM" or "PM"
    val repeat: String,        // display label
    val repeatDays: List<Int> = listOf(0, 1, 2, 3, 4), // 0=Mon … 6=Sun
    val isEnabled: Boolean = true,
    val isVibrate: Boolean = true,
    val snoozeMinutes: Int = 10,
    val ringtoneUri: String? = null   // null = device default alarm ringtone
) : Serializable {

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
                daysList.addAll(listOf(0, 1, 2, 3, 4))
            }

            val hour = json.optInt("hour", 7)
            val minute = json.optInt("minute", 30)
            val time = json.optString("time", String.format("%02d:%02d", hour, minute))

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
                snoozeMinutes = json.optInt("snoozeMinutes", 10),
                ringtoneUri = json.optString("ringtoneUri", "").ifEmpty { null }
            )
        }
    }
}