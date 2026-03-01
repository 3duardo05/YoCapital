package com.example.yocapital.vendedor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.yocapital.R
import com.google.firebase.firestore.FirebaseFirestore

class AgregarClienteFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_cliente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputNombre = view.findViewById<EditText>(R.id.input_nombre_cliente)
        val inputTelefono = view.findViewById<EditText>(R.id.input_telefono_cliente)
        val inputCorreo = view.findViewById<EditText>(R.id.input_correo_cliente)
        val btnGuardar = view.findViewById<Button>(R.id.btn_guardar_cliente)

        btnGuardar.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val telefono = inputTelefono.text.toString().trim()
            val correo = inputCorreo.text.toString().trim()

            if (nombre.isEmpty()) {
                inputNombre.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            val nuevoCliente = hashMapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "correo" to correo
            )

            db.collection("clientes")
                .add(nuevoCliente)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cliente guardado con éxito", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}