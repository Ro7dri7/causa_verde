package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class RegistroRolActivity : AppCompatActivity() {

    private lateinit var cardComprador: MaterialCardView
    private lateinit var cardDonador: MaterialCardView
    private lateinit var tvSeleccionadoComprador: TextView
    private lateinit var tvSeleccionadoDonador: TextView
    private lateinit var btnContinuar: MaterialButton
    private lateinit var tvLoginLink: TextView
    private lateinit var btnBack: ImageButton

    private var rolElegido = "COMPRADOR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_rol)

        cardComprador = findViewById(R.id.card_comprador)
        cardDonador = findViewById(R.id.card_donador)
        tvSeleccionadoComprador = findViewById(R.id.tv_seleccionado_comprador)
        tvSeleccionadoDonador = findViewById(R.id.tv_seleccionado_donador)
        btnContinuar = findViewById(R.id.btn_continuar)
        tvLoginLink = findViewById(R.id.tv_iniciar_sesion_link)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        cardComprador.setOnClickListener {
            actualizarSeleccion("COMPRADOR")
        }

        cardDonador.setOnClickListener {
            actualizarSeleccion("DONADOR")
        }

        btnContinuar.setOnClickListener {
            if (rolElegido == "COMPRADOR") {
                val intent = Intent(this, RegistroCompradorActivity::class.java)
                startActivity(intent)
            } else if (rolElegido == "DONADOR") {
                val intent = Intent(this, RegistroDonadorActivity::class.java)
                startActivity(intent)
            }
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun actualizarSeleccion(rolSeleccionado: String) {
        rolElegido = rolSeleccionado
        if (rolSeleccionado == "COMPRADOR") {
            cardComprador.alpha = 1.0f
            cardComprador.strokeWidth = 4
            tvSeleccionadoComprador.visibility = View.VISIBLE

            cardDonador.alpha = 0.6f
            cardDonador.strokeWidth = 0
            tvSeleccionadoDonador.visibility = View.GONE
        } else if (rolSeleccionado == "DONADOR") {
            cardDonador.alpha = 1.0f
            cardDonador.strokeWidth = 4
            tvSeleccionadoDonador.visibility = View.VISIBLE

            cardComprador.alpha = 0.6f
            cardComprador.strokeWidth = 0
            tvSeleccionadoComprador.visibility = View.GONE
        }
    }
}
