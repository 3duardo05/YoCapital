package com.example.yocapital.vendedor

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class VentaVendedorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    private val listaNombresProductos = mutableListOf("Selecciona un producto...")
    private val listaPrecios = mutableListOf(0.0)
    private val listaComisiones = mutableListOf(0.0)
    private val listaStock = mutableListOf(0)
    private val listaIdsProductos = mutableListOf("")

    private lateinit var btnGuardar: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_venta_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnGuardar = view.findViewById(R.id.btn_AnadirVenta)

        configurarCalendario(view)
        configurarNavegacion(view)
        configurarSpinners(view)
        configurarCalculadoraEnTiempoReal(view)
        configurarBotonGuardarVenta(view)
    }

    private fun configurarSpinners(view: View) {
        val spinnerCliente = view.findViewById<Spinner>(R.id.spinner_cliente)
        val spinnerProducto = view.findViewById<Spinner>(R.id.spinner_producto)

        listaNombresProductos.clear()
        listaPrecios.clear()
        listaComisiones.clear()
        listaStock.clear()
        listaIdsProductos.clear()

        listaNombresProductos.add("Selecciona un producto...")
        listaPrecios.add(0.0)
        listaComisiones.add(0.0)
        listaStock.add(0)
        listaIdsProductos.add("")

        val listaClientes = mutableListOf("Selecciona un cliente...")

        val adapterClientes = ArrayAdapter(
            requireContext(),
            R.layout.spinner_personalizado,
            listaClientes
        )
        adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCliente.adapter = adapterClientes

        val adapterProductos = ArrayAdapter(
            requireContext(),
            R.layout.spinner_personalizado,
            listaNombresProductos
        )
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProducto.adapter = adapterProductos

        db.collection("clientes").get().addOnSuccessListener { documentos ->
            for (documento in documentos) {
                val nombre = documento.getString("nombre")
                if (nombre != null) listaClientes.add(nombre)
            }
            adapterClientes.notifyDataSetChanged()
        }

        db.collection("productos").get().addOnSuccessListener { documentos ->
            for (documento in documentos) {
                try {
                    val id = documento.id
                    val nombre = documento.getString("nombre") ?: ""

                    val precio = when (val precioRaw = documento.get("precio")) {
                        is Number -> precioRaw.toDouble()
                        is String -> precioRaw.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    val comision = when (val comisionRaw = documento.get("comision")) {
                        is Number -> comisionRaw.toDouble()
                        is String -> comisionRaw.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }

                    val stock = when (val stockRaw = documento.get("stock")) {
                        is Number -> stockRaw.toInt()
                        is String -> stockRaw.toIntOrNull() ?: 0
                        else -> 0
                    }

                    if (nombre.isNotEmpty()) {
                        val nombreConStock = when {
                            stock == 0 -> "$nombre ❌ SIN STOCK"
                            stock < 5 -> "$nombre ⚠️ Stock: $stock"
                            else -> "$nombre (Stock: $stock)"
                        }

                        listaNombresProductos.add(nombreConStock)
                        listaPrecios.add(precio)
                        listaComisiones.add(comision)
                        listaStock.add(stock)
                        listaIdsProductos.add(id)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("VentaVendedor", "Error al cargar producto: ${e.message}")
                }
            }
            adapterProductos.notifyDataSetChanged()
        }
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
                tvTotalVenta.setTextColor(0xFF0E3E3E.toInt())
                tvComisionEstimada.setTextColor(0xFF0E3E3E.toInt())
                btnGuardar.isEnabled = true
                btnGuardar.alpha = 1.0f
                return
            }

            val precioDelProducto = listaPrecios[posicionSeleccionada]
            val porcentajeComision = listaComisiones[posicionSeleccionada]
            val stockDisponible = listaStock[posicionSeleccionada]

            val textoCantidad = inputCantidad.text.toString()
            val cantidad = if (textoCantidad.isNotEmpty()) textoCantidad.toInt() else 0

            if (stockDisponible == 0) {
                tvTotalVenta.text = "❌ PRODUCTO SIN STOCK"
                tvTotalVenta.setTextColor(0xFFFF0000.toInt())
                tvComisionEstimada.text = "No disponible para venta"
                tvComisionEstimada.setTextColor(0xFFFF0000.toInt())
                btnGuardar.isEnabled = false
                btnGuardar.alpha = 0.5f
                return
            }

            if (cantidad > stockDisponible) {
                tvTotalVenta.text = "⚠️ Stock insuficiente"
                tvTotalVenta.setTextColor(0xFFFF0000.toInt())
                tvComisionEstimada.text = "Disponible: $stockDisponible unidades"
                tvComisionEstimada.setTextColor(0xFFFF0000.toInt())
                btnGuardar.isEnabled = false
                btnGuardar.alpha = 0.5f
                return
            }

            tvTotalVenta.setTextColor(0xFF0E3E3E.toInt())
            tvComisionEstimada.setTextColor(0xFF0E3E3E.toInt())
            btnGuardar.isEnabled = true
            btnGuardar.alpha = 1.0f

            val totalVenta = precioDelProducto * cantidad
            val gananciaVendedor = totalVenta * (porcentajeComision / 100)

            tvTotalVenta.text = String.format("Total Venta: $%,.2f", totalVenta)
            tvComisionEstimada.text = String.format("Tu Comisión (%d%%): $%,.2f", porcentajeComision.toInt(), gananciaVendedor)
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

                if (position > 0) {
                    val stockDisponible = listaStock[position]
                    when {
                        stockDisponible == 0 -> {
                            Toast.makeText(requireContext(), "❌ Este producto NO tiene stock disponible", Toast.LENGTH_LONG).show()
                        }
                        stockDisponible < 5 -> {
                            Toast.makeText(requireContext(), "⚠️ Stock bajo: Solo quedan $stockDisponible unidades", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarCalendario(view: View) {
        val inputFecha = view.findViewById<TextInputEditText>(R.id.input_fecha_venta)
        val calendario = Calendar.getInstance()

        val diaHoy = calendario.get(Calendar.DAY_OF_MONTH)
        val mesHoy = calendario.get(Calendar.MONTH) + 1
        val anioHoy = calendario.get(Calendar.YEAR)

        val fechaHoyFormateada = String.format("%02d/%02d/%04d", diaHoy, mesHoy, anioHoy)
        inputFecha.setText(fechaHoyFormateada)

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

            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragmentoCliente)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun configurarBotonGuardarVenta(view: View) {
        val spinnerCliente = view.findViewById<Spinner>(R.id.spinner_cliente)
        val spinnerProducto = view.findViewById<Spinner>(R.id.spinner_producto)
        val inputCantidad = view.findViewById<EditText>(R.id.input_cantidad)
        val inputFecha = view.findViewById<TextInputEditText>(R.id.input_fecha_venta)

        btnGuardar.setOnClickListener {
            val posicionCliente = spinnerCliente.selectedItemPosition
            val posicionProducto = spinnerProducto.selectedItemPosition
            val textoCantidad = inputCantidad.text.toString()

            if (posicionCliente == 0 || posicionProducto == 0 || textoCantidad.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad = textoCantidad.toInt()
            val stockDisponible = listaStock[posicionProducto]
            val productoId = listaIdsProductos[posicionProducto]

            if (stockDisponible == 0) {
                Toast.makeText(requireContext(), "❌ Este producto NO tiene stock disponible", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (cantidad > stockDisponible) {
                Toast.makeText(requireContext(), "❌ Stock insuficiente. Disponible: $stockDisponible unidades", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val clienteElegido = spinnerCliente.selectedItem.toString()
            val productoElegido = listaNombresProductos[posicionProducto]
                .replace(Regex(" \\(Stock: \\d+\\)|❌ SIN STOCK|⚠️ Stock: \\d+"), "").trim()
            val fecha = inputFecha.text.toString()

            val precio = listaPrecios[posicionProducto]
            val porcentajeComision = listaComisiones[posicionProducto]

            val totalVenta = precio * cantidad
            val gananciaVendedor = totalVenta * (porcentajeComision / 100)

            val nuevaVenta = hashMapOf(
                "vendedor_id" to SessionManager.getUserId(requireContext()),
                "cliente" to clienteElegido,
                "producto" to productoElegido,
                "cantidad" to cantidad,
                "fecha" to fecha,
                "total_venta" to totalVenta,
                "comision_vendedor" to gananciaVendedor
            )

            db.collection("ventas")
                .add(nuevaVenta)
                .addOnSuccessListener {
                    val nuevoStock = stockDisponible - cantidad
                    db.collection("productos")
                        .document(productoId)
                        .update("stock", nuevoStock)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "¡Venta registrada exitosamente!", Toast.LENGTH_LONG).show()

                            spinnerCliente.setSelection(0)
                            spinnerProducto.setSelection(0)
                            inputCantidad.setText("1")

                            configurarSpinners(requireView())
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Venta guardada pero error al actualizar stock", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}