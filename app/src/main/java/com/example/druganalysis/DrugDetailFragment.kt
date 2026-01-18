package com.example.druganalysis

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class DrugDetailFragment : Fragment() {

    companion object {
        private const val ARG_ITEM = "arg_item"

        fun newInstance(item: DrugCardItem): DrugDetailFragment {
            return DrugDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ITEM, item)
                }
            }
        }
    }

    // 🔹 lateinit 선언
    private lateinit var drugImage: ImageView
    private lateinit var drugNameText: TextView
    private lateinit var depletionText: TextView
    private lateinit var avoidText: TextView
    private lateinit var foodsText: TextView
    private lateinit var backIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_drug_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔙 뒤로가기 버튼
        backIcon = view.findViewById(R.id.backIcon)
        backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ 1️⃣ 반드시 먼저 findViewById
        drugImage = view.findViewById(R.id.drugImage)
        drugNameText = view.findViewById(R.id.drugNameText)
        depletionText = view.findViewById(R.id.depletionText)
        avoidText = view.findViewById(R.id.avoidText)
        foodsText = view.findViewById(R.id.foodsText)

        // ✅ 2️⃣ 그 다음 arguments 사용
        val item = arguments?.getParcelable<DrugCardItem>(ARG_ITEM)
        if (item == null) {
            Log.e("DrugDetailFragment", "DrugCardItem is null")
            return
        }

        // ✅ 3️⃣ 이제 안전
        drugNameText.text = item.drugName
        drugImage.setImageResource(R.drawable.drug)

        requestDrugDetailFromServer(item)
    }

    private fun requestDrugDetailFromServer(item: DrugCardItem) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("drug_name", item.drugName)
            put("raw", JSONObject().apply {
                put("depletion", JSONArray(item.depletion))
                put("avoid", JSONArray(item.avoid))
                put("foods", JSONArray(item.foods))
            })
        }

        val body = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:8001/ai/drug-detail")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DRUG_DETAIL", "서버 요청 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = response.body?.string()

                // 🔥 1. 서버 원본 응답 로그
                Log.d("DETAIL_RESPONSE_RAW", raw ?: "응답 바디 없음")

                if (raw == null) return

                val obj = JSONObject(raw)

                // 🔥 2. 파싱 직후 로그
                Log.d("DETAIL_RESPONSE_JSON", obj.toString(2))

                requireActivity().runOnUiThread {
                    bindAiResult(obj)
                }
            }

        })
    }

    private fun bindAiResult(obj: JSONObject) {

        if (!obj.optBoolean("success", false)) {
            depletionText.text = "정보 없음"
            avoidText.text = "정보 없음"
            foodsText.text = "정보 없음"
            return
        }

        val aiText = obj.optString("ai_text", "")
        if (aiText.isBlank()) {
            depletionText.text = "정보 없음"
            avoidText.text = "정보 없음"
            foodsText.text = "정보 없음"
            return
        }

        // 🔥 섹션별 분리
        val depletion = extractSection(aiText, "결핍 영양소 설명:")
        val avoid = extractSection(aiText, "피해야 할 것 설명:")
        val foods = extractSection(aiText, "추천 음식 설명:")

        depletionText.text = depletion.ifBlank { "정보 없음" }
        avoidText.text = avoid.ifBlank { "정보 없음" }
        foodsText.text = foods.ifBlank { "정보 없음" }
    }

    private fun extractSection(text: String, title: String): String {
        val start = text.indexOf(title)
        if (start == -1) return ""

        val contentStart = start + title.length
        val end = text.indexOf("\n\n", contentStart)

        return if (end == -1) {
            text.substring(contentStart).trim()
        } else {
            text.substring(contentStart, end).trim()
        }
    }



}