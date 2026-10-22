package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val client = OkHttpClient()

    // Dirección IP de tu servidor EC2 en AWS donde corre FastAPI
    private val BASE_URL = "http://16.59.55.5:8001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Referencias basadas exactamente en tu XML actual
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etPassword = findViewById<EditText>(R.id.et_contrasena)
        val btnLogin = findViewById<MaterialButton>(R.id.btn_login)

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false

            // 1. Iniciar sesión en Firebase Auth
            auth.signInWithEmailAndPassword(correo, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val uid = firebaseUser?.uid

                    if (uid != null) {
                        // 2. Consultar el perfil del usuario en tu API de AWS / FastAPI
                        obtenerDatosUsuarioDesdeAWS(uid, btnLogin)
                    } else {
                        btnLogin.isEnabled = true
                        Toast.makeText(this, "Error: No se pudo obtener el UID de Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    btnLogin.isEnabled = true
                    Toast.makeText(this, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun obtenerDatosUsuarioDesdeAWS(uid: String, btnLogin: MaterialButton) {
        val url = "$BASE_URL/api/usuarios/$uid"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    btnLogin.isEnabled = true
                    Log.e("AWS_API", "Error de red al buscar usuario", e)
                    // AHORA MOSTRARÁ ERROR EN LUGAR DE ENVIARTE A LA VISTA EQUIVOCADA
                    Toast.makeText(this@LoginActivity, "Error de red: No se pudo conectar a AWS", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    btnLogin.isEnabled = true
                }
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        try {
                            val json = JSONObject(responseData)
                            val nombre = json.optString("nombre", "Usuario")
                            val apellidos = json.optString("apellidos", "")
                            val direccion = json.optString("direccion", "Carabayllo, Lima")
                            val rol = json.optString("rol", "COMPRADOR_REGULAR")
                            val nombreOlla = json.optString("nombre_olla", null)
                            val razonSocial = json.optString("razon_social", null)

                            runOnUiThread {
                                navegarAInicio(nombre, apellidos, direccion, rol, nombreOlla, razonSocial)
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Log.e("AWS_API", "Error al leer JSON", e)
                                Toast.makeText(this@LoginActivity, "Error al procesar los datos de AWS", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Error: Usuario no encontrado en AWS (Código ${response.code})", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun navegarAInicio(nombre: String, apellidos: String, direccion: String, rol: String, nombreOlla: String?, razonSocial: String?) {

        // Ruteo inteligente y blindado contra mayúsculas/minúsculas
        val destinoActivity = if (rol.uppercase().contains("DONADOR")) {
            InicioDonadorActivity::class.java
        } else {
            InicioCompradorActivity::class.java
        }

        val intent = Intent(this, destinoActivity).apply {
            putExtra("NOMBRE", nombre)
            putExtra("APELLIDOS", apellidos)
            putExtra("DIRECCION", direccion)
            putExtra("ROL", rol)

            if (!nombreOlla.isNullOrEmpty()) {
                putExtra("NOMBRE_OLLA", nombreOlla)
            }
            if (!razonSocial.isNullOrEmpty()) {
                putExtra("RAZON_SOCIAL", razonSocial)
            }
        }
        startActivity(intent)
        finish()
    }
}