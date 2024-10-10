package com.bitbrillantes.parqueoparatodos.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bitbrillantes.parqueoparatodos.R
import com.bitbrillantes.parqueoparatodos.databinding.ActivityForgotPasswordBinding
import com.bitbrillantes.parqueoparatodos.util.Validators
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.resetPasswordButton.setOnClickListener { resetPassword() }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null)
        loadingDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

    }

    private fun resetPassword() {
        val email = binding.emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            binding.emailEditText.error = "Por favor, ingrese su correo electrónico"
            return
        }

        if(!Validators.isValidEmail(email)){
            binding.emailEditText.error = "Correo electrónico no válido"
            return
        }

        loadingDialog.show()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()
                if (task.isComplete && task.isSuccessful && task.exception == null) {
                    binding.emailEditText.text.clear()
                    binding.emailEditText.clearFocus()
                    binding.emailEditText.error = null
                    binding.emailEditText.hint = "Correo enviado"
                    Toast.makeText(this, "Si la dirección ingresada es correcta, usted recibirá un correo para restablecer su contraseña", Toast.LENGTH_LONG).show()
                    handler.postDelayed({
                        finish()
                    }, 4000)

                } else {
                    binding.emailEditText.error = "Correo no se encuentra registrado"
                    loadingDialog.dismiss()
                }
            }
    }
}