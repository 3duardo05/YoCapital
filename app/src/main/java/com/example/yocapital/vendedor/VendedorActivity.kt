package com.example.yocapital.vendedor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.example.yocapital.vendedor.home.HomeVendedorFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class VendedorActivity : AppCompatActivity() {

    private var fragmentoActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendedor)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            val nuevoFragmento = when (item.itemId) {
                R.id.menu_inicio -> "home"
                R.id.menu_agregar -> "agregar"
                R.id.menu_perfil -> "perfil"
                else -> null
            }

            if (nuevoFragmento != null && nuevoFragmento != fragmentoActual) {
                fragmentoActual = nuevoFragmento

                when (item.itemId) {
                    R.id.menu_inicio -> cambiarFragmento(HomeVendedorFragment())
                    R.id.menu_agregar -> cambiarFragmento(VentaVendedorFragment())
                    R.id.menu_perfil -> cambiarFragmento(PerfilVendedorFragment())
                }
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            fragmentoActual = "home"
            cambiarFragmento(HomeVendedorFragment())
            bottomNav.selectedItemId = R.id.menu_inicio
        }
    }

    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}