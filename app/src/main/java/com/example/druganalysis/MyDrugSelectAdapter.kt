package com.example.druganalysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyDrugSelectAdapter(
    private val items: List<DrugCardItem>,
    private val onSelectionChanged: (Set<DrugCardItem>) -> Unit
) : RecyclerView.Adapter<MyDrugSelectAdapter.VH>() {

    private val selectedItems = mutableSetOf<DrugCardItem>()

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.drugNameText)
        val check: ImageView = view.findViewById(R.id.checkIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_drug_select, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        holder.name.text = item.drugName

        holder.check.visibility =
            if (selectedItems.contains(item)) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }
            notifyItemChanged(position)
            onSelectionChanged(selectedItems)
        }
    }

    override fun getItemCount() = items.size
}
