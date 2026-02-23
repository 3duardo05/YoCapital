package com.example.yocapital.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.yocapital.R

class OnBoarding2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_on_boarding2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSiguiente = view.findViewById<Button>(R.id.btnSiguiente_a_fragmento2)
        val circuloAnterior = view.findViewById<ImageView>(R.id.circulo1)

//        btnSiguiente.setOnClickListener {
//            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
//            viewPager.currentItem = 1
//        }

        circuloAnterior.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 0
        }
    }
}