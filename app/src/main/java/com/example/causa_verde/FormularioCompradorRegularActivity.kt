package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class FormularioCompradorRegularActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_comprador_regular)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        // Vistas
        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val etApellidos = findViewById<EditText>(R.id.et_apellidos)
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etTelefono = findViewById<EditText>(R.id.et_telefono)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val cbTerminos = findViewById<CheckBox>(R.id.cb_terminos)
        val tvErrorMessage = findViewById<TextView>(R.id.tv_error_message)

        // Botones
        val btnCrearCuentaManual = findViewById<MaterialButton>(R.id.btn_crear_cuenta_manual)
        val btnRegistrarseGoogle = findViewById<MaterialButton>(R.id.btn_registrarse_google)

        // --- 1. LÓGICA DEL BOTÓN: CREAR CUENTA (FLUJO MANUAL) ---
        btnCrearCuentaManual.setOnClickListener {
            tvErrorMessage.visibility = View.GONE

            if (ValidatorUtil.estanCamposVacios(etNombre, etApellidos, etCorreo, etTelefono, etPassword, etConfirmPassword)) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!cbTerminos.isChecked) {
                Toast.makeText(this, "Debes aceptar los Términos y Condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val correo = etCorreo.text.toString()

            if (!ValidatorUtil.validarPassword(password)) {
                etPassword.error = "Mín. 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial"
                Toast.makeText(this, "La contraseña no es segura", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnCrearCuentaManual.isEnabled = false

            // CRÍTICO: Crear usuario en Firebase primero
            auth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener {
                    btnCrearCuentaManual.isEnabled = true

                    // Navegar al Mapa (corregido: antes iba a MainActivity)
                    val intent = Intent(this, MapaUbicacionActivity::class.java).apply {
                        putExtra("NOMBRE", etNombre.text.toString())
                        putExtra("APELLIDOS", etApellidos.text.toString())
                        putExtra("CORREO", correo)
                        putExtra("TELEFONO", etTelefono.text.toString())
                        putExtra("ROL", "COMPRADOR_REGULAR")
                    }
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    btnCrearCuentaManual.isEnabled = true
                    Toast.makeText(this, "Error al crear cuenta: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // --- 2. LÓGICA DEL BOTÓN: REGISTRARSE CON GOOGLE (FLUJO CONDICIONADO) ---
        btnRegistrarseGoogle.setOnClickListener {
            tvErrorMessage.visibility = View.GONE

            // Solo validar el campo de rol específico y términos
            if (etTelefono.text.toString().trim().isEmpty()) {
                tvErrorMessage.text = "Ingresa tu número de celular antes de continuar con Google."
                tvErrorMessage.visibility = View.VISIBLE
                etTelefono.error = "Requerido"
                return@setOnClickListener
            }

            if (!cbTerminos.isChecked) {
                tvErrorMessage.text = "Debes aceptar los términos y condiciones antes de continuar."
                tvErrorMessage.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Si los extras son válidos, iniciar CredentialManager
            iniciarRegistroGoogle(etTelefono.text.toString())
        }
    }

    private fun iniciarRegistroGoogle(telefono: String) {
        lifecycleScope.launch {
            try {
                val defaultWebClientId = "75065692265-r4c9btpgao6p5ia2kg4k8p4a0k5j3ejh.apps.googleusercontent.com"
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(defaultWebClientId)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = this@FormularioCompradorRegularActivity
                )

                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken, telefono)
                } else {
                    Toast.makeText(this@FormularioCompradorRegularActivity, "Credencial no soportada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Error: ${e.message}")
                Toast.makeText(this@FormularioCompradorRegularActivity, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                try {
                    credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                } catch (ex: Exception) {
                    Log.e("GoogleSignIn", "Failed to clear credential state", ex)
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, telefono: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val nombreCompleto = user?.displayName ?: ""
                val correo = user?.email ?: ""

                Toast.makeText(this, "Autenticación de Google exitosa", Toast.LENGTH_SHORT).show()

                // Salto al mapa
                val intent = Intent(this, MapaUbicacionActivity::class.java).apply {
                    putExtra("NOMBRE", nombreCompleto)
                    putExtra("CORREO", correo)
                    putExtra("TELEFONO", telefono)
                    putExtra("ROL", "COMPRADOR_REGULAR")
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}