package com.example.yocapital.vendedor

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.yocapital.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class VentaVendedorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private val listaNombresProductos = mutableListOf("Selecciona un producto...")
    private val listaPrecios = mutableListOf(0.0)
    private val listaComisiones = mutableListOf(0.0)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_venta_vendedor, container, false)
    }

    private fun configurarCalculadoraEnTiempoReal(view: View) {
        val inputCantidad = view.findViewById<EditText>(R.id.input_cantidad)
        val spinnerProducto = view.findViewById<Spinner>(R.id.spinner_producto)
        val tvTotalVenta = view.findViewById<TextView>(R.id.tv_total_venta)
        val tvComisionEstimada = view.findViewById<TextView>(R.id.tv_comision_estimada)

        fun calcularYMostrar() {
            val posicionSeleccionada = spinnerProducto.selectedItemPosition

            if (posicionSeleccionada == 0) {
                tvTotalVenta.text = "Total Venta: \$0.00"
                tvComisionEstimada.text = "Tu Comisión: \$0.00"
                return
            }

            val precioDelProducto = listaPrecios[posicionSeleccionada]
            val porcentajeComision = listaComisiones[posicionSeleccionada]

            val textoCantidad = inputCantidad.text.toString()
            val cantidad = if (textoCantidad.isNotEmpty()) textoCantidad.toInt() else 0

            val totalVenta = precioDelProducto * cantidad
            val gananciaVendedor = totalVenta * (porcentajeComision / 100)

            tvTotalVenta.text = String.format("Total Venta: $%,.2f", totalVenta)
            tvComisionEstimada.text = String.format("Tu Comisión: $%,.2f", gananciaVendedor)
        }

        inputCantidad.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calcularYMostrar()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        spinnerProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calcularYMostrar()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarCalendario(view)
        configurarNavegacion(view)
        configurarSpinners(view)
        configurarCalculadoraEnTiempoReal(view)
    }

    private fun configurarSpinners(view: View) {
        val spinnerCliente = view.findViewById<Spinner>(R.id.spinner_cliente)
        val spinnerProducto = view.findViewById<Spinner>(R.id.spinner_producto)

        val listaClientes = mutableListOf("Selecciona un cliente...")

        val adapterClientes = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaClientes)
        adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCliente.adapter = adapterClientes

        val adapterProductos = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaNombresProductos)
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducto.adapter = adapterProductos

        db.collection("usuarios").get().addOnSuccessListener { documentos ->
            for (documento in documentos) {
                val nombre = documento.getString("nombre")
                if (nombre != null) listaClientes.add(nombre)
            }
            adapterClientes.notifyDataSetChanged()
        }

        db.collection("productos").get().addOnSuccessListener { documentos ->
            for (documento in documentos) {
                val nombre = documento.getString("producto")
                val precio = documento.getDouble("monto_base") ?: 0.0
                val comision = documento.getDouble("comision") ?: 0.0

                if (nombre != null) {
                    listaNombresProductos.add(nombre)
                    listaPrecios.add(precio)
                    listaComisiones.add(comision)
                }
            }
            adapterProductos.notifyDataSetChanged()
        }
    }

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
        val btnAgregarCliente = view.findViewById<TextView>(R.id.btnAnadirCliente)

        btnAgregarCliente.setOnClickListener {
            val fragmentoCliente = AgregarClienteFragment()

            // Le pedimos permiso a la Activity padre (VendedorActivity) para usar su hueco
            requireActivity().supportFragmentManager.beginTransaction()
                // Animación opcional para que se note que cambiaste de pantalla y no que se "reinició"
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragmentoCliente)
                .addToBackStack(null) // Guarda la pantalla anterior para poder regresar con la flecha
                .commit()
        }
    }
}