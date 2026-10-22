package com.example.causa_verde

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventarioActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptadorReal: AdaptadorInventarioReal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario_donador)

        recyclerView = findViewById(R.id.rv_lista_inventario)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adaptadorReal = AdaptadorInventarioReal(emptyList())
        recyclerView.adapter = adaptadorReal

        val fabCrear = findViewById<FloatingActionButton>(R.id.fab_crear_publicacion)
        fabCrear.setOnClickListener {
            mostrarOpcionesDeCreacion()
        }

        findViewById<LinearLayout>(R.id.btn_nav_inicio)?.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()
        obtenerPublicacionesDesdeAWS()
    }

    private fun obtenerPublicacionesDesdeAWS() {
        RetrofitClient.apiService.obtenerPublicaciones().enqueue(object : Callback<List<PublicacionResponseItem>> {
            override fun onResponse(call: Call<List<PublicacionResponseItem>>, response: Response<List<PublicacionResponseItem>>) {
                if (response.isSuccessful && response.body() != null) {
                    adaptadorReal.actualizarLista(response.body()!!)
                } else {
                    Toast.makeText(this@InventarioActivity, "Error al cargar inventario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<PublicacionResponseItem>>, t: Throwable) {
                Log.e("AWS_INV", "Error: ${t.message}")
                Toast.makeText(this@InventarioActivity, "Error de red con AWS", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarOpcionesDeCreacion() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_opciones_inventario, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundResource(android.R.color.transparent)

        val btnCrearProducto = view.findViewById<MaterialCardView>(R.id.btn_opcion_crear_producto)
        btnCrearProducto.setOnClickListener {
            bottomSheetDialog.dismiss()
            val rolActual = intent.getStringExtra("ROL") ?: "DONADOR_REGULAR"
            val intentCrear = Intent(this, CrearProductoDonadorActivity::class.java)
            intentCrear.putExtra("ROL", rolActual)
            startActivity(intentCrear)
        }
        bottomSheetDialog.show()
    }

    inner class AdaptadorInventarioReal(private var lista: List<PublicacionResponseItem>) : RecyclerView.Adapter<AdaptadorInventarioReal.ViewHolder>() {

        fun actualizarLista(nuevaLista: List<PublicacionResponseItem>) {
            lista = nuevaLista
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarjeta_producto, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val producto = lista[position]

            holder.tvTitulo.text = producto.nombre_producto
            holder.tvCategoria.text = producto.categoria
            holder.tvPrecio.text = "S/ ${producto.precio_oferta}"

            if (!producto.foto_url.isNullOrEmpty()) {
                Glide.with(this@InventarioActivity)
                    .load(producto.foto_url)
                    .centerCrop()
                    .into(holder.ivImagen)
            }
        }

        override fun getItemCount(): Int = lista.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // Referencias exactas al XML corregido
            val ivImagen: ImageView = view.findViewById(R.id.iv_imagen_producto_tarjeta)
            val tvTitulo: TextView = view.findViewById(R.id.tv_titulo_producto_tarjeta)
            val tvCategoria: TextView = view.findViewById(R.id.tv_categoria_producto_tarjeta)
            val tvPrecio: TextView = view.findViewById(R.id.tv_precio_producto_tarjeta)
        }
    }
}