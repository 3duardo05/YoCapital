package com.example.yocapital // Asegúrate de que este sea tu paquete

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.login.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("YoCapitalPrefs", Context.MODE_PRIVATE)

        // 2. Leemos las variables guardadas (si no existen, por defecto serán 'false' o vacío)
        val vioOnboarding = prefs.getBoolean("vioOnboarding", false)
        val sesionIniciada = prefs.getBoolean("sesionIniciada", false)
        val rol = prefs.getString("rol", "") // Puede ser "vendedor" o "gerente"

        // 3. La lógica de decisión (El Cadenero)
        val intentDeDestino = if (!vioOnboarding) {
            // Si NUNCA ha visto el onboarding, lo mandamos ahí
            Intent(this, com.example.yocapital.onboarding.OnBoardingActivity::class.java)
        } else if (!sesionIniciada) {
            // Si ya lo vio, pero NO ha iniciado sesión, lo mandamos al Login
            Intent(this, LoginActivity::class.java)
        } else {
            if (rol == "vendedor") {
                Intent(this, com.example.yocapital.vendedor.VendedorActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
        }

        startActivity(intentDeDestino)

        finish()
    }
}