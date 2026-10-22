package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class InicioDonadorActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_donador)

        auth = FirebaseAuth.getInstance()

        // Capturar los datos del Login (Intent)
        val nombre = intent.getStringExtra("NOMBRE") ?: "Socio"
        val razonSocial = intent.getStringExtra("RAZON_SOCIAL")
        val direccion = intent.getStringExtra("DIRECCION") ?: "Ubicación"
        val rol = intent.getStringExtra("ROL") ?: "DONADOR_REGULAR"

        // Referencias a la UI
        val tvSaludo = findViewById<TextView>(R.id.tv_saludo_donador)
        val tvUbicacion = findViewById<TextView>(R.id.tv_ubicacion_texto_donador)
        val btnPerfilDonador = findViewById<ImageView>(R.id.btn_perfil_donador) // Asegúrate de tener este ID en tu header de donador si aplica, o usa una referencia general

        tvUbicacion.text = direccion

        if (!razonSocial.isNullOrEmpty()) {
            tvSaludo.text = "¡Hola, $razonSocial!"
        } else {
            tvSaludo.text = "¡Hola, $nombre!"
        }

        // Configurar Cierre de Sesión en el perfil si el botón existe en el layout
        btnPerfilDonador?.setOnClickListener { view ->
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

        // Configurar botones de Acciones Rápidas
        val btnCrearProducto = findViewById<MaterialCardView>(R.id.btn_crear_producto)
        val btnArmarPack = findViewById<MaterialCardView>(R.id.btn_armar_pack)

        btnCrearProducto.setOnClickListener {
            // Abrir la actividad para crear producto pasando el rol
            val intent = Intent(this, CrearProductoDonadorActivity::class.java)
            intent.putExtra("ROL", rol)
            startActivity(intent)
        }

        btnArmarPack.setOnClickListener {
            Toast.makeText(this, "Módulo de Paquete Sorpresa en desarrollo", Toast.LENGTH_SHORT).show()
        }

        // Navegación inferior
        val btnNavInventario = findViewById<LinearLayout>(R.id.btn_nav_inventario)
        btnNavInventario.setOnClickListener {
            val intent = Intent(this, InventarioActivity::class.java)
            startActivity(intent)
        }
    }
}