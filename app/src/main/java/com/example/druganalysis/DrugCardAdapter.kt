package com.example.druganalysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DrugCardAdapter(
    private val items: List<DrugCardItem>,
    private val onClick: (DrugCardItem) -> Unit
) : RecyclerView.Adapter<DrugCardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drugImage: ImageView = itemView.findViewById(R.id.drugImage)
        val drugName: TextView = itemView.findViewById(R.id.drugName)
        val nutrientText: TextView = itemView.findViewById(R.id.nutrientText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_drug_cardd, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.itemView.setOnClickListener {
            onClick(item)
        }

        holder.drugName.text = item.drugName
        holder.drugImage.setImageResource(R.drawable.drug)

        val depletion = item.depletion   // ✅ 여기 수정됨

        holder.nutrientText.visibility = View.VISIBLE
        holder.nutrientText.text =
            if (depletion.isEmpty()) {
                "필요한 영양소가 없어요"
            } else {
                depletion.joinToString(", ") { "\"$it\"" } + " 이/가 필요해요"
            }
    }

    override fun getItemCount(): Int = items.size
}