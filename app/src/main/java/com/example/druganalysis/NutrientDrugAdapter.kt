package com.example.druganalysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NutrientDrugAdapter(
    private val items: List<DrugCardItem>,
    private val onClick: (DrugCardItem) -> Unit
) : RecyclerView.Adapter<NutrientDrugAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val drugImage: ImageView = view.findViewById(R.id.drugImage)
        val drugName: TextView = view.findViewById(R.id.drugNameKor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nutrient_drug, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // 🔹 약 이름만 변경
        holder.drugName.text = item.drugName

        // 🔹 이미지 고정
        holder.drugImage.setImageResource(R.drawable.drug)

        // 🔹 클릭 시 상세 이동
        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
