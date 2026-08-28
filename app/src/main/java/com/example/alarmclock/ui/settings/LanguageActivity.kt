package com.example.alarmclock.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.data.SettingsStorage
import com.example.alarmclock.model.LanguageItem
import com.example.alarmclock.ui.adapter.LanguageAdapter
import com.example.alarmclock.ui.alarm.MainActivity

class LanguageActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FIRST_LAUNCH = "extra_first_launch"
    }

    private lateinit var btnBack: ImageView
    private lateinit var btnConfirm: ImageView
    private lateinit var rvLanguages: RecyclerView
    private lateinit var languageAdapter: LanguageAdapter

    private var isFirstLaunch = false
    private var selectedLanguageItem: LanguageItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        isFirstLaunch = intent.getBooleanExtra(EXTRA_FIRST_LAUNCH, false)

        initViews()
        setupLanguageList()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnConfirm = findViewById(R.id.btnConfirm)
        rvLanguages = findViewById(R.id.rvLanguages)

        if (isFirstLaunch) {
            btnBack.visibility = View.GONE
        } else {
            btnBack.visibility = View.VISIBLE
        }
    }

    private fun setupLanguageList() {
        val currentCode = SettingsStorage.getLanguageCode(this)

        val languages = listOf(
            LanguageItem("en", "English (Default)", R.drawable.ic_flag_en),
            LanguageItem("de", "German", R.drawable.ic_flag_de),
            LanguageItem("fr", "French", R.drawable.ic_flag_fr),
            LanguageItem("es", "Spanish", R.drawable.ic_flag_es),
            LanguageItem("it", "Italian", R.drawable.ic_flag_it),
            LanguageItem("nl", "Dutch", R.drawable.ic_flag_nl),
            LanguageItem("pt", "Portuguese", R.drawable.ic_flag_pt),
            LanguageItem("ar", "Arabic", R.drawable.ic_flag_ar),
            LanguageItem("ko", "Korean", R.drawable.ic_flag_ko),
            LanguageItem("ja", "Japanese", R.drawable.ic_flag_ja),
            LanguageItem("hi", "Hindi", R.drawable.ic_flag_hi),
            LanguageItem("in", "Indonesia", R.drawable.ic_flag_id),
            LanguageItem("vi", "Vietnamese", R.drawable.ic_flag_vi)
        )

        // Select saved language or default to English
        for (lang in languages) {
            lang.isSelected = (lang.code == currentCode)
        }
        if (languages.none { it.isSelected }) {
            languages[0].isSelected = true
        }

        selectedLanguageItem = languages.firstOrNull { it.isSelected } ?: languages[0]

        languageAdapter = LanguageAdapter(languages) { selected ->
            selectedLanguageItem = selected
        }

        rvLanguages.layoutManager = LinearLayoutManager(this)
        rvLanguages.adapter = languageAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnConfirm.setOnClickListener {
            val selected = selectedLanguageItem ?: languageAdapter.getSelectedItem()
            SettingsStorage.setLanguage(this, selected.displayName, selected.code)

            if (isFirstLaunch) {
                SettingsStorage.setFirstLaunchCompleted(this)
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } else {
                // Signal OK to SettingsActivity so it refreshes subtitle
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
