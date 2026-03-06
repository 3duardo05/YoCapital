package com.example.yocapital.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object GeneradorPDF {

    // Color verde YoCapital
    private val COLOR_VERDE = DeviceRgb(0, 208, 158)
    private val COLOR_GRIS = DeviceRgb(102, 102, 102)
    private val COLOR_NEGRO = DeviceRgb(5, 34, 36)

    data class DatosReporte(
        val mes: String,
        val anio: Int,
        val totalVentas: Double,
        val transacciones: Int,
        val promedioVenta: Double,
        val totalComisiones: Double,
        val mesAnterior: Double,
        val crecimiento: Double,
        val ranking: List<VendedorRanking>,
        val productos: List<ProductoVendido>
    )

    data class VendedorRanking(val posicion: Int, val nombre: String, val ventas: Double)
    data class ProductoVendido(val posicion: Int, val nombre: String, val cantidad: Int)

    fun generarReporteMensual(context: Context, datos: DatosReporte): Boolean {
        return try {
            // Crear nombre del archivo
            val nombreArchivo = "Reporte_${datos.mes}_${datos.anio}.pdf"

            // Crear archivo según versión de Android
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                crearArchivoMediaStore(context, nombreArchivo)
            } else {
                crearArchivoLegacy(nombreArchivo)
            }

            if (file == null) {
                Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
                return false
            }

            // Generar PDF
            val writer = PdfWriter(file)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // === PORTADA ===
            agregarPortada(document, datos)

            // === RESUMEN EJECUTIVO ===
            agregarResumenEjecutivo(document, datos)

            // === COMPARATIVA ===
            agregarComparativa(document, datos)

            // === RANKING ===
            agregarRanking(document, datos)

            // === PRODUCTOS ===
            agregarProductos(document, datos)

            // === PIE DE PÁGINA ===
            agregarPiePagina(document)

            document.close()

            Toast.makeText(context, "PDF descargado: $nombreArchivo", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun crearArchivoMediaStore(context: Context, nombreArchivo: String): FileOutputStream? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )

        return uri?.let { context.contentResolver.openOutputStream(it) as? FileOutputStream }
    }

    private fun crearArchivoLegacy(nombreArchivo: String): FileOutputStream? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, nombreArchivo)
        return FileOutputStream(file)
    }

    private fun agregarPortada(document: Document, datos: DatosReporte) {
        // Título principal
        val titulo = Paragraph("YoCapital")
            .setFontSize(32f)
            .setBold()
            .setFontColor(COLOR_VERDE)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(100f)

        val subtitulo = Paragraph("Reporte Mensual de Ventas")
            .setFontSize(20f)
            .setFontColor(COLOR_NEGRO)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10f)

        val periodo = Paragraph("${datos.mes} ${datos.anio}")
            .setFontSize(16f)
            .setFontColor(COLOR_GRIS)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(5f)

        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val fechaGeneracion = Paragraph("Generado el: $fecha")
            .setFontSize(12f)
            .setFontColor(COLOR_GRIS)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(20f)

        document.add(titulo)
        document.add(subtitulo)
        document.add(periodo)
        document.add(fechaGeneracion)
    }

    private fun agregarResumenEjecutivo(document: Document, datos: DatosReporte) {
        document.add(Paragraph("\n"))

        val titulo = Paragraph("📊 RESUMEN EJECUTIVO")
            .setFontSize(18f)
            .setBold()
            .setFontColor(COLOR_VERDE)
            .setMarginTop(20f)

        document.add(titulo)

        // Tabla de resumen
        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f)))
            .useAllAvailableWidth()
            .setMarginTop(10f)

        // Datos del resumen
        agregarFilaTabla(tabla, "Total Ventas", String.format("$%,.2f", datos.totalVentas))
        agregarFilaTabla(tabla, "Transacciones", datos.transacciones.toString())
        agregarFilaTabla(tabla, "Promedio por venta", String.format("$%,.2f", datos.promedioVenta))
        agregarFilaTabla(tabla, "Total Comisiones", String.format("$%,.2f", datos.totalComisiones))

        document.add(tabla)
    }

    private fun agregarComparativa(document: Document, datos: DatosReporte) {
        document.add(Paragraph("\n"))

        val titulo = Paragraph("📈 COMPARATIVA")
            .setFontSize(18f)
            .setBold()
            .setFontColor(COLOR_VERDE)
            .setMarginTop(15f)

        document.add(titulo)

        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f)))
            .useAllAvailableWidth()
            .setMarginTop(10f)

        agregarFilaTabla(tabla, "Mes anterior", String.format("$%,.2f", datos.mesAnterior))

        val simbolo = when {
            datos.crecimiento > 0 -> "▲"
            datos.crecimiento < 0 -> "▼"
            else -> "➡"
        }
        agregarFilaTabla(tabla, "Crecimiento", String.format("$simbolo %+.1f%%", datos.crecimiento))

        document.add(tabla)
    }

    private fun agregarRanking(document: Document, datos: DatosReporte) {
        document.add(com.itextpdf.layout.element.AreaBreak())

        val titulo = Paragraph("🏆 RANKING DE VENDEDORES")
            .setFontSize(18f)
            .setBold()
            .setFontColor(COLOR_VERDE)
            .setMarginTop(15f)

        document.add(titulo)

        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f, 3f)))
            .useAllAvailableWidth()
            .setMarginTop(10f)

        // Header
        tabla.addHeaderCell(crearCeldaHeader("#"))
        tabla.addHeaderCell(crearCeldaHeader("Vendedor"))
        tabla.addHeaderCell(crearCeldaHeader("Ventas"))

        // Datos
        for (vendedor in datos.ranking) {
            tabla.addCell(crearCelda("${vendedor.posicion}"))
            tabla.addCell(crearCelda(vendedor.nombre))
            tabla.addCell(crearCelda(String.format("$%,.2f", vendedor.ventas)))
        }

        document.add(tabla)
    }

    private fun agregarProductos(document: Document, datos: DatosReporte) {
        document.add(Paragraph("\n"))

        val titulo = Paragraph("📦 PRODUCTOS MÁS VENDIDOS")
            .setFontSize(18f)
            .setBold()
            .setFontColor(COLOR_VERDE)
            .setMarginTop(15f)

        document.add(titulo)

        val tabla = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f, 2f)))
            .useAllAvailableWidth()
            .setMarginTop(10f)

        // Header
        tabla.addHeaderCell(crearCeldaHeader("#"))
        tabla.addHeaderCell(crearCeldaHeader("Producto"))
        tabla.addHeaderCell(crearCeldaHeader("Unidades"))

        // Datos
        for (producto in datos.productos) {
            tabla.addCell(crearCelda("${producto.posicion}"))
            tabla.addCell(crearCelda(producto.nombre))
            tabla.addCell(crearCelda("${producto.cantidad} uds"))
        }

        document.add(tabla)
    }

    private fun agregarPiePagina(document: Document) {
        val pie = Paragraph("\n\n© 2026 YoCapital - Todos los derechos reservados")
            .setFontSize(10f)
            .setFontColor(COLOR_GRIS)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30f)

        document.add(pie)
    }

    private fun agregarFilaTabla(tabla: Table, label: String, valor: String) {
        tabla.addCell(
            Cell().add(Paragraph(label).setFontColor(COLOR_GRIS).setFontSize(12f))
                .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(8f)
        )
        tabla.addCell(
            Cell().add(Paragraph(valor).setBold().setFontColor(COLOR_NEGRO).setFontSize(12f))
                .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(8f)
                .setTextAlignment(TextAlignment.RIGHT)
        )
    }

    private fun crearCeldaHeader(texto: String): Cell {
        return Cell().add(
            Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE).setFontSize(12f)
        )
            .setBackgroundColor(COLOR_VERDE)
            .setPadding(8f)
            .setTextAlignment(TextAlignment.CENTER)
    }

    private fun crearCelda(texto: String): Cell {
        return Cell().add(Paragraph(texto).setFontSize(11f))
            .setBorder(SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
            .setPadding(8f)
    }
}