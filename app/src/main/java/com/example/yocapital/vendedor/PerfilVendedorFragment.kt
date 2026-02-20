package com.example.yocapital.vendedor // Deja el tuyo como estaba

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.yocapital.R

class PerfilVendedorFragment : Fragment() {

    // Esta función es la única importante: es la que dibuja la pantalla
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Aquí le dice: "Infla (dibuja) el diseño de fragment_home_vendedor"
        return inflater.inflate(R.layout.fragment_perfil_vendedor, container, false)
    }
}