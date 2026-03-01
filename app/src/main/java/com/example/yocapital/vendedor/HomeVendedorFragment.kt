package com.example.yocapital.vendedor

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.yocapital.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class HomeVendedorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtSaludo = view.findViewById<TextView>(R.id.txtSaludo)
        val tvTotalComisiones = view.findViewById<TextView>(R.id.total_comisiones)
        val tvTotalVentas = view.findViewById<TextView>(R.id.total_ventas)

        val sharedPref = requireActivity().getSharedPreferences("MiAppPrefs", Context.MODE_PRIVATE)
        val idVendedorActual = sharedPref.getString("VENDEDOR_ID", "VENDEDOR_PRUEBA_123")

        txtSaludo.text = "Hola, Juan Pérez"

        val calendario = Calendar.getInstance()
        val mesActual = calendario.get(Calendar.MONTH) + 1
        val anioActual = calendario.get(Calendar.YEAR)

        val terminacionFechaMesActual = String.format("/%02d/%04d", mesActual, anioActual)

        db.collection("ventas")
            .whereEqualTo("vendedor_id", idVendedorActual)
            .get()
            .addOnSuccessListener { documentos ->
                var sumaComisiones = 0.0
                var sumaVentasTotales = 0.0

                for (documento in documentos) {
                    val fechaVenta = documento.getString("fecha") ?: ""

                    if (fechaVenta.endsWith(terminacionFechaMesActual)) {
                        val comision = documento.getDouble("comision_vendedor") ?: 0.0
                        val totalVenta = documento.getDouble("total_venta") ?: 0.0

                        sumaComisiones += comision
                        sumaVentasTotales += totalVenta
                    }
                }

                tvTotalComisiones.text = String.format("$%,.2f", sumaComisiones)
                tvTotalVentas.text = String.format("$%,.2f", sumaVentasTotales)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Error al cargar totales", e)
            }
    }
}