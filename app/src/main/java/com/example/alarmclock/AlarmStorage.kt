package com.example.alarmclock

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

object AlarmStorage {

    private const val PREFS_NAME = "alarm_clock_prefs"
    private const val KEY_ALARMS = "key_alarms_list"
    private const val KEY_INITIALIZED = "key_initialized_defaults"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAlarms(context: Context): MutableList<Alarm> {
        val prefs = getPrefs(context)
        val jsonString = prefs.getString(KEY_ALARMS, null)

        if (jsonString.isNullOrEmpty()) {
            if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
                // Initialize default sample alarms only once
                val defaults = listOf(
                    Alarm(
                        id = 1001L,
                        title = "Morning Workout",
                        time = "07:30",
                        hour = 7,
                        minute = 30,
                        amPm = "AM",
                        repeat = "Everyday",
                        repeatDays = listOf(0, 1, 2, 3, 4, 5, 6),
                        isEnabled = true
                    ),
                    Alarm(
                        id = 1002L,
                        title = "Office Meeting",
                        time = "12:30",
                        hour = 12,
                        minute = 30,
                        amPm = "PM",
                        repeat = "Mon, Tue, Wed, Thu, Fri",
                        repeatDays = listOf(0, 1, 2, 3, 4),
                        isEnabled = false
                    ),
                    Alarm(
                        id = 1003L,
                        title = "Sleep Time",
                        time = "02:00",
                        hour = 2,
                        minute = 0,
                        amPm = "PM",
                        repeat = "Mon, Tue, Wed, Thu, Fri",
                        repeatDays = listOf(0, 1, 2, 3, 4),
                        isEnabled = false
                    )
                )
                saveAlarms(context, defaults)
                prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
                return defaults.toMutableList()
            }
            return mutableListOf()
        }

        val result = mutableListOf<Alarm>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(Alarm.fromJson(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    fun saveAlarms(context: Context, alarms: List<Alarm>) {
        val jsonArray = JSONArray()
        for (alarm in alarms) {
            jsonArray.put(alarm.toJson())
        }
        getPrefs(context).edit()
            .putString(KEY_ALARMS, jsonArray.toString())
            .apply()
    }

    fun addAlarm(context: Context, alarm: Alarm) {
        val list = getAlarms(context)
        list.add(alarm)
        saveAlarms(context, list)
    }

    fun updateAlarm(context: Context, alarm: Alarm) {
        val list = getAlarms(context)
        val index = list.indexOfFirst { it.id == alarm.id }
        if (index != -1) {
            list[index] = alarm
            saveAlarms(context, list)
        }
    }

    fun deleteAlarm(context: Context, alarmId: Long) {
        val list = getAlarms(context)
        val removed = list.removeAll { it.id == alarmId }
        if (removed) {
            saveAlarms(context, list)
        }
    }

    fun isDuplicateTime(context: Context, hour: Int, minute: Int, amPm: String, excludeId: Long? = null): Boolean {
        val list = getAlarms(context)
        return list.any { it.id != excludeId && it.hour == hour && it.minute == minute && it.amPm.equals(amPm, ignoreCase = true) }
    }
}
