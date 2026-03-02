package com.example.yocapital.vendedor.home

sealed class VentaItem {
    data class Header(val mesAnio: String) : VentaItem()
    data class VentaData(val venta: Venta) : VentaItem()
}