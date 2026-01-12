package com.example.druganalysis

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.example.druganalysis.databinding.FragmentSearchBinding
import com.google.android.material.chip.Chip

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val recentKeywords = listOf("타이레놀", "이부프로펜", "모사피아정")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    SearchResultFragment.newInstance(
                        binding.searchInput.text.toString()
                    )
                )
                .addToBackStack(null)
                .commit()
        }


        // 200ms 후 키보드 자동 표시
        Handler(Looper.getMainLooper()).postDelayed({
            binding.searchInput.requestFocus()
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }, 200)

        // 검색어 유무에 따른 UI
        if (recentKeywords.isEmpty()) {
            binding.emptyBox.visibility = View.VISIBLE
            binding.recentChipGroup.visibility = View.GONE
        } else {
            binding.emptyBox.visibility = View.GONE
            binding.recentChipGroup.visibility = View.VISIBLE

            recentKeywords.forEach { keyword ->
                val chip = Chip(requireContext()).apply {
                    text = keyword
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        binding.recentChipGroup.removeView(this)
                    }
                }
                binding.recentChipGroup.addView(chip)
            }
        }

        // 뒤로가기
        binding.backIcon.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}