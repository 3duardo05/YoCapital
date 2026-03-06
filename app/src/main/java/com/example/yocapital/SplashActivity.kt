package com.example.yocapital

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.gerente.GerenteActivity
import com.example.yocapital.login.SessionManager
import com.example.yocapital.login.login.LoginActivity
import com.example.yocapital.vendedor.VendedorActivity
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Configurar Firebase
        val settings = com.google.firebase.firestore.firestoreSettings {
            isPersistenceEnabled = true
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings

        // ✅ Mostrar el diseño del splash
        setContentView(R.layout.activity_splash)

        // ✅ Obtener vistas
        val logo = findViewById<ImageView>(R.id.logo)
        val nombre = findViewById<TextView>(R.id.txtNombre)

        // ✅ Animación de entrada
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f
        nombre.alpha = 0f

        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .start()

        nombre.animate()
            .alpha(1f)
            .setStartDelay(500)
            .setDuration(1000)
            .start()

        // ✅ Esperar 2.5 segundos y navegar
        Handler(Looper.getMainLooper()).postDelayed({
            navegarSiguientePantalla()
        }, 2500)
    }

    private fun navegarSiguientePantalla() {
        val intentDeDestino = when {
            SessionManager.veteAOnboarding(this) -> {
                Intent(this, com.example.yocapital.onboarding.OnBoardingActivity::class.java)
            }
            !SessionManager.isLogueado(this) -> {
                Intent(this, LoginActivity::class.java)
            }
            else -> {
                val rol = SessionManager.getRol(this)
                if (rol == "gerente") {
                    Intent(this, GerenteActivity::class.java)
                } else {
                    Intent(this, VendedorActivity::class.java)
                }
            }
        }

        startActivity(intentDeDestino)
        finish()
    }
}