package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VerificacionBapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificacion_bap)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        // Recuperar todos los datos del formulario anterior
        val ruc = intent.getStringExtra("RUC") ?: ""
        val razonSocial = intent.getStringExtra("RAZON_SOCIAL") ?: ""
        val telefono = intent.getStringExtra("TELEFONO") ?: ""
        val nombre = intent.getStringExtra("NOMBRE") ?: ""
        val apellidos = intent.getStringExtra("APELLIDOS") ?: ""
        val correo = intent.getStringExtra("CORREO") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""
        val rol = intent.getStringExtra("ROL") ?: "DONADOR_BAP"

        val etCodigoBap = findViewById<EditText>(R.id.et_codigo_bap)
        val btnValidarBap = findViewById<MaterialButton>(R.id.btn_validar_bap)

        btnValidarBap.setOnClickListener {
            if (ValidatorUtil.estanCamposVacios(etCodigoBap)) {
                Toast.makeText(this, "Por favor ingresa el código", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Una vez ingresado el código, saltar directo al mapa con todos los datos
            val intent = Intent(this, MapaUbicacionActivity::class.java).apply {
                putExtra("CODIGO_BAP", etCodigoBap.text.toString())
                putExtra("RUC", ruc)
                putExtra("RAZON_SOCIAL", razonSocial)
                putExtra("TELEFONO", telefono)
                putExtra("NOMBRE", nombre)
                putExtra("APELLIDOS", apellidos)
                putExtra("CORREO", correo)
                putExtra("PASSWORD", password)
                putExtra("ROL", rol)
            }
            startActivity(intent)
        }
    }
}