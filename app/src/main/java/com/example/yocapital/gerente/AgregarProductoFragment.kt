package com.example.yocapital.gerente // Asegúrate de que esta ruta sea correcta según donde creaste el archivo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.example.yocapital.Producto // Importa tu Data Class Producto
import com.google.firebase.firestore.FirebaseFirestore

class AgregarProductoFragment : Fragment() {

    // 1. Preparamos la conexión a nuestra base de datos en Firebase (Firestore)
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 2. Aquí conectamos este código Kotlin con el diseño XML que hicimos antes
        return inflater.inflate(R.layout.fragment_agregar_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. "Enchufamos" los cuadros de texto y el botón del XML hacia Kotlin
        val inputNombre = view.findViewById<EditText>(R.id.input_nombre_producto)
        val inputPrecio = view.findViewById<EditText>(R.id.input_precio_producto)
        val inputStock = view.findViewById<EditText>(R.id.input_stock_producto)
        val btnGuardar = view.findViewById<Button>(R.id.btn_GuardarProducto)

        // 4. Le decimos al botón qué hacer cuando lo presionen
        btnGuardar.setOnClickListener {

            // Extraemos el texto que escribió el usuario
            val nombre = inputNombre.text.toString()
            val precioTexto = inputPrecio.text.toString()
            val stockTexto = inputStock.text.toString()

            // Validamos que no haya dejado campos en blanco
            if (nombre.isEmpty() || precioTexto.isEmpty() || stockTexto.isEmpty()) {
                // Toast es el mensajito flotante que sale abajo en el celular
                Toast.makeText(requireContext(), "Llena todos los campos, por favor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Detenemos el código aquí para que no marque error
            }

            // Convertimos el texto del precio a Decimal (Double) y el stock a Entero (Int)
            val precio = precioTexto.toDouble()
            val stock = stockTexto.toInt()

            // 5. Creamos el "paquete" con nuestra tabla/Data Class
            // Generamos un ID único automático para este producto
            val idGenerado = db.collection("productos").document().id
            val nuevoProducto = Producto(idGenerado, nombre, precio, stock)

            // 6. Mandamos el paquete a la colección "Productos" en Firebase
            db.collection("productos").document(idGenerado).set(nuevoProducto)
                .addOnSuccessListener {

                    Toast.makeText(requireContext(), "¡Producto guardado al cien!", Toast.LENGTH_SHORT).show()

                    // Limpiamos los cuadros de texto para que pueda agregar otro
                    inputNombre.text.clear()
                    inputPrecio.text.clear()
                    inputStock.text.clear()
                }
                .addOnFailureListener { e ->
                    // Si algo falla, mostramos el error
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}