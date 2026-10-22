package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón para ir al flujo de registro (Crear Cuenta)
        val btnCrearCuenta = findViewById<MaterialButton>(R.id.btn_crear_cuenta)
        btnCrearCuenta.setOnClickListener {
            val intent = Intent(this, RegistroRolActivity::class.java)
            startActivity(intent)
        }

        // 🔑 NUEVO: Botón para ir a la pantalla de credenciales (Iniciar Sesión)
        val btnIniciarSesion = findViewById<MaterialButton>(R.id.btn_iniciar_sesion)
        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}