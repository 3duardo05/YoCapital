package com.example.yocapital.gerente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.Producto
import com.example.yocapital.R

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onEditarClick: (Producto) -> Unit,
    private val onEliminarClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    inner class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvComision: TextView = view.findViewById(R.id.tvComision)
        val tvId: TextView = view.findViewById(R.id.tvId)
        val btnEditar: ImageView = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageView = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]

        holder.tvNombre.text = producto.nombre
        holder.tvPrecio.text = String.format("$%,.2f", producto.precio)
        holder.tvStock.text = "${producto.stock} unidades"
        holder.tvComision.text = "${producto.comision.toInt()}%"
        holder.tvId.text = "#${producto.id.take(6)}"

        holder.btnEditar.setOnClickListener {
            onEditarClick(producto)
        }

        holder.btnEliminar.setOnClickListener {
            onEliminarClick(producto)
        }
    }

    override fun getItemCount(): Int = productos.size

    fun actualizarLista(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }
}