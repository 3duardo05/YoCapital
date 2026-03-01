package com.example.yocapital.login.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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
            val correoIngresado = inputCorreo.text.toString().trim()
            val contrasenaIngresada = inputContrasena.text.toString().trim()

            if (correoIngresado.isNotEmpty() && contrasenaIngresada.isNotEmpty()) {

                db.collection("usuarios")
                    .whereEqualTo("correo", correoIngresado)
                    .get()
                    .addOnSuccessListener { documentos ->
                        if (!documentos.isEmpty) {
                            val usuarioDoc = documentos.documents[0]
                            val contrasenaBaseDatos = usuarioDoc.getString("contrasena") ?: ""

                            if (contrasenaIngresada == contrasenaBaseDatos) {

                                val userId = usuarioDoc.id
                                val nombre = usuarioDoc.getString("nombre") ?: "Usuario"
                                val rol = usuarioDoc.getString("rol") ?: "vendedor"
                                val correo = usuarioDoc.getString("correo") ?: correoIngresado

                                SessionManager.saveSession(this, userId, nombre, correo, rol)

                                Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()

                                navegarAlHome(rol)

                            } else {
                                Toast.makeText(this, "La contraseña es incorrecta", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "El correo no está registrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navegarAlHome(rol: String) {
        val intent = if (rol == "gerente") {
            Intent(this, com.example.yocapital.gerente.GerenteActivity::class.java)
        } else {
            Intent(this, com.example.yocapital.vendedor.VendedorActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}