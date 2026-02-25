package com.example.yocapital.login.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        db = Firebase.firestore

        val btnRegistrarse = findViewById<TextView>(R.id.btnRegistrarse)
        val btnIniciarSesion = findViewById<Button>(R.id.btn_IniciarSesion)
        val inputCorreo = findViewById<EditText>(R.id.input_correo)
        val inputContrasena = findViewById<EditText>(R.id.input_contrasena)

        btnRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }

        btnIniciarSesion.setOnClickListener {
            val correo = inputCorreo.text.toString().trim()
            val contrasena = inputContrasena.text.toString().trim()

            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {

                auth.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val userId = auth.currentUser?.uid

                            if (userId != null) {
                                db.collection("usuarios").document(userId).get()
                                    .addOnSuccessListener { documento ->
                                        if (documento.exists()) {
                                            val rol = documento.getString("rol") ?: ""

                                            val prefs = getSharedPreferences("YoCapitalPrefs", Context.MODE_PRIVATE).edit()
                                            prefs.putBoolean("sesionIniciada", true)
                                            prefs.putString("rol", rol)
                                            prefs.apply()

                                            if (rol == "gerente") {
                                                startActivity(Intent(this, com.example.yocapital.gerente.GerenteActivity::class.java))
                                            } else {
                                                startActivity(Intent(this, com.example.yocapital.vendedor.VendedorActivity::class.java))
                                            }

                                            finish()
                                        } else {
                                            Toast.makeText(this, "El usuario no tiene un rol asignado.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            // Borra el Toast que tenías y pon este:
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()

                        }
                    }
            } else {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}