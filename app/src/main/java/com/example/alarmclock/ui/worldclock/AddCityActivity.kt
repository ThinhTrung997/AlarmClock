package com.example.alarmclock.ui.worldclock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.data.WorldClockStorage
import com.example.alarmclock.ui.adapter.AddCityAdapter

class AddCityActivity : AppCompatActivity() {

    private lateinit var etSearchCity: EditText
    private lateinit var rvAddCity: RecyclerView
    private lateinit var addCityAdapter: AddCityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_city)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        val btnBack = findViewById<ImageButton>(R.id.btnBackAddCity)
        btnBack.setOnClickListener {
            finish()
        }

        etSearchCity = findViewById(R.id.etSearchCity)
        rvAddCity = findViewById(R.id.rvAddCity)

        val availableCities = WorldClockStorage.ALL_AVAILABLE_CITIES

        addCityAdapter = AddCityAdapter(availableCities) { selectedCity ->
            val added = WorldClockStorage.addCity(this, selectedCity)
            if (added) {
                Toast.makeText(this, getString(R.string.city_added, selectedCity.cityName), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.city_already_exists, selectedCity.cityName), Toast.LENGTH_SHORT).show()
            }
        }

        rvAddCity.layoutManager = LinearLayoutManager(this)
        rvAddCity.adapter = addCityAdapter
    }

    private fun setupListeners() {
        etSearchCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addCityAdapter.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
