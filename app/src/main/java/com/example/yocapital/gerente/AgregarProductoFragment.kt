package com.example.yocapital.gerente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yocapital.Producto
import com.example.yocapital.R
import com.google.firebase.firestore.FirebaseFirestore

class AgregarProductoFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputNombre = view.findViewById<EditText>(R.id.input_nombre_producto)
        val inputPrecio = view.findViewById<EditText>(R.id.input_precio_producto)
        val inputStock = view.findViewById<EditText>(R.id.input_stock_producto)
        val inputComision = view.findViewById<EditText>(R.id.input_comision_producto)
        val btnGuardar = view.findViewById<Button>(R.id.btn_GuardarProducto)

        btnGuardar.setOnClickListener {
            val nombre = inputNombre.text.toString().trim()
            val precioTexto = inputPrecio.text.toString().trim()
            val stockTexto = inputStock.text.toString().trim()
            val comisionTexto = inputComision.text.toString().trim()

            if (nombre.isEmpty() || precioTexto.isEmpty() || stockTexto.isEmpty() || comisionTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos, por favor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = precioTexto.toDoubleOrNull()
            val stock = stockTexto.toIntOrNull()
            val comision = comisionTexto.toDoubleOrNull()

            if (precio == null || precio <= 0) {
                Toast.makeText(requireContext(), "Ingresa un precio válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (stock == null || stock < 0) {
                Toast.makeText(requireContext(), "Ingresa un stock válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (comision == null || comision < 0 || comision > 100) {
                Toast.makeText(requireContext(), "La comisión debe estar entre 0 y 100%", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idGenerado = db.collection("productos").document().id
            val nuevoProducto = Producto(idGenerado, nombre, precio, stock, comision)

            db.collection("productos").document(idGenerado).set(nuevoProducto)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "¡Producto guardado exitosamente!", Toast.LENGTH_SHORT).show()

                    inputNombre.text?.clear()
                    inputPrecio.text?.clear()
                    inputStock.text?.clear()
                    inputComision.text?.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}