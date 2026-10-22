package com.example.causa_verde

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/api/registro")
    suspend fun registrarUsuarioFinal(@Body request: RegistroUsuarioRequest): Response<RegistroResponse>

    // Ruta anterior mantenida por compatibilidad si la necesitas en otro lado
    @POST("/api/publicar_producto")
    fun publicarProducto(@Body request: PublicacionRequest): Call<PublicacionResponse>

    // NUEVA RUTA: Sube la imagen física a AWS S3 y guarda los datos en MySQL
    @Multipart
    @POST("/api/publicar_producto")
    fun publicarProductoConImagen(
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("nombre_producto") nombre: RequestBody,
        @Part("categoria") categoria: RequestBody,
        @Part("precio_regular") precioRegular: RequestBody,
        @Part("precio_oferta") precioOferta: RequestBody,
        @Part("stock_disponible") stock: RequestBody,
        @Part("fecha_vencimiento") fecha: RequestBody,
        @Part("estado_empaque") estado: RequestBody,
        @Part("lote_bap") loteBap: RequestBody?,
        @Part("limite_retiro") limiteRetiro: RequestBody,
        @Part foto: MultipartBody.Part
    ): Call<PublicacionResponse>

    @GET("/api/publicaciones")
    fun obtenerPublicaciones(): Call<List<PublicacionResponseItem>>
}