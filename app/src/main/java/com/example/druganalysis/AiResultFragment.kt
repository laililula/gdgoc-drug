package com.example.druganalysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout

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

    // 결과 리스트
    private lateinit var drugRecyclerView: RecyclerView

    private lateinit var summaryRecyclerView: RecyclerView
    private lateinit var cautionRecyclerView: RecyclerView
    private lateinit var backIcon: ImageView
    private lateinit var drugFlexbox: FlexboxLayout

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
        backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

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

        setSelectedTab(true)

        tabDrugs.setOnClickListener { setSelectedTab(true) }
        tabNutrients.setOnClickListener { setSelectedTab(false) }
    }

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
            tabDrugs.setTextColor(requireContext().getColor(android.R.color.black))
            underlineDrugs.setBackgroundColor(requireContext().getColor(android.R.color.black))

            tabNutrients.setTextColor(requireContext().getColor(android.R.color.darker_gray))
            underlineNutrients.setBackgroundColor(
                requireContext().getColor(android.R.color.transparent)
            )

            // 👉 약 리스트 표시
            drugRecyclerView.visibility = View.VISIBLE

        } else {
            tabNutrients.setTextColor(requireContext().getColor(android.R.color.black))
            underlineNutrients.setBackgroundColor(requireContext().getColor(android.R.color.black))

            tabDrugs.setTextColor(requireContext().getColor(android.R.color.darker_gray))
            underlineDrugs.setBackgroundColor(
                requireContext().getColor(android.R.color.transparent)
            )

            // 👉 영양소 탭일 때 (추후 RecyclerView 교체 가능)
            drugRecyclerView.visibility = View.GONE
        }
    }
}
