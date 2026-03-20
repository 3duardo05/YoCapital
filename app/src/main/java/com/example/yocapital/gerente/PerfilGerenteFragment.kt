package com.example.yocapital.gerente

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.example.yocapital.login.login.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore

class PerfilGerenteFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_perfil_gerente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtNombre = view.findViewById<TextView>(R.id.txt_nombre_usuario)
        val txtCorreo = view.findViewById<TextView>(R.id.txt_correo_usuario)
        val txtTelefono = view.findViewById<TextView>(R.id.txt_telefono_usuario)
        val btnEditarPerfil = view.findViewById<Button>(R.id.btn_editar_perfil)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btn_cerrar_sesion)
        val btnVistaPrevia = view.findViewById<Button>(R.id.btn_vista_previa_reporte)
        val btnDescargarPdf = view.findViewById<Button>(R.id.btn_descargar_pdf)

        cargarDatosGerente(txtNombre, txtCorreo, txtTelefono)

        cargarEstadisticas(view)

        btnEditarPerfil.setOnClickListener {
            mostrarDialogoEditarPerfil()
        }

        btnVistaPrevia.setOnClickListener {
            val fragment = VistaReporteFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnDescargarPdf.setOnClickListener {
            val fragment = VistaReporteFragment()
            val bundle = Bundle()
            bundle.putBoolean("descargar_automatico", true)
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()

            Toast.makeText(requireContext(), "Generando PDF...", Toast.LENGTH_SHORT).show()
        }

        btnCerrarSesion.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("Sí") { _, _ ->
                    SessionManager.logout(requireContext())
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun cargarDatosGerente(txtNombre: TextView, txtCorreo: TextView, txtTelefono: TextView) {
        val userId = SessionManager.getUserId(requireContext())

        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { documento ->
                val nombre = documento.getString("nombre") ?: "Gerente"
                val correo = documento.getString("correo") ?: "Sin correo"
                val telefono = documento.getString("telefono") ?: "Sin teléfono"

                txtNombre.text = nombre
                txtCorreo.text = correo
                txtTelefono.text = telefono

                SessionManager.saveSession(
                    context = requireContext(),
                    id = userId,
                    nombre = nombre,
                    correo = correo,
                    rol = "gerente",
                    telefono = telefono
                )
            }
            .addOnFailureListener {
                txtNombre.text = SessionManager.getNombre(requireContext())
                txtCorreo.text = SessionManager.getCorreo(requireContext())
                txtTelefono.text = SessionManager.getTelefono(requireContext())

                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarEstadisticas(view: View) {
        val txtTotalVendedores = view.findViewById<TextView>(R.id.txt_total_vendedores)
        val txtTotalProductos = view.findViewById<TextView>(R.id.txt_total_productos)
        val txtVentasAnio = view.findViewById<TextView>(R.id.txt_ventas_anio)

        db.collection("usuarios")
            .whereEqualTo("rol", "vendedor")
            .get()
            .addOnSuccessListener { documentos ->
                txtTotalVendedores.text = documentos.size().toString()
            }

        db.collection("productos")
            .get()
            .addOnSuccessListener { documentos ->
                txtTotalProductos.text = documentos.size().toString()
            }

        val anioActual = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        db.collection("ventas")
            .get()
            .addOnSuccessListener { documentos ->
                var totalAnio = 0.0
                val formatoFecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

                for (documento in documentos) {
                    try {
                        val fecha = formatoFecha.parse(documento.getString("fecha") ?: "")
                        if (fecha != null) {
                            val cal = java.util.Calendar.getInstance()
                            cal.time = fecha
                            val anio = cal.get(java.util.Calendar.YEAR)

                            if (anio == anioActual) {
                                totalAnio += documento.getDouble("total_venta") ?: 0.0
                            }
                        }
                    } catch (e: Exception) {
                    }
                }

                txtVentasAnio.text = String.format("$%,.0f", totalAnio)
            }
    }

    private fun mostrarDialogoEditarPerfil() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialogo_editar_perfil_gerente, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val editCorreo = dialogView.findViewById<EditText>(R.id.editCorreo)
        val editTelefono = dialogView.findViewById<EditText>(R.id.editTelefono)
        val editContrasena = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editContrasena)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)

        editNombre.setText(SessionManager.getNombre(requireContext()))
        editCorreo.setText(SessionManager.getCorreo(requireContext()))
        editTelefono.setText(SessionManager.getTelefono(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val correo = editCorreo.text.toString().trim()
            val telefono = editTelefono.text.toString().trim()
            val nuevaContrasena = editContrasena.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datosActualizados = hashMapOf<String, Any>(
                "nombre" to nombre,
                "correo" to correo,
                "telefono" to telefono
            )

            if (nuevaContrasena.isNotEmpty()) {
                if (nuevaContrasena.length < 6) {
                    Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val passHasheada = hashPassword(nuevaContrasena)
                datosActualizados["contrasena"] = passHasheada
            }

            val userId = SessionManager.getUserId(requireContext())
            db.collection("usuarios")
                .document(userId)
                .update(datosActualizados)
                .addOnSuccessListener {
                    SessionManager.saveSession(
                        context = requireContext(),
                        id = userId,
                        nombre = nombre,
                        correo = correo,
                        rol = "gerente",
                        telefono = telefono
                    )

                    Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()

                    view?.let { v ->
                        v.findViewById<TextView>(R.id.txt_nombre_usuario).text = nombre
                        v.findViewById<TextView>(R.id.txt_correo_usuario).text = correo
                        v.findViewById<TextView>(R.id.txt_telefono_usuario).text = telefono
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}