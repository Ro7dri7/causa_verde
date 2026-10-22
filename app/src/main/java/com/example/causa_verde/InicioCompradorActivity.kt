package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioCompradorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var rvProductosComprador: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_comp)

        auth = FirebaseAuth.getInstance()

        val nombre = intent.getStringExtra("NOMBRE") ?: "Usuario"
        val apellidos = intent.getStringExtra("APELLIDOS") ?: ""
        val direccionUbicacion = intent.getStringExtra("DIRECCION") ?: "Carabayllo, Lima"
        val rol = intent.getStringExtra("ROL") ?: "COMPRADOR_REGULAR"
        val nombreOlla = intent.getStringExtra("NOMBRE_OLLA")

        val tvSaludo = findViewById<TextView>(R.id.tv_saludo)
        val tvUbicacion = findViewById<TextView>(R.id.tv_ubicacion_texto)
        val btnPerfil = findViewById<ImageView>(R.id.btn_perfil)
        val btnCarrito = findViewById<ImageView>(R.id.btn_carrito)
        val btnNotificaciones = findViewById<ImageView>(R.id.btn_notificaciones)

        if (rol == "COMPRADOR_OLLA" && !nombreOlla.isNullOrEmpty()) {
            tvSaludo.text = "¡Hola, $nombre ($nombreOlla)!"
        } else {
            tvSaludo.text = "¡Hola, $nombre $apellidos!"
        }

        tvUbicacion.text = direccionUbicacion

        // Menú flotante de perfil con opción de Cerrar Sesión
        btnPerfil.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            popupMenu.menu.add(0, 1, 0, "Cerrar Sesión")
            popupMenu.setOnMenuItemClickListener { item ->
                if (item.itemId == 1) {
                    auth.signOut()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                } else {
                    false
                }
            }
            popupMenu.show()
        }

        btnCarrito.setOnClickListener {
            Toast.makeText(this, "Carrito de compras", Toast.LENGTH_SHORT).show()
        }
        btnNotificaciones.setOnClickListener {
            Toast.makeText(this, "Sin notificaciones nuevas", Toast.LENGTH_SHORT).show()
        }

        // Configurar RecyclerView
        rvProductosComprador = findViewById(R.id.rv_productos_comprador)
        rvProductosComprador.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        cargarOfertasDesdeAWS()
    }

    private fun cargarOfertasDesdeAWS() {
        RetrofitClient.apiService.obtenerPublicaciones().enqueue(object : Callback<List<PublicacionResponseItem>> {
            override fun onResponse(call: Call<List<PublicacionResponseItem>>, response: Response<List<PublicacionResponseItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    val listaProductos = response.body()!!
                    Log.d("AWS_INICIO", "Productos cargados: ${listaProductos.size}")

                    val adaptador = AdaptadorInicioComprador(listaProductos)
                    rvProductosComprador.adapter = adaptador
                }
            }

            override fun onFailure(call: Call<List<PublicacionResponseItem>>, t: Throwable) {
                Log.e("AWS_INICIO", "Error de red: ${t.message}")
            }
        })
    }
}