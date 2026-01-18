package com.example.druganalysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class MyDrugAdapter(
    private val items: List<DrugCardItem>,
    private val onSelectionChanged: (Set<DrugCardItem>) -> Unit
) : RecyclerView.Adapter<MyDrugAdapter.VH>() {

    private val selectedItems = mutableSetOf<DrugCardItem>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.drugNameKor)
        val check: ImageView = view.findViewById(R.id.checkIcon)
    }

    // ✅ 반드시 필요
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_drug_select, parent, false)
        return VH(view)
    }

    // ✅ 반드시 필요
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val isSelected = selectedItems.contains(item)

        holder.name.text = item.drugName
        holder.check.visibility = if (isSelected) View.VISIBLE else View.GONE

        val card = holder.itemView as MaterialCardView
        card.isChecked = isSelected   // 🔥 핵심

        holder.itemView.setOnClickListener {
            if (isSelected) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }
            notifyItemChanged(position)
            onSelectionChanged(selectedItems)
        }
    }

    // ✅ 반드시 필요
    override fun getItemCount(): Int = items.size
}