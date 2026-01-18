package com.example.druganalysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SaveMyDrugBottomSheet(
    private val drugs: List<DrugCardItem>,
    private val onSave: (List<DrugCardItem>) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_my_drugs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler = view.findViewById<RecyclerView>(R.id.myDrugRecyclerView)
        saveButton = view.findViewById(R.id.saveButton)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        val adapter = MyDrugAdapter(drugs) { selected ->
            saveButton.text = "약 ${selected.size}개 저장"
            saveButton.setOnClickListener {
                onSave(selected.toList())
                dismiss()
            }
        }

        recycler.adapter = adapter
    }
}
