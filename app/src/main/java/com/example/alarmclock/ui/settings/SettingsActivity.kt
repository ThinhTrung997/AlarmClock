package com.example.alarmclock.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.alarmclock.R
import com.example.alarmclock.data.SettingsStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvLanguageSubtitle: TextView
    private lateinit var tvThemeModeSubtitle: TextView
    private lateinit var switchGradualVolume: SwitchCompat

    private lateinit var itemLanguage: LinearLayout
    private lateinit var itemThemeMode: LinearLayout
    private lateinit var itemGradualVolume: LinearLayout

    private val languageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            tvLanguageSubtitle.text = SettingsStorage.getLanguage(this)
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        loadCurrentSettings()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadCurrentSettings()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvLanguageSubtitle = findViewById(R.id.tvLanguageSubtitle)
        tvThemeModeSubtitle = findViewById(R.id.tvThemeModeSubtitle)
        switchGradualVolume = findViewById(R.id.switchGradualVolume)

        itemLanguage = findViewById(R.id.itemLanguage)
        itemThemeMode = findViewById(R.id.itemThemeMode)
        itemGradualVolume = findViewById(R.id.itemGradualVolume)
    }

    private fun loadCurrentSettings() {
        tvLanguageSubtitle.text = SettingsStorage.getLanguage(this)
        tvThemeModeSubtitle.text = SettingsStorage.getThemeMode(this)
        switchGradualVolume.isChecked = SettingsStorage.isGradualVolumeEnabled(this)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        itemLanguage.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            languageLauncher.launch(intent)
        }

        itemThemeMode.setOnClickListener {
            showThemeModeDialog()
        }

        switchGradualVolume.setOnCheckedChangeListener { _, isChecked ->
            SettingsStorage.setGradualVolumeEnabled(this, isChecked)
            val msg = if (isChecked) getString(R.string.gradual_volume_enabled) else getString(R.string.gradual_volume_disabled)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        itemGradualVolume.setOnClickListener {
            switchGradualVolume.toggle()
        }
    }

    // --- DIALOGS ---

    private fun showThemeModeDialog() {
        val themeOptions = arrayOf(
            SettingsStorage.THEME_LIGHT,
            SettingsStorage.THEME_DARK,
            SettingsStorage.THEME_SYSTEM
        )
        val currentTheme = SettingsStorage.getThemeMode(this)
        val selectedIndex = themeOptions.indexOf(currentTheme).let { if (it >= 0) it else 0 }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.setting_theme_mode)
            .setSingleChoiceItems(themeOptions, selectedIndex) { dialog, which ->
                val chosen = themeOptions[which]
                SettingsStorage.setThemeMode(this, chosen)
                tvThemeModeSubtitle.text = chosen
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

}
