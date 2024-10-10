package com.bitbrillantes.parqueoparatodos.util

object Validators {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        // Implementa tus reglas de validación de contraseña
        return password.length in 8..20
                && password.contains(Regex("[A-Z]"))
                && password.contains(Regex("[a-z]"))
                && password.contains(Regex("[0-9]"))
                && password.contains(Regex("[^A-Za-z0-9]"))
    }

}