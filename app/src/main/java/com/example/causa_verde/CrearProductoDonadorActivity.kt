package com.example.causa_verde

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import kotlin.math.roundToInt

class CrearProductoDonadorActivity : AppCompatActivity() {

    private lateinit var ivFotoPreview: ImageView
    private lateinit var llPlaceholderFoto: LinearLayout
    private lateinit var etNombreProducto: EditText
    private lateinit var etEstadoProducto: EditText
    private lateinit var etPrecioNormal: EditText
    private lateinit var etPrecioRescate: EditText
    private lateinit var etLoteBap: EditText
    private lateinit var etLimiteRetiro: EditText
    private lateinit var etFechaVencimiento: EditText
    private lateinit var btnPublicar: MaterialButton

    private var imagenSeleccionadaBitmap: Bitmap? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            ivFotoPreview.setImageURI(uri)
            llPlaceholderFoto.visibility = View.GONE
            try {
                imagenSeleccionadaBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            ivFotoPreview.setImageBitmap(bitmap)
            llPlaceholderFoto.visibility = View.GONE
            imagenSeleccionadaBitmap = bitmap
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_producto_donador)

        ivFotoPreview = findViewById(R.id.iv_foto_producto_preview)
        llPlaceholderFoto = findViewById(R.id.ll_placeholder_foto)
        etNombreProducto = findViewById(R.id.et_nombre_producto)
        etEstadoProducto = findViewById(R.id.et_estado_producto)
        etPrecioNormal = findViewById(R.id.et_precio_normal)
        etPrecioRescate = findViewById(R.id.et_precio_rescate)
        etLoteBap = findViewById(R.id.et_lote_bap)
        etLimiteRetiro = findViewById(R.id.et_limite_retiro)
        etFechaVencimiento = findViewById(R.id.et_fecha_vencimiento)
        btnPublicar = findViewById(R.id.btn_publicar)

        // Configurar selectores automáticos de Fecha y Hora para evitar tipeo manual
        configurarSelectoresFechaHora()

        val rolUsuario = intent.getStringExtra("ROL") ?: "DONADOR_REGULAR"
        val tvEtiquetaRol = findViewById<TextView>(R.id.tv_etiqueta_rol)
        val cardSeccionPrecios = findViewById<MaterialCardView>(R.id.card_seccion_precios)
        val containerLoteAuditoria = findViewById<View>(R.id.container_lote_auditoria)

        if (rolUsuario == "DONADOR_BAP") {
            tvEtiquetaRol.text = "ASOCIADO BAP"
            tvEtiquetaRol.setBackgroundColor(android.graphics.Color.parseColor("#C0EBE8"))
            cardSeccionPrecios.visibility = View.GONE
            containerLoteAuditoria.visibility = View.VISIBLE
        } else {
            tvEtiquetaRol.text = "DONADOR ACTIVO"
            cardSeccionPrecios.visibility = View.VISIBLE
            containerLoteAuditoria.visibility = View.GONE
        }

        val tvPorcentajeCalculado = findViewById<TextView>(R.id.tv_porcentaje_calculado)
        val textWatcherPrecios = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val normal = etPrecioNormal.text.toString().toDoubleOrNull() ?: 0.0
                val rescate = etPrecioRescate.text.toString().toDoubleOrNull() ?: 0.0
                if (normal > 0.0 && rescate < normal && rescate >= 0.0) {
                    val porcentaje = (((normal - rescate) / normal) * 100).roundToInt()
                    tvPorcentajeCalculado.text = "-${porcentaje}% Recomendado"
                } else {
                    tvPorcentajeCalculado.text = "0% Recomendado"
                }
            }
        }
        etPrecioNormal.addTextChangedListener(textWatcherPrecios)
        etPrecioRescate.addTextChangedListener(textWatcherPrecios)

        findViewById<MaterialCardView>(R.id.card_foto_producto).setOnClickListener {
            mostrarOpcionesDeCamara()
        }

        findViewById<ImageView>(R.id.btn_volver).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btn_cancelar).setOnClickListener { finish() }

        btnPublicar.setOnClickListener {
            enviarProductoServidor()
        }
    }

    private fun configurarSelectoresFechaHora() {
        // Selector de Hora para Límite de Retiro
        etLimiteRetiro.setOnClickListener {
            val c = Calendar.getInstance()
            val horaActual = c.get(Calendar.HOUR_OF_DAY)
            val minutoActual = c.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, hourOfDay, minute ->
                val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                etLimiteRetiro.setText(horaFormateada)
            }, horaActual, minutoActual, true).show()
        }
        etLimiteRetiro.isFocusable = false
        etLimiteRetiro.isClickable = true

        // Selector de Calendario para Fecha de Vencimiento
        etFechaVencimiento.setOnClickListener {
            val c = Calendar.getInstance()
            val anio = c.get(Calendar.YEAR)
            val mes = c.get(Calendar.MONTH)
            val dia = c.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val fechaFormateada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                etFechaVencimiento.setText(fechaFormateada)
            }, anio, mes, dia).show()
        }
        etFechaVencimiento.isFocusable = false
        etFechaVencimiento.isClickable = true
    }

    private fun mostrarOpcionesDeCamara() {
        val dialog = BottomSheetDialog(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 60, 60, 60)
        }
        val titulo = TextView(this).apply {
            text = "Añadir Foto"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 40)
        }
        val btnCamara = TextView(this).apply {
            text = "📷 Tomar Foto"
            textSize = 16f
            setPadding(0, 40, 0, 40)
            setOnClickListener {
                dialog.dismiss()
                if (ContextCompat.checkSelfPermission(this@CrearProductoDonadorActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(null)
                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
        val btnGaleria = TextView(this).apply {
            text = "🖼️ Elegir de la Galería"
            textSize = 16f
            setPadding(0, 40, 0, 40)
            setOnClickListener {
                dialog.dismiss()
                galleryLauncher.launch("image/*")
            }
        }
        layout.addView(titulo)
        layout.addView(btnCamara)
        layout.addView(btnGaleria)
        dialog.setContentView(layout)
        dialog.show()
    }

    private fun crearPartFromString(texto: String): RequestBody {
        return texto.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun enviarProductoServidor() {
        if (imagenSeleccionadaBitmap == null) {
            Toast.makeText(this, "Por favor añade una foto del producto", Toast.LENGTH_SHORT).show()
            return
        }

        val nombre = etNombreProducto.text.toString().trim()
        val estado = etEstadoProducto.text.toString().trim()
        val precioNormal = etPrecioNormal.text.toString().toDoubleOrNull() ?: 0.0
        val precioRescate = etPrecioRescate.text.toString().toDoubleOrNull() ?: 0.0
        val limiteRetiro = etLimiteRetiro.text.toString().trim()
        val fechaVencimiento = etFechaVencimiento.text.toString().trim()
        val loteBap = etLoteBap.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre del producto", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Subiendo imagen a AWS S3...", Toast.LENGTH_LONG).show()
        btnPublicar.isEnabled = false

        val idUsuarioPart = crearPartFromString("2")
        val nombrePart = crearPartFromString(nombre)
        val categoriaPart = crearPartFromString("Alimentos")
        val precioRegularPart = crearPartFromString(precioNormal.toString())
        val precioOfertaPart = crearPartFromString(precioRescate.toString())
        val stockPart = crearPartFromString("10")
        val fechaPart = crearPartFromString(if (fechaVencimiento.isEmpty()) "Sin fecha" else fechaVencimiento)
        val estadoPart = crearPartFromString(if (estado.isEmpty()) "Buen estado" else estado)
        val limitePart = crearPartFromString(if (limiteRetiro.isEmpty()) "Cierre de tienda" else limiteRetiro)
        val lotePart = if (loteBap.isEmpty()) null else crearPartFromString(loteBap)

        val file = File(cacheDir, "imagen_producto.jpg")
        file.createNewFile()
        val fos = FileOutputStream(file)
        imagenSeleccionadaBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, fos)
        fos.flush()
        fos.close()

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val fotoPart = MultipartBody.Part.createFormData("foto", file.name, requestFile)

        RetrofitClient.apiService.publicarProductoConImagen(
            idUsuarioPart, nombrePart, categoriaPart, precioRegularPart,
            precioOfertaPart, stockPart, fechaPart, estadoPart, lotePart, limitePart, fotoPart
        ).enqueue(object : Callback<PublicacionResponse> {
            override fun onResponse(call: Call<PublicacionResponse>, response: Response<PublicacionResponse>) {
                btnPublicar.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@CrearProductoDonadorActivity, "¡Producto en la nube con éxito!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AWS_S3", "Error HTTP ${response.code()}: $errorBody")
                    Toast.makeText(this@CrearProductoDonadorActivity, "Error al subir a S3", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PublicacionResponse>, t: Throwable) {
                btnPublicar.isEnabled = true
                Log.e("AWS_S3", "Fallo de red: ${t.message}")
                Toast.makeText(this@CrearProductoDonadorActivity, "Fallo de red: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }
}