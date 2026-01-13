package com.example.druganalysis

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class AiResultFragment : Fragment() {

    companion object {
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(message: String): AiResultFragment {
            return AiResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MESSAGE, message)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ai_result, container, false)

        val textView = view.findViewById<TextView>(R.id.aiMessageText)
        textView.text = arguments?.getString(ARG_MESSAGE) ?: "결과 없음"

        return view
    }
}
