package com.example.alarmclock.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object SettingsStorage {

    private const val PREFS_NAME = "alarm_clock_settings_prefs"

    private const val KEY_FIRST_LAUNCH = "key_first_launch"
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_LANGUAGE = "key_language"
    private const val KEY_LANGUAGE_CODE = "key_language_code"
    private const val KEY_CLOCK_TYPE = "key_clock_type"
    private const val KEY_TIME_FORMAT = "key_time_format"
    private const val KEY_GRADUAL_VOLUME = "key_gradual_volume"

    const val THEME_LIGHT = "Light Mode"
    const val THEME_DARK = "Dark Mode"
    const val THEME_SYSTEM = "System Default"

    const val CLOCK_TYPE_ANALOG = "Analog"
    const val CLOCK_TYPE_DIGITAL = "Digital"

    const val TIME_FORMAT_12H = "12 - hours"
    const val TIME_FORMAT_24H = "24 - hours"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- FIRST LAUNCH ---
    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    // --- THEME ---
    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, THEME_LIGHT) ?: THEME_LIGHT
    }

    fun setThemeMode(context: Context, themeMode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, themeMode).apply()
        applyTheme(themeMode)
    }

    fun applyTheme(themeMode: String) {
        when (themeMode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun initAppTheme(context: Context) {
        val savedTheme = getThemeMode(context)
        applyTheme(savedTheme)
        val savedLangCode = getLanguageCode(context)
        applyLanguage(savedLangCode)
    }

    // --- LANGUAGE ---
    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "English (Default)") ?: "English (Default)"
    }

    fun getLanguageCode(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE_CODE, "en") ?: "en"
    }

    fun setLanguage(context: Context, displayName: String, code: String) {
        getPrefs(context).edit()
            .putString(KEY_LANGUAGE, displayName)
            .putString(KEY_LANGUAGE_CODE, code)
            .apply()
        applyLanguage(code)
    }

    fun applyLanguage(code: String) {
        val appLocale = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    // --- CLOCK TYPE ---
    fun getClockType(context: Context): String {
        return getPrefs(context).getString(KEY_CLOCK_TYPE, CLOCK_TYPE_ANALOG) ?: CLOCK_TYPE_ANALOG
    }

    fun setClockType(context: Context, clockType: String) {
        getPrefs(context).edit().putString(KEY_CLOCK_TYPE, clockType).apply()
    }

    // --- TIME FORMAT ---
    fun getTimeFormat(context: Context): String {
        return getPrefs(context).getString(KEY_TIME_FORMAT, TIME_FORMAT_12H) ?: TIME_FORMAT_12H
    }

    fun setTimeFormat(context: Context, timeFormat: String) {
        getPrefs(context).edit().putString(KEY_TIME_FORMAT, timeFormat).apply()
    }

    // --- GRADUAL VOLUME ---
    fun isGradualVolumeEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_GRADUAL_VOLUME, false)
    }

    fun setGradualVolumeEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GRADUAL_VOLUME, enabled).apply()
    }
}
