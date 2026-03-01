package com.example.yocapital.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.example.yocapital.login.login.LoginActivity

class OnBoarding2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_on_boarding2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnSiguiente = view.findViewById<Button>(R.id.btnSiguiente_a_sigActividad)
        val circuloAnterior = view.findViewById<ImageView>(R.id.circulo1)

        btnSiguiente.setOnClickListener {
            SessionManager.setOnboardingVisto(requireContext())

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()
        }

        circuloAnterior.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 0
        }
    }
}