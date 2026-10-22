package com.example.causa_verde

import android.widget.EditText

object ValidatorUtil {
    // Mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial
    private val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-])(?=\\S+$).{8,}$".toRegex()

    fun validarPassword(password: String): Boolean {
        return PASSWORD_PATTERN.matches(password)
    }

    fun estanCamposVacios(vararg editTexts: EditText): Boolean {
        for (et in editTexts) {
            if (et.text.toString().trim().isEmpty()) {
                et.error = "Este campo no puede estar vacío"
                return true
            }
        }
        return false
    }
}
