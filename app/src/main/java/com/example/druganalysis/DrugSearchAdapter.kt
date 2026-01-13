package com.example.druganalysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DrugSearchAdapter(
    private val selectedDrugs: Set<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<DrugSearchAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()

    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val drugNameText = view.findViewById<TextView>(R.id.drugNameText)
        private val container = view   // item 전체
        private val checkIcon = view.findViewById<View>(R.id.checkIcon) // ✅ 추가

        fun bind(name: String) {
            drugNameText.text = name

            val isSelected = selectedDrugs.contains(name)

            // ✅ 선택 UI 반영
            container.isSelected = isSelected
            checkIcon.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onClick(name)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drug_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
