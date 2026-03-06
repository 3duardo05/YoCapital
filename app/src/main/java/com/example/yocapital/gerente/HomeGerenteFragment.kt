package com.example.yocapital.gerente

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import android.widget.Toast
import java.util.*

class HomeGerenteFragment : Fragment(), OnChartValueSelectedListener {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var barChart: BarChart
    private lateinit var btnMensual: Button
    private lateinit var btnAnual: Button
    private lateinit var tvVentasTotales: TextView
    private lateinit var tvPorcentaje: TextView
    private lateinit var rvRanking: RecyclerView
    private lateinit var txtSaludo: TextView

    private var vistaActual = "mensual"
    private var dialogoActual: androidx.appcompat.app.AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_gerente, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.barChart)
        btnMensual = view.findViewById(R.id.btnMensual)
        btnAnual = view.findViewById(R.id.btnAnual)
        tvVentasTotales = view.findViewById(R.id.tvVentasTotales)
        tvPorcentaje = view.findViewById(R.id.tvPorcentaje)
        rvRanking = view.findViewById(R.id.rvRanking)
        txtSaludo = view.findViewById(R.id.txtSaludo)

        val nombreGerente = SessionManager.getNombre(requireContext())
        txtSaludo.text = "Hola, $nombreGerente"

        rvRanking.layoutManager = LinearLayoutManager(requireContext())

        btnMensual.setOnClickListener {
            cambiarVista("mensual")
        }

        btnAnual.setOnClickListener {
            cambiarVista("anual")
        }

        cargarDatos()
    }

    private fun cambiarVista(vista: String) {
        vistaActual = vista

        if (vista == "mensual") {
            btnMensual.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.verde)
            btnMensual.setTextColor(Color.WHITE)
            btnAnual.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
            btnAnual.setTextColor(Color.GRAY)
        } else {
            btnAnual.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.verde)
            btnAnual.setTextColor(Color.WHITE)
            btnMensual.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
            btnMensual.setTextColor(Color.GRAY)
        }

        cargarDatos()
    }

    private fun cargarDatos() {
        db.collection("ventas")
            .get()
            .addOnSuccessListener { documentos ->
                val ventas = mutableListOf<Venta>()

                for (documento in documentos) {
                    val fecha = documento.getString("fecha") ?: ""
                    val total = documento.getDouble("total_venta") ?: 0.0
                    val vendedorId = documento.getString("vendedor_id") ?: ""

                    ventas.add(Venta(fecha, total, vendedorId))
                }

                procesarDatos(ventas)
            }
    }

    private fun procesarDatos(ventas: List<Venta>) {
        android.util.Log.d("HomeGerente", "🔄 procesarDatos() llamado con ${ventas.size} ventas")

        val totalVentas = ventas.sumOf { it.total }
        tvVentasTotales.text = String.format("$%,.2f", totalVentas)

        android.util.Log.d("HomeGerente", "💵 Total ventas calculado: $$totalVentas")

        calcularYMostrarCrecimiento(ventas)

        if (vistaActual == "mensual") {
            mostrarGraficaMensual(ventas)
        } else {
            mostrarGraficaAnual(ventas)
        }

        cargarRanking(ventas)
    }

    private fun calcularYMostrarCrecimiento(ventas: List<Venta>) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendario = Calendar.getInstance()

        val mesActual = calendario.get(Calendar.MONTH)
        val anioActual = calendario.get(Calendar.YEAR)

        calendario.add(Calendar.MONTH, -1)
        val mesAnterior = calendario.get(Calendar.MONTH)
        val anioMesAnterior = calendario.get(Calendar.YEAR)

        val nombresMeses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        android.util.Log.d("HomeGerente", "📅 Mes actual: ${nombresMeses[mesActual]} $anioActual")
        android.util.Log.d("HomeGerente", "📅 Mes anterior: ${nombresMeses[mesAnterior]} $anioMesAnterior")

        var ventasMesActual = 0.0
        var ventasMesAnterior = 0.0

        for (venta in ventas) {
            try {
                val fecha = formatoFecha.parse(venta.fecha)
                if (fecha != null) {
                    val cal = Calendar.getInstance()
                    cal.time = fecha
                    val mes = cal.get(Calendar.MONTH)
                    val anio = cal.get(Calendar.YEAR)

                    android.util.Log.d("HomeGerente", "📊 Venta: ${nombresMeses[mes]} $anio - $${venta.total}")

                    if (mes == mesActual && anio == anioActual) {
                        ventasMesActual += venta.total
                        android.util.Log.d("HomeGerente", "✅ Sumada a mes actual: $${venta.total}")
                    }

                    if (mes == mesAnterior && anio == anioMesAnterior) {
                        ventasMesAnterior += venta.total
                        android.util.Log.d("HomeGerente", "✅ Sumada a mes anterior: $${venta.total}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeGerente", "❌ Error parseando fecha: ${venta.fecha}")
            }
        }

        android.util.Log.d("HomeGerente", "💰 Total mes actual (${nombresMeses[mesActual]}): $$ventasMesActual")
        android.util.Log.d("HomeGerente", "💰 Total mes anterior (${nombresMeses[mesAnterior]}): $$ventasMesAnterior")

        val porcentaje = if (ventasMesAnterior > 0) {
            ((ventasMesActual - ventasMesAnterior) / ventasMesAnterior) * 100
        } else {
            if (ventasMesActual > 0) 100.0 else 0.0
        }

        android.util.Log.d("HomeGerente", "📈 Porcentaje calculado: $porcentaje%")

        when {
            porcentaje > 0 -> {
                tvPorcentaje.text = String.format("▲ +%.1f%%", porcentaje)
                tvPorcentaje.setTextColor(0xFF00D09E.toInt())
                android.util.Log.d("HomeGerente", "🟢 Mostrando: ▲ +$porcentaje%")
            }
            porcentaje < 0 -> {
                tvPorcentaje.text = String.format("▼ %.1f%%", porcentaje)
                tvPorcentaje.setTextColor(0xFFFF3B30.toInt())
                android.util.Log.d("HomeGerente", "🔴 Mostrando: ▼ $porcentaje%")
            }
            else -> {
                tvPorcentaje.text = "➡ 0%"
                tvPorcentaje.setTextColor(0xFF999999.toInt())
                android.util.Log.d("HomeGerente", "⚪ Mostrando: ➡ 0%")
            }
        }
    }

    private fun mostrarGraficaMensual(ventas: List<Venta>) {
        val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        val ventasPorMes = MutableList(12) { 0.0 }

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)

        for (venta in ventas) {
            try {
                val fecha = formatoFecha.parse(venta.fecha)
                if (fecha != null) {
                    val calendario = Calendar.getInstance()
                    calendario.time = fecha
                    val anio = calendario.get(Calendar.YEAR)
                    val mes = calendario.get(Calendar.MONTH)

                    if (anio == anioActual) {
                        ventasPorMes[mes] += venta.total
                    }
                }
            } catch (e: Exception) {
            }
        }

        val entries = mutableListOf<BarEntry>()
        for (i in 0 until 12) {
            entries.add(BarEntry(i.toFloat(), ventasPorMes[i].toFloat()))
        }

        configurarGrafica(entries, meses)
    }

    private fun mostrarGraficaAnual(ventas: List<Venta>) {
        val ventasPorAnio = mutableMapOf<Int, Double>()
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (venta in ventas) {
            try {
                val fecha = formatoFecha.parse(venta.fecha)
                if (fecha != null) {
                    val calendario = Calendar.getInstance()
                    calendario.time = fecha
                    val anio = calendario.get(Calendar.YEAR)
                    ventasPorAnio[anio] = ventasPorAnio.getOrDefault(anio, 0.0) + venta.total
                }
            } catch (e: Exception) {
            }
        }

        val anioActual = Calendar.getInstance().get(Calendar.YEAR)
        if (ventasPorAnio.size < 2) {
            for (i in 2 downTo 0) {
                val anio = anioActual - i
                if (!ventasPorAnio.containsKey(anio)) {
                    ventasPorAnio[anio] = 0.0
                }
            }
        }

        val aniosOrdenados = ventasPorAnio.keys.sorted()
        val entries = aniosOrdenados.mapIndexed { index, anio ->
            BarEntry(index.toFloat(), ventasPorAnio[anio]!!.toFloat())
        }

        val labels = aniosOrdenados.map { it.toString() }

        configurarGrafica(entries, labels)
    }

    private fun configurarGrafica(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Ventas").apply {
            color = ContextCompat.getColor(requireContext(), R.color.verde)
            valueTextColor = Color.BLACK
            valueTextSize = 10f
        }

        val barData = BarData(dataSet)
        barChart.data = barData

        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            animateY(1000)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textColor = Color.BLACK
            }

            axisLeft.apply {
                setDrawGridLines(true)
                textColor = Color.BLACK
            }

            axisRight.isEnabled = false

            setOnChartValueSelectedListener(this@HomeGerenteFragment)
        }

        barChart.invalidate()
    }


    override fun onNothingSelected() {
    }

    private fun cargarRanking(ventas: List<Venta>) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH)
        val anioActual = calendario.get(Calendar.YEAR)

        val ventasPorVendedor = mutableMapOf<String, Double>()

        for (venta in ventas) {
            try {
                val fecha = formatoFecha.parse(venta.fecha)
                if (fecha != null) {
                    val cal = Calendar.getInstance()
                    cal.time = fecha
                    val mes = cal.get(Calendar.MONTH)
                    val anio = cal.get(Calendar.YEAR)

                    if (mes == mesActual && anio == anioActual) {
                        ventasPorVendedor[venta.vendedorId] =
                            ventasPorVendedor.getOrDefault(venta.vendedorId, 0.0) + venta.total
                    }
                }
            } catch (e: Exception) {
            }
        }

        val ranking = ventasPorVendedor.entries
            .sortedByDescending { it.value }
            .take(5)

        val vendedoresRanking = mutableListOf<VendedorRanking>()

        val nombresMeses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val nombreMesActual = nombresMeses[mesActual]

        for ((vendedorId, total) in ranking) {
            db.collection("usuarios")
                .document(vendedorId)
                .get()
                .addOnSuccessListener { documento ->
                    val nombre = documento.getString("nombre") ?: "Vendedor"
                    vendedoresRanking.add(VendedorRanking(nombre, total, nombreMesActual))

                    if (vendedoresRanking.size == ranking.size) {
                        rvRanking.adapter = RankingAdapter(vendedoresRanking)
                    }
                }
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null) return

        dialogoActual?.dismiss()

        val index = e.x.toInt()
        val valor = e.y.toDouble()

        val chart = barChart
        val labels = (chart.xAxis.valueFormatter as? IndexAxisValueFormatter)?.values

        val label = if (labels != null && index < labels.size) {
            labels[index]
        } else {
            "Período"
        }

        if (vistaActual == "mensual") {
            mostrarDetalleMensual(label, valor, index)
        } else {
            mostrarDetalleAnual(label, valor)
        }
    }

    private fun mostrarDetalleMensual(mes: String, totalVentas: Double, mesIndex: Int) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val anioActual = Calendar.getInstance().get(Calendar.YEAR)

        var cantidadVentas = 0
        var ventaMasAlta = 0.0
        var mejorVendedor = ""
        val ventasPorVendedor = mutableMapOf<String, Double>()

        db.collection("ventas")
            .get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    try {
                        val fecha = formatoFecha.parse(documento.getString("fecha") ?: "")
                        if (fecha != null) {
                            val cal = Calendar.getInstance()
                            cal.time = fecha
                            val mes = cal.get(Calendar.MONTH)
                            val anio = cal.get(Calendar.YEAR)

                            if (mes == mesIndex && anio == anioActual) {
                                val total = documento.getDouble("total_venta") ?: 0.0
                                val vendedorId = documento.getString("vendedor_id") ?: ""

                                cantidadVentas++
                                if (total > ventaMasAlta) ventaMasAlta = total

                                ventasPorVendedor[vendedorId] =
                                    ventasPorVendedor.getOrDefault(vendedorId, 0.0) + total
                            }
                        }
                    } catch (e: Exception) {
                    }
                }

                val mejorVendedorId = ventasPorVendedor.maxByOrNull { it.value }?.key

                if (mejorVendedorId != null) {
                    db.collection("usuarios")
                        .document(mejorVendedorId)
                        .get()
                        .addOnSuccessListener { doc ->
                            mejorVendedor = doc.getString("nombre") ?: "Vendedor"

                            val promedioVenta = if (cantidadVentas > 0) totalVentas / cantidadVentas else 0.0

                            val mensaje = """
                            📊 $mes $anioActual
                            
                            💰 Total: ${String.format("$%,.2f", totalVentas)}
                            📈 Ventas realizadas: $cantidadVentas
                            📊 Promedio por venta: ${String.format("$%,.2f", promedioVenta)}
                            🏆 Mayor venta: ${String.format("$%,.2f", ventaMasAlta)}
                            ⭐ Mejor vendedor: $mejorVendedor
                        """.trimIndent()

                            mostrarDialogoDetalle("Detalles de $mes", mensaje)
                        }
                } else {
                    val mensaje = """
                    📊 $mes $anioActual
                    
                    💰 Total: ${String.format("$%,.2f", totalVentas)}
                    📈 No hay ventas registradas
                """.trimIndent()

                    mostrarDialogoDetalle("Detalles de $mes", mensaje)
                }
            }
    }

    private fun mostrarDetalleAnual(anio: String, totalVentas: Double) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val anioInt = anio.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

        var cantidadVentas = 0
        var ventaMasAlta = 0.0
        val ventasPorMes = MutableList(12) { 0 }

        db.collection("ventas")
            .get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    try {
                        val fecha = formatoFecha.parse(documento.getString("fecha") ?: "")
                        if (fecha != null) {
                            val cal = Calendar.getInstance()
                            cal.time = fecha
                            val anioVenta = cal.get(Calendar.YEAR)
                            val mes = cal.get(Calendar.MONTH)

                            if (anioVenta == anioInt) {
                                val total = documento.getDouble("total_venta") ?: 0.0
                                cantidadVentas++
                                if (total > ventaMasAlta) ventaMasAlta = total
                                ventasPorMes[mes]++
                            }
                        }
                    } catch (e: Exception) {
                    }
                }

                val mesesConVentas = ventasPorMes.count { it > 0 }
                val promedioMensual = if (mesesConVentas > 0) totalVentas / mesesConVentas else 0.0
                val promedioVenta = if (cantidadVentas > 0) totalVentas / cantidadVentas else 0.0

                val mensaje = """
                📊 Año $anio
                
                💰 Total anual: ${String.format("$%,.2f", totalVentas)}
                📈 Ventas realizadas: $cantidadVentas
                📅 Meses activos: $mesesConVentas/12
                📊 Promedio mensual: ${String.format("$%,.2f", promedioMensual)}
                💵 Promedio por venta: ${String.format("$%,.2f", promedioVenta)}
                🏆 Mayor venta: ${String.format("$%,.2f", ventaMasAlta)}
            """.trimIndent()

                mostrarDialogoDetalle("Detalles de $anio", mensaje)
            }
    }

    private fun mostrarDialogoDetalle(titulo: String, mensaje: String) {
        dialogoActual?.dismiss()

        dialogoActual = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
                dialogoActual = null
            }
            .setOnCancelListener {
                dialogoActual = null
            }
            .show()
    }

    data class Venta(val fecha: String, val total: Double, val vendedorId: String)
    data class VendedorRanking(val nombre: String, val ventas: Double, val periodo: String)
}