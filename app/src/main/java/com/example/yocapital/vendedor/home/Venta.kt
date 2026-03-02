package com.example.yocapital.vendedor.home

data class Venta(
    val producto: String = "",
    val cliente: String = "",
    val fecha: String = "",
    val comision_vendedor: Double = 0.0,
    val total_venta: Double = 0.0
)