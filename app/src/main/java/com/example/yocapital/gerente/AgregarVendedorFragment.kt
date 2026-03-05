package com.example.yocapital.gerente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.example.yocapital.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class AgregarVendedorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

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
        val inputTelefono = view.findViewById<EditText>(R.id.input_telefono_vendedor)
        val btnGuardar = view.findViewById<Button>(R.id.btn_GuardarVendedor)

        btnGuardar.setOnClickListener {
            val nombre = inputNombre.text.toString()
            val correo = inputCorreo.text.toString()
            val telefono = inputTelefono.text.toString()

            if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos, por favor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generamos el ID y armamos el paquete de datos del vendedor
            val idGenerado = db.collection("usuarios").document().id
            val nuevoVendedor = Usuario(idGenerado, nombre, correo, telefono, "vendedor")

            // Lo mandamos a la colección "usuarios" en Firebase
            db.collection("usuarios").document(idGenerado).set(nuevoVendedor)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "¡Vendedor guardado al cien!", Toast.LENGTH_SHORT).show()
                    inputNombre.text.clear()
                    inputCorreo.text.clear()
                    inputTelefono.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}