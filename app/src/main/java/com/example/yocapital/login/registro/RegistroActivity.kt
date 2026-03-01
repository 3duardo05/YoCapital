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
import java.security.MessageDigest

class RegistroActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        db = Firebase.firestore

        val inputNombre = findViewById<EditText>(R.id.input_nombre)
        val inputNegocio = findViewById<EditText>(R.id.input_negocio)
        val inputCorreo = findViewById<EditText>(R.id.input_correo_registro)
        val inputTelefono = findViewById<EditText>(R.id.input_telefono)
        val inputPass = findViewById<EditText>(R.id.input_pass_registro)
        val inputConfirmPass = findViewById<EditText>(R.id.input_confirm_pass)
        val btnRegistrarse = findViewById<Button>(R.id.btnSiguiente_a_sigActividad)
        val btnVolverLogin = findViewById<TextView>(R.id.btnIniciarSesion)

        btnVolverLogin.setOnClickListener { finish() }

        btnRegistrarse.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val negocio = inputNegocio.text.toString().trim()
            val correo = inputCorreo.text.toString().trim()
            val telefono = inputTelefono.text.toString().trim()
            val pass = inputPass.text.toString().trim()
            val confirmPass = inputConfirmPass.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Nombre, Correo y Contraseña son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gerenteActualId = SessionManager.getUserId(this)
            val esGerenteCreandoVendedor = SessionManager.getRol(this) == "gerente"

            val rolAsignado = if (esGerenteCreandoVendedor) "vendedor" else "gerente"
            val idDelGerenteResponsable = if (esGerenteCreandoVendedor) gerenteActualId else ""

            val passHasheada = hashPassword(pass)

            val usuarioMap = hashMapOf(
                "nombre" to nombre,
                "negocio" to negocio,
                "correo" to correo,
                "telefono" to telefono,
                "contrasena" to passHasheada,
                "rol" to rolAsignado,
                "gerentePadreId" to idDelGerenteResponsable,
                "fechaRegistro" to com.google.firebase.Timestamp.now()
            )

            db.collection("usuarios")
                .add(usuarioMap)
                .addOnSuccessListener { documento ->
                    Toast.makeText(this, "Registro exitoso como $rolAsignado", Toast.LENGTH_SHORT).show()

                    if (esGerenteCreandoVendedor) {
                        finish()
                    } else {
                        SessionManager.saveSession(this, documento.id, nombre, correo, rolAsignado)

                        val intent = Intent(this, com.example.yocapital.gerente.GerenteActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}