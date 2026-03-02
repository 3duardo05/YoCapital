package com.example.yocapital.vendedor.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R

class VentaAdapter(private val items: List<VentaItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_VENTA = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMesAnio: TextView = view.findViewById(R.id.txtMesAnio)
    }

    class VentaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtProducto: TextView = view.findViewById(R.id.txtNombreProducto)
        val txtCliente: TextView = view.findViewById(R.id.txtCliente)
        val txtComision: TextView = view.findViewById(R.id.txtMontoComision)
        val txtFechaCorta: TextView = view.findViewById(R.id.txtCliente2)
        val imgIcono: ImageView = view.findViewById(R.id.imgIconoVenta)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is VentaItem.Header -> VIEW_TYPE_HEADER
            is VentaItem.VentaData -> VIEW_TYPE_VENTA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.mes_separador, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_VENTA -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_venta, parent, false)
                VentaViewHolder(view)
            }
            else -> throw IllegalArgumentException("Tipo de vista desconocido: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is VentaItem.Header -> {
                (holder as HeaderViewHolder).txtMesAnio.text = item.mesAnio
            }
            is VentaItem.VentaData -> {
                val ventaHolder = holder as VentaViewHolder
                val venta = item.venta

                ventaHolder.txtProducto.text = venta.producto
                ventaHolder.txtCliente.text = venta.cliente
                ventaHolder.txtComision.text = String.format("$%,.2f", venta.comision_vendedor)

                if (venta.fecha.isNotEmpty()) {
                    try {
                        val partes = venta.fecha.split("/")
                        if (partes.size >= 2) {
                            val dia = partes[0]
                            val mesNum = partes[1].toInt()
                            val meses = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

                            ventaHolder.txtFechaCorta.text = "$dia ${meses[mesNum - 1]}"
                        }
                    } catch (e: Exception) {
                        ventaHolder.txtFechaCorta.text = venta.fecha
                    }
                }

                val pro = venta.producto.lowercase()
                when {
                    pro.contains("moto") || pro.contains("yamaha") -> {
                        ventaHolder.imgIcono.setImageResource(R.drawable.home_icon_24)
                    }
                    pro.contains("auto") || pro.contains("camion") || pro.contains("lote") -> {
                        ventaHolder.imgIcono.setImageResource(R.drawable.home_icon_24)
                    }
                    else -> {
                        ventaHolder.imgIcono.setImageResource(R.drawable.home_icon_24)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}