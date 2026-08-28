package com.example.alarmclock.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmclock.R
import com.example.alarmclock.model.LanguageItem

class LanguageAdapter(
    private val items: List<LanguageItem>,
    private val onItemSelected: (LanguageItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedIndex = items.indexOfFirst { it.isSelected }.let { if (it >= 0) it else 0 }

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.layoutLanguageItem)
        val ivFlag: ImageView = itemView.findViewById(R.id.ivFlag)
        val tvName: TextView = itemView.findViewById(R.id.tvLanguageName)
        val ivRadio: ImageView = itemView.findViewById(R.id.ivRadio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = items[position]
        holder.ivFlag.setImageResource(item.flagResId)
        holder.tvName.text = item.displayName

        if (position == selectedIndex) {
            holder.container.setBackgroundResource(R.drawable.bg_language_item_selected)
            holder.ivRadio.setImageResource(R.drawable.ic_radio_checked)
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_language_item_unselected)
            holder.ivRadio.setImageResource(R.drawable.ic_radio_unchecked)
        }

        holder.itemView.setOnClickListener {
            val prevIndex = selectedIndex
            selectedIndex = holder.bindingAdapterPosition
            if (prevIndex != selectedIndex) {
                notifyItemChanged(prevIndex)
                notifyItemChanged(selectedIndex)
            }
            onItemSelected(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItem(): LanguageItem {
        return items[selectedIndex]
    }
}
