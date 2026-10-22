package com.example.causa_verde

data class RegistroResponse(
    val status: String,
    val mensaje: String
)

data class PublicacionRequest(
    val id_usuario: Int, // <-- Cambiado de id_entidad a id_usuario
    val nombre_producto: String,
    val categoria: String,
    val foto_url: String,
    val precio_regular: Double,
    val precio_oferta: Double,
    val stock_disponible: Int,
    val fecha_vencimiento: String,
    val estado_empaque: String,
    val lote_bap: String?
)

data class PublicacionResponse(
    val mensaje: String,
    val id_publicacion: Int
)

data class PublicacionResponseItem(
    val id_publicacion: Int,
    val id_usuario: Int, // <-- Cambiado de id_entidad a id_usuario
    val nombre_donador: String,
    val tipo_publicacion: String,
    val nombre_producto: String,
    val categoria: String,
    val foto_url: String?,
    val precio_regular: Double,
    val precio_oferta: Double,
    val stock_disponible: Int,
    val fecha_vencimiento: String,
    val estado_empaque: String,
    val lote_bap: String?,
    val estado: String
)