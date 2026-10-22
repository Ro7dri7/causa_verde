package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class RegistroDonadorActivity : AppCompatActivity() {

    private lateinit var cardBap: MaterialCardView
    private lateinit var cardOtros: MaterialCardView
    private lateinit var tvSeleccionadoBap: TextView
    private lateinit var tvSeleccionadoOtros: TextView
    private lateinit var btnContinuar: MaterialButton
    private lateinit var tvLoginLink: TextView
    private lateinit var btnBack: ImageButton

    private var rolSeleccionado = "BAP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_donador)

        cardBap = findViewById(R.id.card_bap)
        cardOtros = findViewById(R.id.card_otros)
        tvSeleccionadoBap = findViewById(R.id.tv_seleccionado_bap)
        tvSeleccionadoOtros = findViewById(R.id.tv_seleccionado_otros)
        btnContinuar = findViewById(R.id.btn_continuar_donador)
        tvLoginLink = findViewById(R.id.tv_login_link_donador)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        cardBap.setOnClickListener {
            actualizarSeleccion("BAP")
        }

        cardOtros.setOnClickListener {
            actualizarSeleccion("OTROS")
        }

        btnContinuar.setOnClickListener {
            val intent = Intent(this, FormularioDonadorActivity::class.java)
            intent.putExtra("TIPO_DONADOR", rolSeleccionado)
            startActivity(intent)
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun actualizarSeleccion(tipo: String) {
        rolSeleccionado = tipo
        if (tipo == "BAP") {
            cardBap.alpha = 1.0f
            cardBap.strokeWidth = 4
            tvSeleccionadoBap.visibility = View.VISIBLE

            cardOtros.alpha = 0.6f
            cardOtros.strokeWidth = 0
            tvSeleccionadoOtros.visibility = View.GONE
        } else if (tipo == "OTROS") {
            cardOtros.alpha = 1.0f
            cardOtros.strokeWidth = 4
            tvSeleccionadoOtros.visibility = View.VISIBLE

            cardBap.alpha = 0.6f
            cardBap.strokeWidth = 0
            tvSeleccionadoBap.visibility = View.GONE
        }
    }
}
