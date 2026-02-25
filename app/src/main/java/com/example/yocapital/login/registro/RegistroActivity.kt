package com.example.yocapital.login.login

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.R

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        val btnVolverLogin = findViewById<TextView>(R.id.btnIniciarSesion)

        btnVolverLogin.setOnClickListener {
            finish()
        }
    }
}