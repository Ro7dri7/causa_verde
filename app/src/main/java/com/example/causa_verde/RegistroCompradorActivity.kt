package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class RegistroCompradorActivity : AppCompatActivity() {

    private lateinit var cardRegular: MaterialCardView
    private lateinit var cardLider: MaterialCardView
    private lateinit var tvSeleccionadoRegular: TextView
    private lateinit var tvSeleccionadoLider: TextView
    private lateinit var btnContinuar: MaterialButton
    private lateinit var tvLoginLink: TextView
    private lateinit var btnBack: ImageButton

    private var rolSeleccionado = "REGULAR"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_comprador)

        cardRegular = findViewById(R.id.card_regular)
        cardLider = findViewById(R.id.card_lider)
        tvSeleccionadoRegular = findViewById(R.id.tv_seleccionado_regular)
        tvSeleccionadoLider = findViewById(R.id.tv_seleccionado_lider)
        btnContinuar = findViewById(R.id.btn_continuar_comprador)
        tvLoginLink = findViewById(R.id.tv_login_link_comprador)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            finish()
        }

        cardRegular.setOnClickListener {
            actualizarSeleccion("REGULAR")
        }

        cardLider.setOnClickListener {
            actualizarSeleccion("LIDER")
        }

        btnContinuar.setOnClickListener {
            if (rolSeleccionado == "REGULAR") {
                val intent = Intent(this, FormularioCompradorRegularActivity::class.java)
                startActivity(intent)
            } else if (rolSeleccionado == "LIDER") {
                val intent = Intent(this, FormularioCompradorOllaActivity::class.java)
                startActivity(intent)
            }
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun actualizarSeleccion(tipo: String) {
        rolSeleccionado = tipo
        if (tipo == "REGULAR") {
            cardRegular.alpha = 1.0f
            cardRegular.strokeWidth = 4
            tvSeleccionadoRegular.visibility = View.VISIBLE

            cardLider.alpha = 0.6f
            cardLider.strokeWidth = 0
            tvSeleccionadoLider.visibility = View.GONE
        } else if (tipo == "LIDER") {
            cardLider.alpha = 1.0f
            cardLider.strokeWidth = 4
            tvSeleccionadoLider.visibility = View.VISIBLE

            cardRegular.alpha = 0.6f
            cardRegular.strokeWidth = 0
            tvSeleccionadoRegular.visibility = View.GONE
        }
    }
}
