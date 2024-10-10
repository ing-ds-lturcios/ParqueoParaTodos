package com.bitbrillantes.parqueoparatodos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bitbrillantes.parqueoparatodos.auth.EmailVerificationActivity
import com.bitbrillantes.parqueoparatodos.auth.LoginActivity
import com.bitbrillantes.parqueoparatodos.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else if (!auth.currentUser!!.isEmailVerified) {
            startActivity(Intent(this, EmailVerificationActivity::class.java))
            finish()
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.textView.text = "Bienvenido (a<) ${auth.currentUser?.displayName}"

    }
}