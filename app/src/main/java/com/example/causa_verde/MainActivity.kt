package com.example.causa_verde

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                enableUserLocation()
            }
            else -> {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val btnBackMapa = findViewById<ImageButton>(R.id.btn_back_mapa)
        btnBackMapa.setOnClickListener {
            finish()
        }

        val btnConfirmarUbicacion = findViewById<MaterialButton>(R.id.btn_confirmar_ubicacion)
        btnConfirmarUbicacion.setOnClickListener {
            if (::mMap.isInitialized) {
                // Obtener coordenadas de la cámara del mapa
                val target = mMap.cameraPosition.target
                val lat = target.latitude
                val lng = target.longitude

                // Recuperar datos empaquetados del formulario anterior
                val password = intent.getStringExtra("PASSWORD")
                val correo = intent.getStringExtra("CORREO") ?: "Sin correo"
                val rol = intent.getStringExtra("ROL") ?: "Sin rol"
                val telefono = intent.getStringExtra("TELEFONO") ?: ""
                val ruc = intent.getStringExtra("RUC") ?: ""
                val razonSocial = intent.getStringExtra("RAZON_SOCIAL") ?: ""
                val nombre = intent.getStringExtra("NOMBRE") ?: ""
                val apellidos = intent.getStringExtra("APELLIDOS") ?: ""
                val nombreOlla = intent.getStringExtra("NOMBRE_OLLA") ?: ""
                val codigoBap = intent.getStringExtra("CODIGO_BAP") ?: ""

                // Función para procesar el guardado en Firebase y luego registrar en AWS
                fun procesarRegistroFinal(uid: String) {
                    val request = RegistroUsuarioRequest(
                        uid = uid,
                        rol = rol,
                        correo = correo,
                        telefono = telefono,
                        latitud = lat,
                        longitud = lng,
                        nombre = nombre,
                        apellidos = apellidos,
                        ruc = ruc,
                        razon_social = razonSocial,
                        nombre_olla = nombreOlla,
                        codigo_bap = codigoBap
                    )

                    Log.d("RegistroCausaVerde", "Iniciando envío a AWS: $request")
                    Toast.makeText(this@MainActivity, "Guardando perfil...", Toast.LENGTH_SHORT).show()

                    btnConfirmarUbicacion.isEnabled = false

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val response = RetrofitClient.apiService.registrarUsuarioFinal(request)
                            withContext(Dispatchers.Main) {
                                btnConfirmarUbicacion.isEnabled = true
                                if (response.isSuccessful) {
                                    Toast.makeText(this@MainActivity, "¡Registro completado en AWS!", Toast.LENGTH_SHORT).show()

                                    // RUTEO INTELIGENTE SEGÚN EL ROL DEL USUARIO
                                    val destinoActivity = if (rol.uppercase().contains("DONADOR")) {
                                        InicioDonadorActivity::class.java
                                    } else {
                                        InicioCompradorActivity::class.java
                                    }

                                    // Empaquetar los datos hacia la vista Home correspondiente
                                    val intentDestino = Intent(this@MainActivity, destinoActivity).apply {
                                        putExtra("NOMBRE", nombre)
                                        putExtra("APELLIDOS", apellidos)
                                        putExtra("DIRECCION", "Carabayllo, Lima") // O la dirección capturada
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
                                    Log.e("RegistroCausaVerde", "Error de API: ${response.code()} $errorBodyString")
                                    Toast.makeText(this@MainActivity, "Error en el servidor. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("RegistroCausaVerde", "Excepción de Red", e)
                            withContext(Dispatchers.Main) {
                                btnConfirmarUbicacion.isEnabled = true
                                Toast.makeText(this@MainActivity, "Error de conexión: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                if (!password.isNullOrEmpty()) {
                    // Flujo manual: Creando cuenta en Firebase Auth con correo y contraseña
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, password)
                        .addOnSuccessListener {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "UID_NO_ENCONTRADO"
                            procesarRegistroFinal(uid)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error de Firebase: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Flujo alternativo (ej. Google Sign-In si aplica)
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "UID_NO_ENCONTRADO"
                    procesarRegistroFinal(uid)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun enableUserLocation() {
        try {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}