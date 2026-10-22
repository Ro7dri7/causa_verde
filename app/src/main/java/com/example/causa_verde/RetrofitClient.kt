package com.example.causa_verde

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Reemplaza X.X.X.X con la IP pública real de tu instancia en AWS EC2
    // y usa el puerto en el que esté corriendo tu FastAPI (ej. 8000 u 8001)
    private const val BASE_URL = "http://16.59.55.5:8001/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}