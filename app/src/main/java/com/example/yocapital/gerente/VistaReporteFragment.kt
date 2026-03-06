package com.example.yocapital.gerente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yocapital.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.example.yocapital.utils.GeneradorPDF

class VistaReporteFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vista_previa_reporte, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCerrar = view.findViewById<ImageButton>(R.id.btn_cerrar)
        val btnDescargar = view.findViewById<ImageButton>(R.id.btn_descargar_desde_vista)
        val txtTitulo = view.findViewById<TextView>(R.id.txt_titulo_reporte)

        // Configurar título con mes y año actual
        val calendario = Calendar.getInstance()
        val nombresMeses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val mesActual = nombresMeses[calendario.get(Calendar.MONTH)]
        val anioActual = calendario.get(Calendar.YEAR)
        txtTitulo.text = "Reporte $mesActual $anioActual"

        if (arguments?.getBoolean("descargar_automatico") == true) {
            generarPDF()
        }

        // Botón cerrar
        btnCerrar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Botón descargar desde vista previa
        btnDescargar.setOnClickListener {
            generarPDF()
        }

        // Cargar datos del reporte
        cargarDatosReporte(view)
    }

    private fun cargarDatosReporte(view: View) {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH)
        val anioActual = calendario.get(Calendar.YEAR)

        // Referencias a los TextViews
        val txtTotalVentas = view.findViewById<TextView>(R.id.txt_total_ventas)
        val txtTotalTransacciones = view.findViewById<TextView>(R.id.txt_total_transacciones)
        val txtPromedioVenta = view.findViewById<TextView>(R.id.txt_promedio_venta)
        val txtTotalComisiones = view.findViewById<TextView>(R.id.txt_total_comisiones)
        val txtMesAnterior = view.findViewById<TextView>(R.id.txt_mes_anterior)
        val txtCrecimiento = view.findViewById<TextView>(R.id.txt_crecimiento)
        val rvRanking = view.findViewById<RecyclerView>(R.id.rv_ranking_reporte)
        val rvProductos = view.findViewById<RecyclerView>(R.id.rv_productos_reporte)

        rvRanking.layoutManager = LinearLayoutManager(requireContext())
        rvProductos.layoutManager = LinearLayoutManager(requireContext())

        // Obtener ventas del mes actual
        db.collection("ventas")
            .get()
            .addOnSuccessListener { documentos ->
                var totalVentas = 0.0
                var totalComisiones = 0.0
                var cantidadTransacciones = 0
                var ventasMesAnterior = 0.0
                val ventasPorVendedor = mutableMapOf<String, Double>()
                val productosMasVendidos = mutableMapOf<String, Int>()

                for (documento in documentos) {
                    try {
                        val fecha = formatoFecha.parse(documento.getString("fecha") ?: "")
                        if (fecha != null) {
                            val cal = Calendar.getInstance()
                            cal.time = fecha
                            val mes = cal.get(Calendar.MONTH)
                            val anio = cal.get(Calendar.YEAR)

                            // Ventas del mes actual
                            if (mes == mesActual && anio == anioActual) {
                                val total = documento.getDouble("total_venta") ?: 0.0
                                val comision = when (val c = documento.get("comision_vendedor")) {
                                    is Number -> c.toDouble()
                                    is String -> c.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }
                                val vendedorId = documento.getString("vendedor_id") ?: ""
                                val producto = documento.getString("producto") ?: ""
                                val cantidad = documento.getLong("cantidad")?.toInt() ?: 1

                                totalVentas += total
                                totalComisiones += comision
                                cantidadTransacciones++

                                ventasPorVendedor[vendedorId] =
                                    ventasPorVendedor.getOrDefault(vendedorId, 0.0) + total

                                productosMasVendidos[producto] =
                                    productosMasVendidos.getOrDefault(producto, 0) + cantidad
                            }

                            // Ventas del mes anterior
                            val mesAnterior = if (mesActual == 0) 11 else mesActual - 1
                            val anioMesAnterior = if (mesActual == 0) anioActual - 1 else anioActual
                            if (mes == mesAnterior && anio == anioMesAnterior) {
                                ventasMesAnterior += documento.getDouble("total_venta") ?: 0.0
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar
                    }
                }

                // Actualizar resumen ejecutivo
                txtTotalVentas.text = String.format("$%,.2f", totalVentas)
                txtTotalTransacciones.text = cantidadTransacciones.toString()
                val promedio = if (cantidadTransacciones > 0) totalVentas / cantidadTransacciones else 0.0
                txtPromedioVenta.text = String.format("$%,.2f", promedio)
                txtTotalComisiones.text = String.format("$%,.2f", totalComisiones)

                // Actualizar comparativa
                txtMesAnterior.text = String.format("$%,.2f", ventasMesAnterior)
                val crecimiento = if (ventasMesAnterior > 0) {
                    ((totalVentas - ventasMesAnterior) / ventasMesAnterior) * 100
                } else if (totalVentas > 0) {
                    100.0
                } else {
                    0.0
                }

                val simbolo = when {
                    crecimiento > 0 -> "▲"
                    crecimiento < 0 -> "▼"
                    else -> "➡"
                }
                val color = when {
                    crecimiento > 0 -> 0xFF00D09E.toInt()
                    crecimiento < 0 -> 0xFFFF3B30.toInt()
                    else -> 0xFF999999.toInt()
                }
                txtCrecimiento.text = String.format("$simbolo %+.1f%%", crecimiento)
                txtCrecimiento.setTextColor(color)

                // Cargar ranking de vendedores
                cargarRanking(rvRanking, ventasPorVendedor)

                // Cargar productos más vendidos
                cargarProductos(rvProductos, productosMasVendidos)
            }
    }

    private fun cargarRanking(recyclerView: RecyclerView, ventasPorVendedor: Map<String, Double>) {
        val ranking = ventasPorVendedor.entries
            .sortedByDescending { it.value }
            .take(5)

        val listaRanking = mutableListOf<VendedorRankingReporte>()

        if (ranking.isEmpty()) {
            // Mostrar mensaje de "sin datos"
            return
        }

        for ((index, entrada) in ranking.withIndex()) {
            db.collection("usuarios")
                .document(entrada.key)
                .get()
                .addOnSuccessListener { documento ->
                    val nombre = documento.getString("nombre") ?: "Vendedor"
                    listaRanking.add(
                        VendedorRankingReporte(
                            posicion = index + 1,
                            nombre = nombre,
                            ventas = entrada.value
                        )
                    )

                    if (listaRanking.size == ranking.size) {
                        recyclerView.adapter = RankingReporteAdapter(listaRanking)
                    }
                }
        }
    }

    private fun cargarProductos(recyclerView: RecyclerView, productos: Map<String, Int>) {
        val listaProductos = productos.entries
            .sortedByDescending { it.value }
            .take(5)
            .mapIndexed { index, entrada ->
                ProductoReporte(
                    posicion = index + 1,
                    nombre = entrada.key,
                    cantidad = entrada.value
                )
            }

        recyclerView.adapter = ProductosReporteAdapter(listaProductos)
    }

    // ✅ NUEVO: Generar y descargar PDF
    private fun generarPDF() {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH)
        val anioActual = calendario.get(Calendar.YEAR)

        val nombresMeses = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val nombreMes = nombresMeses[mesActual]

        // Obtener ventas del mes actual
        db.collection("ventas")
            .get()
            .addOnSuccessListener { documentos ->
                var totalVentas = 0.0
                var totalComisiones = 0.0
                var cantidadTransacciones = 0
                var ventasMesAnterior = 0.0
                val ventasPorVendedor = mutableMapOf<String, Double>()
                val productosMasVendidos = mutableMapOf<String, Int>()

                for (documento in documentos) {
                    try {
                        val fecha = formatoFecha.parse(documento.getString("fecha") ?: "")
                        if (fecha != null) {
                            val cal = Calendar.getInstance()
                            cal.time = fecha
                            val mes = cal.get(Calendar.MONTH)
                            val anio = cal.get(Calendar.YEAR)

                            // Ventas del mes actual
                            if (mes == mesActual && anio == anioActual) {
                                val total = documento.getDouble("total_venta") ?: 0.0
                                val comision = when (val c = documento.get("comision_vendedor")) {
                                    is Number -> c.toDouble()
                                    is String -> c.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }
                                val vendedorId = documento.getString("vendedor_id") ?: ""
                                val producto = documento.getString("producto") ?: ""
                                val cantidad = documento.getLong("cantidad")?.toInt() ?: 1

                                totalVentas += total
                                totalComisiones += comision
                                cantidadTransacciones++

                                ventasPorVendedor[vendedorId] =
                                    ventasPorVendedor.getOrDefault(vendedorId, 0.0) + total

                                productosMasVendidos[producto] =
                                    productosMasVendidos.getOrDefault(producto, 0) + cantidad
                            }

                            // Ventas del mes anterior
                            val mesAnterior = if (mesActual == 0) 11 else mesActual - 1
                            val anioMesAnterior = if (mesActual == 0) anioActual - 1 else anioActual
                            if (mes == mesAnterior && anio == anioMesAnterior) {
                                ventasMesAnterior += documento.getDouble("total_venta") ?: 0.0
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar
                    }
                }

                // Calcular crecimiento
                val crecimiento = if (ventasMesAnterior > 0) {
                    ((totalVentas - ventasMesAnterior) / ventasMesAnterior) * 100
                } else if (totalVentas > 0) {
                    100.0
                } else {
                    0.0
                }

                // Calcular promedio
                val promedioVenta = if (cantidadTransacciones > 0) {
                    totalVentas / cantidadTransacciones
                } else {
                    0.0
                }

                // Preparar ranking
                val ranking = ventasPorVendedor.entries
                    .sortedByDescending { it.value }
                    .take(5)

                val listaRankingPDF = mutableListOf<GeneradorPDF.VendedorRanking>()

                if (ranking.isEmpty()) {
                    // Sin datos, generar PDF vacío
                    generarPDFConDatos(
                        nombreMes, anioActual, totalVentas, cantidadTransacciones,
                        promedioVenta, totalComisiones, ventasMesAnterior, crecimiento,
                        emptyList(), emptyList()
                    )
                    return@addOnSuccessListener
                }

                // Obtener nombres de vendedores
                var vendedoresObtenidos = 0
                for ((index, entrada) in ranking.withIndex()) {
                    db.collection("usuarios")
                        .document(entrada.key)
                        .get()
                        .addOnSuccessListener { documento ->
                            val nombre = documento.getString("nombre") ?: "Vendedor"
                            listaRankingPDF.add(
                                GeneradorPDF.VendedorRanking(
                                    posicion = index + 1,
                                    nombre = nombre,
                                    ventas = entrada.value
                                )
                            )

                            vendedoresObtenidos++
                            if (vendedoresObtenidos == ranking.size) {
                                // Ya tenemos todos los vendedores, preparar productos
                                val listaProductosPDF = productosMasVendidos.entries
                                    .sortedByDescending { it.value }
                                    .take(5)
                                    .mapIndexed { idx, prod ->
                                        GeneradorPDF.ProductoVendido(
                                            posicion = idx + 1,
                                            nombre = prod.key,
                                            cantidad = prod.value
                                        )
                                    }

                                // Generar PDF
                                generarPDFConDatos(
                                    nombreMes, anioActual, totalVentas, cantidadTransacciones,
                                    promedioVenta, totalComisiones, ventasMesAnterior, crecimiento,
                                    listaRankingPDF.sortedBy { it.posicion },
                                    listaProductosPDF
                                )
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generarPDFConDatos(
        mes: String,
        anio: Int,
        totalVentas: Double,
        transacciones: Int,
        promedioVenta: Double,
        totalComisiones: Double,
        mesAnterior: Double,
        crecimiento: Double,
        ranking: List<GeneradorPDF.VendedorRanking>,
        productos: List<GeneradorPDF.ProductoVendido>
    ) {
        val datos = GeneradorPDF.DatosReporte(
            mes = mes,
            anio = anio,
            totalVentas = totalVentas,
            transacciones = transacciones,
            promedioVenta = promedioVenta,
            totalComisiones = totalComisiones,
            mesAnterior = mesAnterior,
            crecimiento = crecimiento,
            ranking = ranking,
            productos = productos
        )

        GeneradorPDF.generarReporteMensual(requireContext(), datos)
    }

    // Data classes
    data class VendedorRankingReporte(val posicion: Int, val nombre: String, val ventas: Double)
    data class ProductoReporte(val posicion: Int, val nombre: String, val cantidad: Int)
}