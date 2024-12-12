package com.capstone.temfore.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.capstone.temfore.R


class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private lateinit var slideImage: ImageView
    private lateinit var slideText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slideImage = view.findViewById(R.id.slideImage)
        slideText = view.findViewById(R.id.slideText)

        val imageRes = arguments?.getInt("image")
        val textRes = arguments?.getInt("text")

        slideImage.setImageResource(imageRes ?: 0)
        slideText.setText(textRes ?: 0)
    }

    companion object {
        fun newInstance(imageRes: Int, textRes: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val bundle = Bundle().apply {
                putInt("image", imageRes)
                putInt("text", textRes)
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
