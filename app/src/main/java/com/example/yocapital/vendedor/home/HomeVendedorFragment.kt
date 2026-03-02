package com.example.yocapital.vendedor.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeVendedorFragment : Fragment() {

    private lateinit var viewModel: VentasViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(VentasViewModel::class.java)

        val txtSaludo = view.findViewById<TextView>(R.id.txtSaludo)
        val tvTotalComisiones = view.findViewById<TextView>(R.id.total_comisiones)
        val tvTotalVentas = view.findViewById<TextView>(R.id.total_ventas)
        val rvVentas = view.findViewById<RecyclerView>(R.id.rvVentasRecientes)

        rvVentas.layoutManager = LinearLayoutManager(requireContext())

        val nombreUsuario = SessionManager.getNombre(requireContext())
        val idVendedorActual = SessionManager.getUserId(requireContext())
        txtSaludo.text = "Hola, $nombreUsuario"

        viewModel.cargarVentas(idVendedorActual)

        viewModel.listaVentas.observe(viewLifecycleOwner) { ventas ->
            actualizarUI(ventas, tvTotalComisiones, tvTotalVentas, rvVentas)
        }
    }

    private fun actualizarUI(
        ventas: List<Venta>,
        tvComisiones: TextView,
        tvTotales: TextView,
        rv: RecyclerView
    ) {
        val calendario = Calendar.getInstance()
        val filtroMes = String.format("/%02d/%04d", calendario.get(Calendar.MONTH) + 1, calendario.get(Calendar.YEAR))

        var sumaComisiones = 0.0
        var sumaVentas = 0.0

        val listaOrdenada = ventas.sortedByDescending { venta ->
            try {
                val partes = venta.fecha.split("/")
                if (partes.size == 3) {
                    val dia = partes[0].toInt()
                    val mes = partes[1].toInt()
                    val anio = partes[2].toInt()
                    anio * 10000 + mes * 100 + dia
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        }

        for (venta in listaOrdenada) {
            if (venta.fecha.endsWith(filtroMes)) {
                sumaComisiones += venta.comision_vendedor
                sumaVentas += venta.total_venta
            }
        }

        tvComisiones.text = String.format("$%,.2f", sumaComisiones)
        tvTotales.text = String.format("$%,.2f", sumaVentas)

        val itemsConHeaders = agruparVentasPorMes(listaOrdenada)
        rv.adapter = VentaAdapter(itemsConHeaders)
    }

    private fun agruparVentasPorMes(ventas: List<Venta>): List<VentaItem> {
        val resultado = mutableListOf<VentaItem>()
        var mesAnioAnterior = ""

        val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")

        for (venta in ventas) {
            try {
                val partes = venta.fecha.split("/")
                if (partes.size >= 3) {
                    val mesNum = partes[1].toInt()
                    val anio = partes[2]
                    val mesAnioActual = "${meses[mesNum - 1]} $anio"

                    if (mesAnioActual != mesAnioAnterior) {
                        resultado.add(VentaItem.Header(mesAnioActual))
                        mesAnioAnterior = mesAnioActual
                    }

                    resultado.add(VentaItem.VentaData(venta))
                }
            } catch (e: Exception) {
                Log.e("HomeVendedor", "Error al parsear fecha: ${venta.fecha}", e)
                resultado.add(VentaItem.VentaData(venta))
            }
        }

        return resultado
    }
}