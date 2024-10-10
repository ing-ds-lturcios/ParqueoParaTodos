package com.bitbrillantes.parqueoparatodos.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bitbrillantes.parqueoparatodos.MainActivity
import com.bitbrillantes.parqueoparatodos.R
import com.bitbrillantes.parqueoparatodos.databinding.ActivityRegisterBinding
import com.bitbrillantes.parqueoparatodos.util.Validators
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.registerButton.setOnClickListener { registerUser() }
        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val repeatPassword = binding.repeatPasswordEditText.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != repeatPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Validators.isValidPassword(password)) {
            Toast.makeText(this, "La contraseña no cumple con los requisitos", Toast.LENGTH_SHORT).show()
            return
        }

        if(!Validators.isValidEmail(email)) {
            Toast.makeText(this, "El correo ingresado no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressBar(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email
                        )

                        FirebaseFirestore.getInstance().collection("users")
                            .document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                updateProfile(user, name)
                            }
                            .addOnFailureListener { e ->
                                showProgressBar(false)
                                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    showProgressBar(false)
                    Toast.makeText(baseContext, "Error en el registro: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateProfile(user: FirebaseUser?, name: String) {
        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    sendVerificationEmail(user)
                } else {
                    Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendVerificationEmail(user: FirebaseUser){
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de verificación enviado", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, EmailVerificationActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error al enviar correo de verificación", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.registerButton.isEnabled = !show
        binding.nameEditText.isEnabled = !show
        binding.emailEditText.isEnabled = !show
        binding.passwordEditText.isEnabled = !show
        binding.loginTextView.isEnabled = !show
    }
}