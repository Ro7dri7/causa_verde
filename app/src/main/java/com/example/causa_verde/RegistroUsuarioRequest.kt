package com.example.causa_verde

data class RegistroUsuarioRequest(
    val uid: String,
    val rol: String,
    val correo: String,
    val telefono: String,
    val latitud: Double,
    val longitud: Double,
    val nombre: String = "",
    val apellidos: String = "",
    val ruc: String = "",
    val razon_social: String = "",
    val nombre_olla: String = "",
    val codigo_bap: String = ""
)