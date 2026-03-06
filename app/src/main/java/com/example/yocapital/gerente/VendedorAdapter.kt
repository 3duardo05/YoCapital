package com.example.yocapital.gerente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R

data class Vendedor(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: String = "vendedor"
)

class VendedorAdapter(
    private var vendedores: List<Vendedor>,
    private val onEditarClick: (Vendedor) -> Unit,
    private val onEliminarClick: (Vendedor) -> Unit
) : RecyclerView.Adapter<VendedorAdapter.VendedorViewHolder>() {

    inner class VendedorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvRol: TextView = view.findViewById(R.id.tvRol)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreo)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageView = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendedorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vendedor, parent, false)
        return VendedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendedorViewHolder, position: Int) {
        val vendedor = vendedores[position]

        holder.tvNombre.text = vendedor.nombre
        holder.tvRol.text = vendedor.rol.capitalize()
        holder.tvCorreo.text = vendedor.correo
        holder.tvTelefono.text = vendedor.telefono

        holder.btnEditar.setOnClickListener {
            onEditarClick(vendedor)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(vendedor)
        }
    }

    override fun getItemCount(): Int = vendedores.size

    fun actualizarLista(nuevaLista: List<Vendedor>) {
        vendedores = nuevaLista
        notifyDataSetChanged()
    }
}