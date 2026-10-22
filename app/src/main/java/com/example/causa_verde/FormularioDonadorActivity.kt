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
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class FormularioDonadorActivity : AppCompatActivity() {

    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_donador)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val tipoDonador = intent.getStringExtra("TIPO_DONADOR") ?: "BAP"
        val tvTipoDonadorBadge = findViewById<TextView>(R.id.tv_tipo_donador_badge)

        val etRuc = findViewById<EditText>(R.id.et_ruc)
        val etRazonSocial = findViewById<EditText>(R.id.et_razon_social)
        val etTelefono = findViewById<EditText>(R.id.et_telefono)
        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val etApellidos = findViewById<EditText>(R.id.et_apellidos)
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val etConfirmPassword = findViewById<EditText>(R.id.et_confirm_password)
        val cbTerminos = findViewById<CheckBox>(R.id.cb_terminos)
        val tvErrorMessage = findViewById<TextView>(R.id.tv_error_message)

        val btnCrearCuentaManual = findViewById<MaterialButton>(R.id.btn_crear_cuenta_manual)
        val tvDivisorO = findViewById<TextView>(R.id.tv_divisor_o)
        val btnRegistrarseGoogle = findViewById<MaterialButton>(R.id.btn_registrarse_google)

        if (tipoDonador == "BAP") {
            tvTipoDonadorBadge.text = "CUENTA\nAsociado BAP"
            btnCrearCuentaManual.text = "Continuar"

            // --- 1. LÓGICA MANUAL (BAP) ---
            btnCrearCuentaManual.setOnClickListener {
                tvErrorMessage.visibility = View.GONE

                if (ValidatorUtil.estanCamposVacios(etRuc, etRazonSocial, etTelefono, etNombre, etApellidos, etCorreo, etPassword, etConfirmPassword)) {
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
                    return@setOnClickListener
                }

                btnCrearCuentaManual.isEnabled = false

                // CRÍTICO: Crear usuario en Firebase primero
                auth.createUserWithEmailAndPassword(correo, password)
                    .addOnSuccessListener {
                        btnCrearCuentaManual.isEnabled = true
                        val intent = Intent(this, VerificacionBapActivity::class.java).apply {
                            putExtra("RUC", etRuc.text.toString())
                            putExtra("RAZON_SOCIAL", etRazonSocial.text.toString())
                            putExtra("TELEFONO", etTelefono.text.toString())
                            putExtra("NOMBRE", etNombre.text.toString())
                            putExtra("APELLIDOS", etApellidos.text.toString())
                            putExtra("CORREO", correo)
                            putExtra("ROL", "DONADOR_BAP")
                        }
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        btnCrearCuentaManual.isEnabled = true
                        Toast.makeText(this, "Error al crear cuenta: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }

            // --- 2. LÓGICA GOOGLE (BAP) ---
            btnRegistrarseGoogle.setOnClickListener {
                // ... (Mantiene tu misma lógica de validación de Google)
                tvErrorMessage.visibility = View.GONE
                if (etRuc.text.toString().trim().isEmpty() || etRazonSocial.text.toString().trim().isEmpty() || etTelefono.text.toString().trim().isEmpty()) {
                    tvErrorMessage.text = "Ingresa RUC, Razón Social y Teléfono antes de continuar con Google."
                    tvErrorMessage.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (!cbTerminos.isChecked) {
                    tvErrorMessage.text = "Debes aceptar los términos y condiciones."
                    tvErrorMessage.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                iniciarRegistroGoogle(etRuc.text.toString(), etRazonSocial.text.toString(), etTelefono.text.toString())
            }

        } else {
            tvTipoDonadorBadge.text = "CUENTA\nOtros Comercios"
            btnCrearCuentaManual.text = "Crear Cuenta"

            // --- 1. LÓGICA MANUAL (REGULAR) ---
            btnCrearCuentaManual.setOnClickListener {
                tvErrorMessage.visibility = View.GONE

                if (ValidatorUtil.estanCamposVacios(etRuc, etRazonSocial, etTelefono, etNombre, etApellidos, etCorreo, etPassword, etConfirmPassword)) {
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
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    etConfirmPassword.error = "Las contraseñas no coinciden"
                    return@setOnClickListener
                }

                btnCrearCuentaManual.isEnabled = false

                // CRÍTICO: Crear usuario en Firebase primero
                auth.createUserWithEmailAndPassword(correo, password)
                    .addOnSuccessListener {
                        btnCrearCuentaManual.isEnabled = true
                        val intent = Intent(this, MapaUbicacionActivity::class.java).apply {
                            putExtra("RUC", etRuc.text.toString())
                            putExtra("RAZON_SOCIAL", etRazonSocial.text.toString())
                            putExtra("TELEFONO", etTelefono.text.toString())
                            putExtra("NOMBRE", etNombre.text.toString())
                            putExtra("APELLIDOS", etApellidos.text.toString())
                            putExtra("CORREO", correo)
                            putExtra("ROL", "DONADOR_REGULAR")
                        }
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        btnCrearCuentaManual.isEnabled = true
                        Toast.makeText(this, "Error Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }

            // --- 2. LÓGICA GOOGLE (REGULAR) ---
            btnRegistrarseGoogle.setOnClickListener {
                // ... (Mantiene tu misma lógica de validación de Google)
                tvErrorMessage.visibility = View.GONE
                if (etRuc.text.toString().trim().isEmpty() || etRazonSocial.text.toString().trim().isEmpty() || etTelefono.text.toString().trim().isEmpty()) {
                    tvErrorMessage.text = "Ingresa RUC, Razón Social y Teléfono antes de continuar con Google."
                    tvErrorMessage.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (!cbTerminos.isChecked) {
                    tvErrorMessage.text = "Debes aceptar los términos y condiciones."
                    tvErrorMessage.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                iniciarRegistroGoogle(etRuc.text.toString(), etRazonSocial.text.toString(), etTelefono.text.toString())
            }
        }
    }

    private fun iniciarRegistroGoogle(ruc: String, razonSocial: String, telefono: String) {
        lifecycleScope.launch {
            try {
                val defaultWebClientId = "75065692265-r4c9btpgao6p5ia2kg4k8p4a0k5j3ejh.apps.googleusercontent.com"

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(defaultWebClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(request = request, context = this@FormularioDonadorActivity)
                val credential = result.credential

                if (credential is GoogleIdTokenCredential) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    firebaseAuthWithGoogle(googleIdTokenCredential.idToken, ruc, razonSocial, telefono)
                }
            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "Error: ${e.message}")
                Toast.makeText(this@FormularioDonadorActivity, "Error al iniciar sesión con Google", Toast.LENGTH_LONG).show()
                credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, ruc: String, razonSocial: String, telefono: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val correo = user?.email ?: ""
                val nombreCompleto = user?.displayName ?: ""
                val tipoDonador = intent.getStringExtra("TIPO_DONADOR") ?: "BAP"

                if (tipoDonador == "BAP") {
                    val intent = Intent(this, VerificacionBapActivity::class.java).apply {
                        putExtra("CORREO", correo)
                        putExtra("NOMBRE", nombreCompleto)
                        putExtra("RUC", ruc)
                        putExtra("RAZON_SOCIAL", razonSocial)
                        putExtra("TELEFONO", telefono)
                        putExtra("ROL", "DONADOR_BAP")
                    }
                    startActivity(intent)
                } else {
                    val intent = Intent(this, MapaUbicacionActivity::class.java).apply {
                        putExtra("CORREO", correo)
                        putExtra("NOMBRE", nombreCompleto)
                        putExtra("RUC", ruc)
                        putExtra("RAZON_SOCIAL", razonSocial)
                        putExtra("TELEFONO", telefono)
                        putExtra("ROL", "DONADOR_REGULAR")
                    }
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error Firebase: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}