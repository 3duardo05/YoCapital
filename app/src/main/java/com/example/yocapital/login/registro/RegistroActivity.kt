package com.example.yocapital.login.login

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.R

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. CORRECCIÓN: Usa el layout de registro
        setContentView(R.layout.activity_registro)

        // 2. CORRECCIÓN: Busca el ID del botón/texto que dice "Iniciar Sesión"
        val btnVolverLogin = findViewById<TextView>(R.id.btnIniciarSesion)

        btnVolverLogin.setOnClickListener {
            // 3. CORRECCIÓN: Usamos finish() para cerrar esta pantalla y volver
            finish()
        }
    }
}