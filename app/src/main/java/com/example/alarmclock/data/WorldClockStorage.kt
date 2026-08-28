package com.example.alarmclock.data

import android.content.Context
import com.example.alarmclock.model.WorldClockCity
import org.json.JSONArray
import org.json.JSONObject

object WorldClockStorage {
    private const val PREFS_NAME = "world_clock_prefs"
    private const val KEY_CITIES = "saved_cities"

    // Default cities matching the mockups
    val DEFAULT_CITIES = listOf(
        WorldClockCity("tokyo", "Tokyo", "Japan", "Asia/Tokyo"),
        WorldClockCity("london", "London", "United Kingdom", "Europe/London"),
        WorldClockCity("new_york", "New York", "United States", "America/New_York")
    )

    // Comprehensive list of worldwide cities for the Add City screen
    val ALL_AVAILABLE_CITIES = listOf(
        WorldClockCity("tokyo", "Tokyo", "Japan", "Asia/Tokyo"),
        WorldClockCity("london", "London", "United Kingdom", "Europe/London"),
        WorldClockCity("new_york", "New York", "USA", "America/New_York"),
        WorldClockCity("paris", "Paris", "France", "Europe/Paris"),
        WorldClockCity("seoul", "Seoul", "South Korea", "Asia/Seoul"),
        WorldClockCity("singapore", "Singapore", "Singapore", "Asia/Singapore"),
        WorldClockCity("sydney", "Sydney", "Australia", "Australia/Sydney"),
        WorldClockCity("dubai", "Dubai", "United Arab Emirates", "Asia/Dubai"),
        WorldClockCity("bangkok", "Bangkok", "Thailand", "Asia/Bangkok"),
        WorldClockCity("hanoi", "Hanoi", "Vietnam", "Asia/Ho_Chi_Minh"),
        WorldClockCity("ho_chi_minh", "Ho Chi Minh City", "Vietnam", "Asia/Ho_Chi_Minh"),
        WorldClockCity("berlin", "Berlin", "Germany", "Europe/Berlin"),
        WorldClockCity("rome", "Rome", "Italy", "Europe/Rome"),
        WorldClockCity("toronto", "Toronto", "Canada", "America/Toronto"),
        WorldClockCity("los_angeles", "Los Angeles", "USA", "America/Los_Angeles"),
        WorldClockCity("san_francisco", "San Francisco", "USA", "America/Los_Angeles"),
        WorldClockCity("chicago", "Chicago", "USA", "America/Chicago"),
        WorldClockCity("hong_kong", "Hong Kong", "China", "Asia/Hong_Kong"),
        WorldClockCity("shanghai", "Shanghai", "China", "Asia/Shanghai"),
        WorldClockCity("beijing", "Beijing", "China", "Asia/Shanghai"),
        WorldClockCity("mumbai", "Mumbai", "India", "Asia/Kolkata"),
        WorldClockCity("cairo", "Cairo", "Egypt", "Africa/Cairo"),
        WorldClockCity("istanbul", "Istanbul", "Turkey", "Europe/Istanbul"),
        WorldClockCity("sao_paulo", "São Paulo", "Brazil", "America/Sao_Paulo"),
        WorldClockCity("buenos_aires", "Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires"),
        WorldClockCity("auckland", "Auckland", "New Zealand", "Pacific/Auckland"),
        WorldClockCity("amsterdam", "Amsterdam", "Netherlands", "Europe/Amsterdam"),
        WorldClockCity("moscow", "Moscow", "Russia", "Europe/Moscow"),
        WorldClockCity("zurich", "Zurich", "Switzerland", "Europe/Zurich")
    )

    fun getSavedCities(context: Context): MutableList<WorldClockCity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_CITIES, null)

        if (jsonString.isNullOrEmpty()) {
            val defaultList = DEFAULT_CITIES.toMutableList()
            saveCities(context, defaultList)
            return defaultList
        }

        val result = mutableListOf<WorldClockCity>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                result.add(
                    WorldClockCity(
                        id = obj.getString("id"),
                        cityName = obj.getString("cityName"),
                        countryName = obj.getString("countryName"),
                        timeZoneId = obj.getString("timeZoneId")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return DEFAULT_CITIES.toMutableList()
        }
        return result
    }

    fun saveCities(context: Context, cities: List<WorldClockCity>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (city in cities) {
            val obj = JSONObject().apply {
                put("id", city.id)
                put("cityName", city.cityName)
                put("countryName", city.countryName)
                put("timeZoneId", city.timeZoneId)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CITIES, jsonArray.toString()).apply()
    }

    fun addCity(context: Context, city: WorldClockCity): Boolean {
        val list = getSavedCities(context)
        if (list.none { it.id == city.id || (it.cityName.equals(city.cityName, ignoreCase = true) && it.timeZoneId == city.timeZoneId) }) {
            list.add(city)
            saveCities(context, list)
            return true
        }
        return false
    }

    fun deleteCity(context: Context, cityId: String) {
        val list = getSavedCities(context)
        val updated = list.filterNot { it.id == cityId }
        saveCities(context, updated)
    }
}
