package com.example.yocapital

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: String = "vendedor"
)