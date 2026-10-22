package com.example.causa_verde

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapaUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    // UI Elements
    private lateinit var etSearchAddress: EditText
    private lateinit var btnSearchAddress: ImageButton
    private lateinit var btnConfirmarRegistro: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBackMapa: ImageButton

    // Variables empaquetadas desde el Intent
    private var uid = ""
    private var correo = ""
    private var rol = ""
    private var telefono = ""
    private var ruc = ""
    private var razonSocial = ""
    private var nombre = ""
    private var apellidos = ""
    private var nombreOlla = ""
    private var codigoBap = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_ubicacion)

        // Inicializar UI
        etSearchAddress = findViewById(R.id.et_search_address)
        btnSearchAddress = findViewById(R.id.btn_search_address)
        btnConfirmarRegistro = findViewById(R.id.btn_confirmar_registro)
        progressBar = findViewById(R.id.progress_bar)
        btnBackMapa = findViewById(R.id.btn_back_mapa)

        // Listener para retroceder
        btnBackMapa.setOnClickListener {
            finish()
        }

        // Obtener UID de Firebase
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: "UID_NOT_FOUND"

        // Recuperar los datos del Intent
        correo = intent.getStringExtra("CORREO") ?: ""
        rol = intent.getStringExtra("ROL") ?: ""
        telefono = intent.getStringExtra("TELEFONO") ?: ""
        ruc = intent.getStringExtra("RUC") ?: ""
        razonSocial = intent.getStringExtra("RAZON_SOCIAL") ?: ""
        nombre = intent.getStringExtra("NOMBRE") ?: ""
        apellidos = intent.getStringExtra("APELLIDOS") ?: ""
        nombreOlla = intent.getStringExtra("NOMBRE_OLLA") ?: ""
        codigoBap = intent.getStringExtra("CODIGO_BAP") ?: ""

        // Configurar el mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Lógica de búsqueda (Click en la Lupa)
        btnSearchAddress.setOnClickListener {
            ejecutarBusqueda()
        }

        // Lógica de búsqueda (Enter en el teclado)
        etSearchAddress.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                ejecutarBusqueda()
                true
            } else {
                false
            }
        }

        // Listener para confirmar registro y ubicación
        btnConfirmarRegistro.setOnClickListener {
            if (::mMap.isInitialized) {
                confirmarRegistroYEnviarAWS()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val lima = LatLng(-12.046374, -77.042793)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12f))
    }

    private fun ejecutarBusqueda() {
        val query = etSearchAddress.text.toString().trim()
        if (query.isNotEmpty()) {
            buscarDireccion(query)
        } else {
            Toast.makeText(this, "Ingresa una dirección a buscar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscarDireccion(direccion: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val geocoder = Geocoder(this@MapaUbicacionActivity, Locale.getDefault())
            try {
                val addressList: MutableList<Address>? = geocoder.getFromLocationName(direccion, 1)

                withContext(Dispatchers.Main) {
                    if (addressList != null && addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                    } else {
                        Toast.makeText(this@MapaUbicacionActivity, "No se encontró la dirección", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapaUbicacionActivity, "Error al buscar la dirección", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun confirmarRegistroYEnviarAWS() {
        val target = mMap.cameraPosition.target
        val latitud = target.latitude
        val longitud = target.longitude

        progressBar.visibility = View.VISIBLE
        btnConfirmarRegistro.isEnabled = false

        // Usamos tu clase de solicitud exacta: RegistroUsuarioRequest
        val request = RegistroUsuarioRequest(
            uid = uid,
            rol = rol,
            correo = correo,
            telefono = telefono,
            latitud = latitud,
            longitud = longitud,
            nombre = nombre,
            apellidos = apellidos,
            ruc = ruc,
            razon_social = razonSocial,
            nombre_olla = nombreOlla,
            codigo_bap = codigoBap
        )

        Log.d("AWS_API", "Enviando datos: $request")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Corrección: Usamos RetrofitClient.apiService
                val response = RetrofitClient.apiService.registrarUsuarioFinal(request)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnConfirmarRegistro.isEnabled = true

                    if (response.isSuccessful) {
                        Toast.makeText(this@MapaUbicacionActivity, "Registro completado exitosamente en AWS", Toast.LENGTH_SHORT).show()

                        // RUTEO INTELIGENTE SEGÚN EL ROL DEL USUARIO
                        val destinoActivity = if (rol.uppercase().contains("DONADOR")) {
                            InicioDonadorActivity::class.java
                        } else {
                            InicioCompradorActivity::class.java
                        }

                        // Empaquetar los datos hacia la vista Home correspondiente
                        val intentDestino = Intent(this@MapaUbicacionActivity, destinoActivity).apply {
                            putExtra("NOMBRE", nombre)
                            putExtra("APELLIDOS", apellidos)
                            putExtra("DIRECCION", etSearchAddress.text.toString().takeIf { it.isNotEmpty() } ?: "Ubicación seleccionada")
                            putExtra("ROL", rol)

                            if (nombreOlla.isNotEmpty()) {
                                putExtra("NOMBRE_OLLA", nombreOlla)
                            }
                            if (razonSocial.isNotEmpty()) {
                                putExtra("RAZON_SOCIAL", razonSocial)
                            }

                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intentDestino)
                        finishAffinity()
                    } else {
                        val errorBodyString = response.errorBody()?.string() ?: "Sin detalle"
                        Log.e("AWS_API", "Error del servidor: ${response.code()} $errorBodyString")
                        Toast.makeText(this@MapaUbicacionActivity, "Error en el servidor al registrar. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AWS_API", "Error de red", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnConfirmarRegistro.isEnabled = true
                    Toast.makeText(this@MapaUbicacionActivity, "Error de red: Revisa tu conexión a internet", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}