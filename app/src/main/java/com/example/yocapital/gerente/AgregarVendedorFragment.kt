package com.example.yocapital.gerente

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class AgregarVendedorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var fechaSeleccionada: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputNombre = view.findViewById<EditText>(R.id.input_nombre_vendedor)
        val inputCorreo = view.findViewById<EditText>(R.id.input_correo_vendedor)
        val inputContrasena = view.findViewById<EditText>(R.id.input_contrasena)
        val inputTelefono = view.findViewById<EditText>(R.id.input_telefono_vendedor)
        val inputFechaRegistro = view.findViewById<TextInputEditText>(R.id.input_fecha_registro)
        val btnGuardar = view.findViewById<Button>(R.id.btn_GuardarVendedor)

        val fechaActual = Calendar.getInstance()
        fechaSeleccionada = fechaActual.time
        inputFechaRegistro.setText(formatearFecha(fechaSeleccionada!!))

        inputFechaRegistro.setOnClickListener {
            mostrarDatePicker(inputFechaRegistro)
        }

        btnGuardar.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val correo = inputCorreo.text.toString().trim()
            val contrasena = inputContrasena.text.toString().trim()
            val telefono = inputTelefono.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos, por favor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaSeleccionada == null) {
                Toast.makeText(requireContext(), "Selecciona una fecha de registro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val passHasheada = hashPassword(contrasena)

            val vendedorMap = hashMapOf(
                "nombre" to nombre,
                "correo" to correo,
                "contrasena" to passHasheada,
                "telefono" to telefono,
                "rol" to "vendedor",
                "fechaRegistro" to Timestamp(fechaSeleccionada!!)
            )

            db.collection("usuarios")
                .add(vendedorMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "¡Vendedor registrado exitosamente!", Toast.LENGTH_SHORT).show()

                    inputNombre.text?.clear()
                    inputCorreo.text?.clear()
                    inputContrasena.text?.clear()
                    inputTelefono.text?.clear()

                    fechaSeleccionada = Calendar.getInstance().time
                    inputFechaRegistro.setText(formatearFecha(fechaSeleccionada!!))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarDatePicker(inputFecha: TextInputEditText) {
        val calendario = Calendar.getInstance()
        if (fechaSeleccionada != null) {
            calendario.time = fechaSeleccionada!!
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendario.set(year, month, dayOfMonth)
                fechaSeleccionada = calendario.time
                inputFecha.setText(formatearFecha(fechaSeleccionada!!))
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun formatearFecha(fecha: Date): String {
        val formato = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())
        return formato.format(fecha)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}