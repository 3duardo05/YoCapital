package com.example.yocapital.gerente

import android.annotation.SuppressLint
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
import com.example.yocapital.R
import com.google.firebase.firestore.FirebaseFirestore

class GestionarVendedoresFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutVacio: LinearLayout
    private lateinit var tvContador: TextView
    private lateinit var adapter: VendedorAdapter
    private val listaVendedores = mutableListOf<Vendedor>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestionar_vendedores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerVendedores)
        layoutVacio = view.findViewById(R.id.layoutVacio)
        tvContador = view.findViewById(R.id.tvContador)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = VendedorAdapter(
            listaVendedores,
            onEditarClick = { vendedor -> editarVendedor(vendedor) },
            onEliminarClick = { vendedor -> confirmarEliminar(vendedor) }
        )

        recyclerView.adapter = adapter

        cargarVendedores()
    }

    @SuppressLint("SetTextI18n")
    private fun cargarVendedores() {
        db.collection("usuarios")
            .whereEqualTo("rol", "vendedor")
            .get()
            .addOnSuccessListener { documentos ->
                listaVendedores.clear()

                for (documento in documentos) {
                    val vendedor = Vendedor(
                        id = documento.id,
                        nombre = documento.getString("nombre") ?: "",
                        correo = documento.getString("correo") ?: "",
                        telefono = documento.getString("telefono") ?: "",
                        rol = documento.getString("rol") ?: "vendedor"
                    )
                    listaVendedores.add(vendedor)
                }

                adapter.actualizarLista(listaVendedores)

                if (listaVendedores.isEmpty()) {
                    layoutVacio.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    tvContador.text = "0 vendedores"
                } else {
                    layoutVacio.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    tvContador.text = "${listaVendedores.size} vendedor${if (listaVendedores.size != 1) "es" else ""}"
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar vendedores", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editarVendedor(vendedor: Vendedor) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialogo_editar_vendedor, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val editCorreo = dialogView.findViewById<EditText>(R.id.editCorreo)
        val editTelefono = dialogView.findViewById<EditText>(R.id.editTelefono)
        val editContrasena = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editContrasena)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardar)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)

        editNombre.setText(vendedor.nombre)
        editCorreo.setText(vendedor.correo)
        editTelefono.setText(vendedor.telefono)

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

            val vendedorActualizado = hashMapOf<String, Any>(
                "nombre" to nombre,
                "correo" to correo,
                "telefono" to telefono,
                "rol" to "vendedor"
            )

            if (nuevaContrasena.isNotEmpty()) {
                if (nuevaContrasena.length < 6) {
                    Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val passHasheada = hashPassword(nuevaContrasena)
                vendedorActualizado["contrasena"] = passHasheada
            }

            db.collection("usuarios")
                .document(vendedor.id)
                .update(vendedorActualizado)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Vendedor actualizado", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    cargarVendedores()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun hashPassword(password: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun confirmarEliminar(vendedor: Vendedor) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar vendedor")
            .setMessage("¿Estás seguro de eliminar a '${vendedor.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarVendedor(vendedor)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarVendedor(vendedor: Vendedor) {
        db.collection("usuarios")
            .document(vendedor.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Vendedor eliminado", Toast.LENGTH_SHORT).show()
                cargarVendedores()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }
}