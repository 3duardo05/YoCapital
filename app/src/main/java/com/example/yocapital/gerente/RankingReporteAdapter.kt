package com.example.yocapital.gerente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R

class RankingReporteAdapter(
    private val ranking: List<VistaReporteFragment.VendedorRankingReporte>
) : RecyclerView.Adapter<RankingReporteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtPosicion: TextView = view.findViewById(R.id.txt_posicion_ranking)
        val txtNombre: TextView = view.findViewById(R.id.txt_nombre_ranking)
        val txtVentas: TextView = view.findViewById(R.id.txt_ventas_ranking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_reporte, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ranking[position]

        holder.txtPosicion.text = "${item.posicion}."
        holder.txtNombre.text = item.nombre
        holder.txtVentas.text = String.format("$%,.2f", item.ventas)

        val color = when (item.posicion) {
            1 -> 0xFFFFD700.toInt()
            2 -> 0xFFC0C0C0.toInt()
            3 -> 0xFFCD7F32.toInt()
            else -> 0xFF666666.toInt()
        }
        holder.txtPosicion.setTextColor(color)
    }

    override fun getItemCount() = ranking.size
}