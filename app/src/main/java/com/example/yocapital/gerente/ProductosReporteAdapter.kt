package com.example.yocapital.gerente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R

class ProductosReporteAdapter(
    private val productos: List<VistaReporteFragment.ProductoReporte>
) : RecyclerView.Adapter<ProductosReporteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosicion: TextView = view.findViewById(R.id.txt_posicion_producto)
        val txtNombre: TextView = view.findViewById(R.id.txt_nombre_producto)
        val txtCantidad: TextView = view.findViewById(R.id.txt_cantidad_producto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_reporte, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = productos[position]

        holder.txtPosicion.text = "${item.posicion}."
        holder.txtNombre.text = item.nombre
        holder.txtCantidad.text = "${item.cantidad} uds"
    }

    override fun getItemCount() = productos.size
}