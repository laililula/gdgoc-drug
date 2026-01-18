package com.example.druganalysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip

class AiResultFragment : Fragment() {

    companion object {
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(
            message: String,
            drugCards: ArrayList<DrugCardItem>
        ): AiResultFragment {
            return AiResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE, message)
                    putParcelableArrayList("drug_cards", drugCards)
                }
            }
        }

    }

    // 탭 관련 View
    private lateinit var tabDrugs: TextView
    private lateinit var tabNutrients: TextView
    private lateinit var underlineDrugs: View
    private lateinit var underlineNutrients: View

    // 영양소 탭 UI
    private lateinit var nutrientSection: View
    private lateinit var drugSection: View
    private lateinit var nutrientChart: PieChart
    private lateinit var nutrientChipFlexbox: FlexboxLayout
    private lateinit var nutrientDrugRecyclerView: RecyclerView

    // 데이터
    private lateinit var nutrientDrugMap: Map<String, List<DrugCardItem>>
    private var selectedNutrient: String? = null

    // 결과 리스트
    private lateinit var drugRecyclerView: RecyclerView

    private lateinit var summaryRecyclerView: RecyclerView
    private lateinit var cautionRecyclerView: RecyclerView
    private lateinit var backIcon: ImageView
    private lateinit var drugFlexbox: FlexboxLayout
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ai_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔙 뒤로가기 버튼
        backIcon = view.findViewById(R.id.backIcon)
        saveButton = view.findViewById(R.id.saveButton)
        backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        drugSection = view.findViewById(R.id.drugSection)
        nutrientSection = view.findViewById(R.id.nutrientSection)

        summaryRecyclerView = view.findViewById(R.id.aiSentenceRecyclerView)
        cautionRecyclerView = view.findViewById(R.id.aiSentenceRecyclerView2)
        drugFlexbox = view.findViewById(R.id.selectedDrugFlexbox)

        summaryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cautionRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val rawMessage = arguments?.getString(ARG_MESSAGE) ?: return

        val (summaryList, cautionList) = parseAiMessage(rawMessage)
        summaryRecyclerView.adapter = AiSentenceAdapter(summaryList)
        cautionRecyclerView.adapter = AiSentenceAdapter(cautionList)

        tabDrugs = view.findViewById(R.id.tabDrugs)
        tabNutrients = view.findViewById(R.id.tabNutrients)
        underlineDrugs = view.findViewById(R.id.underlineDrugs)
        underlineNutrients = view.findViewById(R.id.underlineNutrients)
        drugRecyclerView = view.findViewById(R.id.drugResultRecyclerView)

        nutrientSection = view.findViewById(R.id.nutrientSection)
        nutrientChart = view.findViewById(R.id.nutrientDonutChart)
        nutrientChipFlexbox = view.findViewById(R.id.nutrientChipFlexbox)
        nutrientDrugRecyclerView = view.findViewById(R.id.nutrientDrugRecyclerView)

        nutrientDrugRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())


        drugRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val drugCards =
            arguments?.getParcelableArrayList<DrugCardItem>("drug_cards")
                ?: emptyList()

        // 🔥 Flexbox도 drugCards 기준
        renderSelectedDrugs(drugCards.map { it.drugName })

        drugRecyclerView.adapter = DrugCardAdapter(
            items = drugCards,
            onClick = { drugItem ->
                openDrugDetail(drugItem)
            }
        )

        nutrientDrugMap =
            drugCards
                .flatMap { drug ->
                    drug.depletion.map { nutrient -> nutrient to drug }
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )

        setSelectedTab(true)

        saveButton.setOnClickListener {
            SaveMyDrugBottomSheet(
                drugs = drugCards
            ) { selectedList ->
                // 👉 여기서 다음 Fragment로 이동
                //openNextFragment(selectedList)
            }.show(parentFragmentManager, "SaveMyDrug")
        }

        tabDrugs.setOnClickListener { setSelectedTab(true) }
        tabNutrients.setOnClickListener { setSelectedTab(false) }
    }

    private fun renderNutrientChips() {
        nutrientChipFlexbox.removeAllViews()

        nutrientDrugMap.keys.forEach { nutrient ->

            val chip = Chip(requireContext()).apply {
                text = nutrient
                isCheckable = true
                isChecked = nutrient == selectedNutrient

                setOnClickListener {
                    // 🔥 핵심: 선택 상태 변경
                    if (selectedNutrient != nutrient) {
                        onNutrientSelected(nutrient)
                        renderNutrientChips() // 🔥 전체 재렌더
                    }
                }
            }

            nutrientChipFlexbox.addView(chip)
        }
    }

    private fun onNutrientSelected(nutrient: String) {
        selectedNutrient = nutrient

        setupDonutChart(nutrient)

        val list = nutrientDrugMap[nutrient].orEmpty()
        nutrientDrugRecyclerView.adapter =
            NutrientDrugAdapter(list) { drug ->
                openDrugDetail(drug)
            }
    }

//    private fun openNextFragment(selected: List<DrugCardItem>) {
//        parentFragmentManager.beginTransaction()
//            .replace(
//                R.id.fragmentContainer,
//                Fragment.newInstance(ArrayList(selected))
//            )
//            .addToBackStack(null)
//            .commit()
//    }

    private fun openDrugDetail(item: DrugCardItem) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                DrugDetailFragment.newInstance(item)
            )
            .addToBackStack(null)
            .commit()
    }

    private fun parseAiMessage(message: String): Pair<List<String>, List<String>> {
        val summary = mutableListOf<String>()
        val caution = mutableListOf<String>()

        var currentSection: String? = null

        message.lines().forEach { line ->
            val text = line.trim()

            when {
                text.startsWith("AI 요약") -> {
                    currentSection = "SUMMARY"
                }
                text.startsWith("주의할 점") -> {
                    currentSection = "CAUTION"
                }
                text.startsWith("-") -> {
                    val content = text.removePrefix("-").trim()
                    if (content.isNotEmpty()) {
                        when (currentSection) {
                            "SUMMARY" -> summary.add(content)
                            "CAUTION" -> caution.add(content)
                        }
                    }
                }
            }
        }

        return Pair(summary, caution)
    }

    private fun renderSelectedDrugs(drugs: List<String>) {
        drugFlexbox.removeAllViews() // 🔥 중요 (중복 방지)

        for (drug in drugs) {
            val textView = TextView(requireContext()).apply {
                text = drug
                textSize = 14f
                setTextColor(resources.getColor(R.color.black, null))
                setPadding(24, 12, 24, 12)
                background = resources.getDrawable(
                    R.drawable.dialog_holo_light_frame,
                    null
                )
            }

            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }

            drugFlexbox.addView(textView, params)
        }
    }

    private fun setSelectedTab(isDrugTab: Boolean) {
        if (isDrugTab) {
            tabDrugs.setTextColor(Color.BLACK)
            underlineDrugs.setBackgroundColor(Color.BLACK)

            tabNutrients.setTextColor(Color.GRAY)
            underlineNutrients.setBackgroundColor(Color.TRANSPARENT)

            drugSection.visibility = View.VISIBLE
            nutrientSection.visibility = View.GONE

        } else {
            tabNutrients.setTextColor(Color.BLACK)
            underlineNutrients.setBackgroundColor(Color.BLACK)

            tabDrugs.setTextColor(Color.GRAY)
            underlineDrugs.setBackgroundColor(Color.TRANSPARENT)

            drugSection.visibility = View.GONE
            nutrientSection.visibility = View.VISIBLE

            if (selectedNutrient == null && nutrientDrugMap.isNotEmpty()) {
                selectedNutrient = nutrientDrugMap.keys.first()
                onNutrientSelected(selectedNutrient!!)
            }

            renderNutrientChips()
        }
    }

    private fun setupDonutChart(selected: String?) {

        val entries = nutrientDrugMap.map { (nutrient, drugs) ->
            PieEntry(drugs.size.toFloat(), nutrient)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = entries.map {
                if (it.label == selected)
                    Color.parseColor("#F2994A")
                else
                    Color.parseColor("#E0E0E0")
            }

            sliceSpace = 0f

            // 🔥 추가
            setDrawValues(false)   // 퍼센트/값 제거
        }

        nutrientChart.data = PieData(dataSet).apply {
            setDrawValues(false)
        }

        nutrientChart.apply {
            isDrawHoleEnabled = true
            holeRadius = 40f
            setUsePercentValues(true)

            description.isEnabled = false
            legend.isEnabled = false

            // 🔥 핵심: 조각 라벨 제거
            setDrawEntryLabels(false)

            // 🔥 터치 차단
            setTouchEnabled(false)
            isHighlightPerTapEnabled = false

            // 🔥 중앙 텍스트
            centerText = selected ?: ""
            setCenterTextColor(Color.BLACK)
            setCenterTextSize(16f)
            setCenterTextTypeface(android.graphics.Typeface.DEFAULT_BOLD)

            invalidate()
        }

    }


}
