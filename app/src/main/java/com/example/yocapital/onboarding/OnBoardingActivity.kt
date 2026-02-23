package com.example.yocapital.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.yocapital.R

class OnBoardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = OnboardingAdapter(this)
    }

    private inner class OnboardingAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> OnBoarding1Fragment()
                1 -> OnBoarding2Fragment()
                else -> OnBoarding1Fragment()
            }
        }
    }
}