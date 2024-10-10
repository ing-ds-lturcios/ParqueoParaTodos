package com.bitbrillantes.parqueoparatodos.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bitbrillantes.parqueoparatodos.MainActivity
import com.bitbrillantes.parqueoparatodos.databinding.ActivityEmailVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else if (auth.currentUser!!.isEmailVerified) {
            Toast.makeText(this, "Tu correo ya ha sido verificado", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.resendEmailButton.setOnClickListener { resendVerificationEmail() }
        binding.backToLoginButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("emailVerification", true)
            startActivity(intent)
            finish()
        }

        runnable = Runnable { checkEmailVerification() }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 5000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
    }

    private fun checkEmailVerification() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Por favor, verifica tu correo electrónico", Toast.LENGTH_SHORT).show()
                    handler.postDelayed(runnable, 10000)
                }
            }
        }
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        Log.d("EmailVerificationActivity", "Resending email verification ${user?.email}")
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Se ha reenviado el correo de verificación", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al reenviar el correo: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}