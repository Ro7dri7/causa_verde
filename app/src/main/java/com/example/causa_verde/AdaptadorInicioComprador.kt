package com.example.causa_verde

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdaptadorInicioComprador(private val listaPublicaciones: List<PublicacionResponseItem>) :
    RecyclerView.Adapter<AdaptadorInicioComprador.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoria: TextView = itemView.findViewById(R.id.tv_categoria_comprador)
        val ivFoto: ImageView = itemView.findViewById(R.id.iv_foto_comprador)
        val tvPrecioOferta: TextView = itemView.findViewById(R.id.tv_precio_oferta_comprador)
        val tvPrecioRegular: TextView = itemView.findViewById(R.id.tv_precio_regular_comprador)
        val tvNombreProducto: TextView = itemView.findViewById(R.id.tv_nombre_comprador)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarjeta_comprador, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaPublicaciones[position]

        holder.tvNombreProducto.text = item.nombre_producto
        holder.tvCategoria.text = item.categoria
        holder.tvPrecioOferta.text = "S/ ${item.precio_oferta}"
        holder.tvPrecioRegular.text = "S/ ${item.precio_regular}"

        // Cargar imagen de AWS S3 de forma fluida con Glide
        if (!item.foto_url.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.foto_url)
                .placeholder(R.drawable.logo_inicio)
                .into(holder.ivFoto)
        }
    }

    override fun getItemCount(): Int = listaPublicaciones.size
}