package com.example.yocapital.gerente

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class GerenteActivity : AppCompatActivity() {

    private var fragmentoActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerente)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            val nuevoFragmento = when (item.itemId) {
                R.id.nav_inicio -> "inicio"
                R.id.nav_equipo -> "equipo"
                R.id.nav_productos -> "productos"
                R.id.nav_perfil -> "perfil"
                else -> null
            }

            if (nuevoFragmento != null && nuevoFragmento != fragmentoActual) {
                fragmentoActual = nuevoFragmento

                when (item.itemId) {
                    R.id.nav_inicio -> cambiarFragmento(HomeGerenteFragment())
                    R.id.nav_equipo -> cambiarFragmento(VendedoresParentFragment())
                    R.id.nav_productos -> cambiarFragmento(ProductosParentFragment())
                    R.id.nav_perfil -> cambiarFragmento(PerfilGerenteFragment())
                }
                true
            } else {
                false
            }
        }

        if (savedInstanceState == null) {
            fragmentoActual = "inicio"
            cambiarFragmento(HomeGerenteFragment())
            bottomNav.selectedItemId = R.id.nav_inicio
        }
    }

    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}