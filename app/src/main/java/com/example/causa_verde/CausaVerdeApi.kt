package com.example.causa_verde

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CausaVerdeApi {
    @POST("/api/registro")
    suspend fun registrarUsuario(@Body request: RegistroUsuarioRequest): Response<Any>
}
