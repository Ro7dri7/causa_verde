package com.example.causa_verde

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class DetalleProductoBottomSheet : BottomSheetDialogFragment() {

    private var cantidad = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el XML que acabamos de crear
        return inflater.inflate(R.layout.layout_detalle_producto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aplicar el efecto tachado al precio viejo
        val tvPrecioTachado = view.findViewById<TextView>(R.id.tv_precio_tachado)
        tvPrecioTachado.paintFlags = tvPrecioTachado.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Configurar botones de Cantidad
        val btnRestar = view.findViewById<TextView>(R.id.btn_restar)
        val btnSumar = view.findViewById<TextView>(R.id.btn_sumar)
        val tvCantidad = view.findViewById<TextView>(R.id.tv_cantidad)
        val tvTotalLabel = view.findViewById<TextView>(R.id.tv_total_label)

        btnSumar.setOnClickListener {
            cantidad++
            tvCantidad.text = cantidad.toString()
            tvTotalLabel.text = "Total ($cantidad paquetes)"
        }

        btnRestar.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                tvCantidad.text = cantidad.toString()
                tvTotalLabel.text = "Total ($cantidad paquetes)"
            }
        }

        // Configurar el botón para cerrar la hoja deslizable
        val btnVolver = view.findViewById<FrameLayout>(R.id.btn_volver_detalle)
        btnVolver.setOnClickListener {
            dismiss() // Cierra el panel deslizable
        }

        // Configurar el botón Agregar
        val btnAgregarCarrito = view.findViewById<MaterialButton>(R.id.btn_agregar_carrito)
        btnAgregarCarrito.setOnClickListener {
            Toast.makeText(context, "Agregado $cantidad paquete(s) al carrito", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // Obliga al panel a desplegarse por completo hacia arriba al abrirse
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}