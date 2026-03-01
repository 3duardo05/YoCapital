package com.example.yocapital.vendedor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class VendedorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_vendedor)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_inicio -> {
                    cambiarFragmento(HomeVendedorFragment())
                    true
                }
                R.id.menu_agregar -> {
                    cambiarFragmento(VentaVendedorFragment())
                    true
                }
                R.id.menu_perfil -> {
                    cambiarFragmento(PerfilVendedorFragment())
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
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