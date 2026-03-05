package com.example.yocapital.gerente

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class GerenteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gerente)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Arrancamos mostrando la pantalla de Agregar Producto por defecto
        cargarFragmento(AgregarProductoFragment())

        // Le decimos qué hacer cuando se presiona cada botón del menú
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    // Aquí irá la pantalla de gráficas que diseñen después
                    // Por ahora no hace nada para que no truene
                    true
                }
                R.id.nav_equipo -> {
                    cargarFragmento(AgregarVendedorFragment()) // Tu pantalla de Vendedores
                    true
                }
                R.id.nav_productos -> {
                    cargarFragmento(AgregarProductoFragment()) // Tu pantalla de Productos
                    true
                }
                else -> false
            }
        }
    }

    // Esta función es la que hace la magia de cambiar el "canal" en la televisión
    private fun cargarFragmento(fragmento: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_fragmentos, fragmento)
            .commit()
    }
}