package com.example.yocapital.gerente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.Producto
import com.example.yocapital.R
import com.google.firebase.firestore.FirebaseFirestore

class GestionarProductosFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutVacio: LinearLayout
    private lateinit var tvContador: TextView
    private lateinit var adapter: ProductoAdapter
    private val listaProductos = mutableListOf<Producto>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestionar_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerProductos)
        layoutVacio = view.findViewById(R.id.layoutVacio)
        tvContador = view.findViewById(R.id.tvContador)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ProductoAdapter(
            listaProductos,
            onEditarClick = { producto -> editarProducto(producto) },
            onEliminarClick = { producto -> confirmarEliminar(producto) }
        )

        recyclerView.adapter = adapter

        cargarProductos()
    }

    private fun cargarProductos() {
        db.collection("productos")
            .get()
            .addOnSuccessListener { documentos ->
                listaProductos.clear()

                for (documento in documentos) {
                    val producto = documento.toObject(Producto::class.java).copy(
                        id = documento.id
                    )
                    listaProductos.add(producto)
                }

                adapter.actualizarLista(listaProductos)

                if (listaProductos.isEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    tvContador.text = "0 productos"
                } else {
                    layoutVacio.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    tvContador.text = "${listaProductos.size} producto${if (listaProductos.size != 1) "s" else ""}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar productos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editarProducto(producto: Producto) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialogo_editar_producto, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editarNombre)
        val editPrecio = dialogView.findViewById<EditText>(R.id.editarPrecio)
        val editStock = dialogView.findViewById<EditText>(R.id.editarStock)
        val editComision = dialogView.findViewById<EditText>(R.id.editarComision)
        val btnGuardar = dialogView.findViewById<Button>(R.id.boton_Guardar)
        val btnCancelar = dialogView.findViewById<Button>(R.id.boton_Cancelar)

        editNombre.setText(producto.nombre)
        editPrecio.setText(producto.precio.toString())
        editStock.setText(producto.stock.toString())
        editComision.setText(producto.comision.toString())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val precioTexto = editPrecio.text.toString().trim()
            val stockTexto = editStock.text.toString().trim()
            val comisionTexto = editComision.text.toString().trim()

            if (nombre.isEmpty() || precioTexto.isEmpty() || stockTexto.isEmpty() || comisionTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Llena todos los campos", Toast.LENGTH_SHORT).show()
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

            val productoActualizado = hashMapOf(
                "id" to producto.id,
                "nombre" to nombre,
                "precio" to precio,
                "stock" to stock,
                "comision" to comision
            )

            db.collection("productos")
                .document(producto.id)
                .set(productoActualizado)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    cargarProductos()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun confirmarEliminar(producto: Producto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de eliminar '${producto.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(producto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto(producto: Producto) {
        db.collection("productos")
            .document(producto.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                cargarProductos()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }
}