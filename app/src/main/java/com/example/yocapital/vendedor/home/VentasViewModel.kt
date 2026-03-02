package com.example.yocapital.vendedor.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class VentasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _listaVentas = MutableLiveData<List<Venta>>()
    val listaVentas: LiveData<List<Venta>> = _listaVentas

    var yaCargado = false

    fun cargarVentas(idVendedor: String) {
        if (yaCargado) return

        db.collection("ventas")
            .whereEqualTo("vendedor_id", idVendedor)
            .get()
            .addOnSuccessListener { documentos ->
                val ventas = documentos.toObjects(Venta::class.java)
                _listaVentas.value = ventas
                yaCargado = true
            }
    }

    fun refrescar(idVendedor: String) {
        yaCargado = false
        cargarVentas(idVendedor)
    }
}