package com.example.yocapital.gerente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R

class RankingAdapter(
    private val vendedores: List<HomeGerenteFragment.VendedorRanking>
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreVendedor: TextView = view.findViewById(R.id.tvNombreVendedor)
        val tvPeriodo: TextView = view.findViewById(R.id.tvPeriodo)
        val tvVentas: TextView = view.findViewById(R.id.tvVentas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_vendedor, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val vendedor = vendedores[position]

        holder.tvNombreVendedor.text = vendedor.nombre
        holder.tvPeriodo.text = vendedor.periodo
        holder.tvVentas.text = String.format("$%,.2f", vendedor.ventas)
    }

    override fun getItemCount(): Int = vendedores.size
}