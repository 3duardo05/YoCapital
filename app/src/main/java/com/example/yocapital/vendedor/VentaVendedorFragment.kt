package com.example.yocapital.vendedor

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.yocapital.R
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class VentaVendedorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_venta_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Llamamos a nuestras funciones organizadoras
        configurarCalendario(view)
        configurarNavegacion(view)
    }

// --- MÉTODOS DE LÓGICA SEPARADOS ---

    private fun configurarCalendario(view: View) {
        val inputFecha = view.findViewById<TextInputEditText>(R.id.input_fecha_venta)
        val calendario = Calendar.getInstance()

        val diaHoy = calendario.get(Calendar.DAY_OF_MONTH)
        val mesHoy = calendario.get(Calendar.MONTH) + 1
        val anioHoy = calendario.get(Calendar.YEAR)

        inputFecha.setText("$diaHoy/$mesHoy/$anioHoy")

        inputFecha.setOnClickListener {
            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val fechaSeleccionada = String.format("%02d/%02d/%04d", day, month + 1, year)
                    inputFecha.setText(fechaSeleccionada)
                },
                anioHoy,
                mesHoy - 1,
                diaHoy
            )
            dpd.show()
        }
    }

    private fun configurarNavegacion(view: View) {
        val btnAgregar = view.findViewById<TextView>(R.id.btnAnadirCliente)

        btnAgregar.setOnClickListener {
            val nuevoFragmento = AgregarClienteFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.viewPager, nuevoFragmento)
                .addToBackStack(null)
                .commit()
        }
    }
}