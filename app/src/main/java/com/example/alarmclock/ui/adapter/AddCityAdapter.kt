package com.example.alarmclock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.WorldClockCity

class AddCityAdapter(
    private var cities: List<WorldClockCity>,
    private val onAddClick: (city: WorldClockCity) -> Unit
) : RecyclerView.Adapter<AddCityAdapter.AddCityViewHolder>() {

    private var filteredCities: MutableList<WorldClockCity> = cities.toMutableList()

    class AddCityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCityName: TextView = itemView.findViewById(R.id.tvAddCityName)
        val tvCountryName: TextView = itemView.findViewById(R.id.tvAddCountryName)
        val btnAddCity: ImageButton = itemView.findViewById(R.id.btnAddCity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddCityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_city, parent, false)
        return AddCityViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddCityViewHolder, position: Int) {
        val city = filteredCities[position]
        holder.tvCityName.text = city.cityName
        holder.tvCountryName.text = city.countryName

        holder.btnAddCity.setOnClickListener {
            onAddClick(city)
        }
        holder.itemView.setOnClickListener {
            onAddClick(city)
        }
    }

    override fun getItemCount(): Int = filteredCities.size

    fun filter(query: String) {
        val trimmed = query.trim().lowercase()
        filteredCities = if (trimmed.isEmpty()) {
            cities.toMutableList()
        } else {
            cities.filter {
                it.cityName.lowercase().contains(trimmed) || it.countryName.lowercase().contains(trimmed)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
