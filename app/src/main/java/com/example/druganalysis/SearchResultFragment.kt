package com.example.druganalysis

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.druganalysis.databinding.FragmentSearchResultBinding
import com.google.android.material.chip.Chip
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private const val MAX_CHIP_COUNT = 5

class SearchResultFragment : Fragment() {

    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DrugSearchAdapter
    private val handler = Handler(Looper.getMainLooper())

    private val selectedDrugs = mutableSetOf<String>()  // 중복 방지

    private var searchRunnable: Runnable? = null

    // ---------------------------
    // Fragment 생성자 패턴 (정석)
    // ---------------------------
    companion object {
        private const val ARG_QUERY = "arg_query"

        fun newInstance(query: String): SearchResultFragment {
            return SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY, query)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchButton.isEnabled = false

        // ---------------------------
        // 뒤로가기
        // ---------------------------
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ---------------------------
        // RecyclerView 설정
        // ---------------------------
        adapter = DrugSearchAdapter { drugName ->
            onDrugClicked(drugName)
        }

        binding.drugRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchResultFragment.adapter
        }

        // ---------------------------
        // Search 버튼 클릭
        // ---------------------------
        binding.searchButton.setOnClickListener {
            requestInteractionAnalysis()
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s.toString().trim()

                // 🔥 이전 예약된 검색 취소
                searchRunnable?.let { handler.removeCallbacks(it) }

                if (keyword.isEmpty()) {
                    adapter.submitList(emptyList())
                    return
                }

                // 🔥 새 검색 예약
                searchRunnable = Runnable {
                    searchDrugFromServer(keyword)
                }

                // ⏱ 400ms 디바운스
                handler.postDelayed(searchRunnable!!, 200)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // ---------------------------
        // SearchFragment에서 넘어온 초기 검색어 처리
        // ---------------------------
        val initialQuery = arguments?.getString(ARG_QUERY)
        if (!initialQuery.isNullOrBlank()) {
            binding.searchInput.setText(initialQuery)
            searchDrugFromServer(initialQuery)
        }

        // ---------------------------
        // 키보드 자동 표시
        // ---------------------------
        handler.postDelayed({
            if (!isAdded) return@postDelayed
            binding.searchInput.requestFocus()
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }

    private fun updateSearchButton() {
        binding.searchButton.isEnabled = selectedDrugs.size >= 2
    }

    /**
     * 서버에서 약 검색
     * GET /search/drug?query=키워드
     */
    private fun searchDrugFromServer(keyword: String) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://10.0.2.2:8001/search/drug?query=$keyword")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                if (!isAdded) return
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "서버 연결 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) return
                val body = response.body?.string() ?: return

                val json = JSONObject(body)
                val results = json.getJSONArray("results")

                val list = mutableListOf<String>()
                for (i in 0 until results.length()) {
                    list.add(results.getString(i))
                }

                if (!isAdded) return
                requireActivity().runOnUiThread {
                    adapter.submitList(list)
                }
            }
        })
    }

    private fun requestInteractionAnalysis() {
        if (selectedDrugs.size < 2) {
            Toast.makeText(requireContext(), "약을 2개 이상 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()

        val drugArray = org.json.JSONArray()
        selectedDrugs.forEach { drugArray.put(it) }

        val json = JSONObject().apply {
            put("drug_names", drugArray)   // ✅ 리스트
        }

        val body = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:8001/check/interaction")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("INTERACTION_RESULT", "서버 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val raw = response.body?.string()
                android.util.Log.d("INTERACTION_RESULT", raw ?: "null")
            }
        })
    }

    private fun onDrugClicked(drugName: String) {
        if (selectedDrugs.size >= MAX_CHIP_COUNT) {
            showLimitBottomSheet()
            return
        }
        addChip(drugName)
    }

    private fun showLimitBottomSheet() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_chip_limit, null)

        view.findViewById<View>(R.id.confirmButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }


    private fun addChip(drugName: String) {
        if (selectedDrugs.contains(drugName)) return

        selectedDrugs.add(drugName)

        val chip = Chip(requireContext()).apply {
            text = drugName
            isCloseIconVisible = true
            isClickable = false
            isCheckable = false
            chipCornerRadius = 50f

            setOnCloseIconClickListener {
                binding.chipGroup.removeView(this)
                selectedDrugs.remove(drugName)
                updateSearchButton()   // ✅ 중요
            }
        }

        binding.chipGroup.addView(chip)

        updateSearchButton()          // ✅ 중요

        binding.searchInput.text.clear()
        adapter.submitList(emptyList())
    }

    private fun requestNutrientFromServer(drugName: String) {
        val client = OkHttpClient()

        val json = JSONObject()
        json.put("drug_name", drugName)

        val requestBody = json.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url("http://10.0.2.2:8001/check/nutrient")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("SERVER_NUTRIENT", "영양소 API 실패", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val rawJson = response.body?.string()

                // 🔥 서버 응답 JSON 전체 그대로 출력
                android.util.Log.d("SERVER_NUTRIENT", "HTTP ${response.code}")
                android.util.Log.d("SERVER_NUTRIENT", rawJson ?: "null")
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
