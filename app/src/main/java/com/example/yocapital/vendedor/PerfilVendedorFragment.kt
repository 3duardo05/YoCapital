package com.example.yocapital.vendedor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yocapital.R
import com.example.yocapital.login.SessionManager
import com.example.yocapital.login.login.LoginActivity
import com.google.firebase.firestore.FirebaseFirestore

class PerfilVendedorFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val TAG = "PerfilVendedorFragment"

    private var txtNombreVendedor: TextView? = null
    private var txtCorreoVendedor: TextView? = null
    private var txtTelefonoVendedor: TextView? = null
    private var txtTotalVentasHistorico: TextView? = null
    private var txtCantidadVentas: TextView? = null
    private var txtTotalComisionesHistorico: TextView? = null
    private var txtClienteFrecuente: TextView? = null
    private var txtVentasClienteFrecuente: TextView? = null
    private var btnCambiarPassword: Button? = null
    private var btnCerrarSesion: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView llamado")
        return inflater.inflate(R.layout.fragment_perfil_vendedor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated llamado")

        try {
            txtNombreVendedor = view.findViewById(R.id.txtNombreVendedor)
            txtCorreoVendedor = view.findViewById(R.id.txtCorreoVendedor)
            txtTelefonoVendedor = view.findViewById(R.id.txtTelefonoVendedor)
            txtTotalVentasHistorico = view.findViewById(R.id.txtTotalVentasHistorico)
            txtCantidadVentas = view.findViewById(R.id.txtCantidadVentas)
            txtTotalComisionesHistorico = view.findViewById(R.id.txtTotalComisionesHistorico)
            txtClienteFrecuente = view.findViewById(R.id.txtClienteFrecuente)
            txtVentasClienteFrecuente = view.findViewById(R.id.txtVentasClienteFrecuente)
            btnCambiarPassword = view.findViewById(R.id.btnCambiarPassword)
            btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)

            Log.d(TAG, "Vistas inicializadas correctamente")

            cargarDatosVendedor()

            btnCambiarPassword?.setOnClickListener {
                Toast.makeText(requireContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show()
            }

            btnCerrarSesion?.setOnClickListener {
                cerrarSesion()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error en onViewCreated: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al cargar perfil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosVendedor() {
        try {
            val userId = SessionManager.getUserId(requireContext())
            Log.d(TAG, "UserId obtenido: $userId")

            if (userId.isEmpty()) {
                Log.e(TAG, "UserId está vacío")
                Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
                return
            }

            val nombre = SessionManager.getNombre(requireContext())
            Log.d(TAG, "Nombre obtenido: $nombre")
            txtNombreVendedor?.text = nombre

            db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener { documento ->
                    try {
                        Log.d(TAG, "Documento Firebase obtenido: ${documento.exists()}")
                        if (documento.exists()) {
                            Log.d(TAG, "Campos disponibles en Firebase: ${documento.data?.keys}")

                            val correo = documento.getString("correo")
                                ?: documento.getString("email")
                                ?: documento.getString("mail")
                                ?: "No disponible"

                            val telefono = documento.getString("telefono")
                                ?: documento.getString("phone")
                                ?: documento.getString("celular")
                                ?: "No disponible"

                            Log.d(TAG, "Correo encontrado: $correo")
                            Log.d(TAG, "Teléfono encontrado: $telefono")

                            txtCorreoVendedor?.text = correo
                            txtTelefonoVendedor?.text = formatearTelefono(telefono)
                        } else {
                            Log.e(TAG, "El documento del usuario no existe en Firebase")
                            txtCorreoVendedor?.text = SessionManager.getCorreo(requireContext())
                            txtTelefonoVendedor?.text = formatearTelefono(SessionManager.getTelefono(requireContext()))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando documento Firebase: ${e.message}", e)
                        txtCorreoVendedor?.text = SessionManager.getCorreo(requireContext())
                        txtTelefonoVendedor?.text = formatearTelefono(SessionManager.getTelefono(requireContext()))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al cargar perfil desde Firebase: ${exception.message}", exception)
                    txtCorreoVendedor?.text = SessionManager.getCorreo(requireContext())
                    txtTelefonoVendedor?.text = formatearTelefono(SessionManager.getTelefono(requireContext()))
                }

            cargarEstadisticasVentas(userId)

        } catch (e: Exception) {
            Log.e(TAG, "Error en cargarDatosVendedor: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarEstadisticasVentas(vendedorId: String) {
        try {
            Log.d(TAG, "Cargando estadísticas para vendedor: $vendedorId")

            db.collection("ventas")
                .whereEqualTo("vendedor_id", vendedorId)
                .get()
                .addOnSuccessListener { documentos ->
                    try {
                        Log.d(TAG, "Ventas encontradas: ${documentos.size()}")

                        if (documentos.isEmpty) {
                            Log.d(TAG, "No hay ventas para este vendedor")
                            txtTotalVentasHistorico?.text = "$0.00"
                            txtCantidadVentas?.text = "0 ventas realizadas"
                            txtTotalComisionesHistorico?.text = "$0.00"
                            txtClienteFrecuente?.text = "Sin clientes aún"
                            txtVentasClienteFrecuente?.text = "0 compras"
                            return@addOnSuccessListener
                        }

                        val ventas = documentos.toObjects(com.example.yocapital.vendedor.home.Venta::class.java)

                        val totalVentas = ventas.sumOf { it.total_venta }
                        val totalComisiones = ventas.sumOf { it.comision_vendedor }
                        val cantidadVentas = ventas.size

                        Log.d(TAG, "Total ventas: $totalVentas, Comisiones: $totalComisiones, Cantidad: $cantidadVentas")

                        txtTotalVentasHistorico?.text = String.format("$%,.2f", totalVentas)
                        txtCantidadVentas?.text = "$cantidadVentas ventas realizadas"
                        txtTotalComisionesHistorico?.text = String.format("$%,.2f", totalComisiones)

                        encontrarClienteFrecuente(ventas)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando ventas: ${e.message}", e)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al cargar estadísticas: ${exception.message}", exception)
                    txtTotalVentasHistorico?.text = "$0.00"
                    txtCantidadVentas?.text = "0 ventas realizadas"
                    txtTotalComisionesHistorico?.text = "$0.00"
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error en cargarEstadisticasVentas: ${e.message}", e)
        }
    }

    private fun encontrarClienteFrecuente(ventas: List<com.example.yocapital.vendedor.home.Venta>) {
        try {
            if (ventas.isEmpty()) {
                txtClienteFrecuente?.text = "Sin clientes aún"
                txtVentasClienteFrecuente?.text = "0 compras"
                return
            }

            val clientesContador = ventas.groupingBy { it.cliente }.eachCount()

            val clienteMasFrecuente = clientesContador.maxByOrNull { it.value }

            if (clienteMasFrecuente != null) {
                Log.d(TAG, "Cliente frecuente: ${clienteMasFrecuente.key} con ${clienteMasFrecuente.value} compras")
                txtClienteFrecuente?.text = clienteMasFrecuente.key
                txtVentasClienteFrecuente?.text = "${clienteMasFrecuente.value} compras"
            } else {
                txtClienteFrecuente?.text = "Sin clientes"
                txtVentasClienteFrecuente?.text = "0 compras"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error en encontrarClienteFrecuente: ${e.message}", e)
            txtClienteFrecuente?.text = "Sin datos"
            txtVentasClienteFrecuente?.text = "0 compras"
        }
    }

    private fun formatearTelefono(telefono: String): String {
        if (telefono.isEmpty() || telefono == "No disponible") return telefono

        return try {
            val telefonoLimpio = telefono.replace(Regex("[^0-9]"), "")

            when {
                telefonoLimpio.length == 10 -> {
                    "${telefonoLimpio.substring(0, 2)} ${telefonoLimpio.substring(2, 6)} ${telefonoLimpio.substring(6)}"
                }
                telefonoLimpio.length > 10 -> {
                    "${telefonoLimpio.substring(0, 2)} ${telefonoLimpio.substring(2, 6)} ${telefonoLimpio.substring(6, 10)}"
                }
                else -> telefono
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error formateando teléfono: ${e.message}", e)
            telefono
        }
    }

    private fun cerrarSesion() {
        try {
            Log.d(TAG, "Mostrando diálogo de confirmación")

            AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("Sí") { _, _ ->
                    Log.d(TAG, "Usuario confirmó cierre de sesión")
                    SessionManager.logout(requireContext())

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    Log.d(TAG, "Usuario canceló cierre de sesión")
                    dialog.dismiss()
                }
                .show()

        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar diálogo: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        txtNombreVendedor = null
        txtCorreoVendedor = null
        txtTelefonoVendedor = null
        txtTotalVentasHistorico = null
        txtCantidadVentas = null
        txtTotalComisionesHistorico = null
        txtClienteFrecuente = null
        txtVentasClienteFrecuente = null
        btnCambiarPassword = null
        btnCerrarSesion = null
    }
}