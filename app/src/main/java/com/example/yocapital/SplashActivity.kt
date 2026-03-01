package com.example.yocapital

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.login.SessionManager
import com.example.yocapital.login.login.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                    Intent(this, com.example.yocapital.gerente.GerenteActivity::class.java)
                } else {
                    Intent(this, com.example.yocapital.vendedor.VendedorActivity::class.java)
                }
            }
        }

        startActivity(intentDeDestino)
        finish()
    }
}