package com.bitbrillantes.parqueoparatodos.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bitbrillantes.parqueoparatodos.MainActivity
import com.bitbrillantes.parqueoparatodos.databinding.ActivityLoginBinding
import com.bitbrillantes.parqueoparatodos.util.Validators
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    private lateinit var credentialManager: CredentialManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        if (auth.currentUser != null){
            if(!auth.currentUser!!.isEmailVerified){
                startActivity(Intent(this, EmailVerificationActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        credentialManager = CredentialManager.create(this)

        binding.loginButton.setOnClickListener { loginUser() }
        binding.forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.googleSignInButton.setOnClickListener { signInWithGoogle() }

        binding.facebookSignInButton.setOnClickListener {
            Toast.makeText(this, "Funcionalidad no implementada", Toast.LENGTH_SHORT).show()
        }

        binding.twitterSignInButton.setOnClickListener {
            Toast.makeText(this, "Funcionalidad no implementada", Toast.LENGTH_SHORT).show()
        }

    }

    private fun loginUser() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (!Validators.isValidEmail(email) || !Validators.isValidPassword(password)) {
            Toast.makeText(this, "Correo o contraseña inválidos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (user.isEmailVerified) {
                            updateUI(user)
                        } else {
                            Toast.makeText(baseContext, "Por favor, verifica tu correo electrónico.",
                                Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, EmailVerificationActivity::class.java))
                        }
                    }
                } else {
                    // Si falla, mostrar un mensaje al usuario
                    Toast.makeText(baseContext, "Error en el inicio de sesión. ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("258339083219-hd4o5nr4bbrrhhgmq7akh70d9rdf18ea.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    request = request, context = this@LoginActivity
                )
                handleSignInResult(result)
            } catch (e: GetCredentialException){
                Log.e(TAG, "Error al obtener credenciales", e)
                Toast.makeText(this@LoginActivity, "Error durante el inicio de sesión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is GoogleIdTokenCredential -> {
                val googleIdTokenString = credential.idToken
                firebaseAuthWithGoogle(googleIdTokenString)
            }

            else -> {
                Log.e("GoogleSignIn", "Tipo de credencial no soportado")
                Toast.makeText(
                    this,
                    "Error: Tipo de credencial no soportado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = withContext(Dispatchers.IO) {
                    auth.signInWithCredential(credential).await()
                }

                val user = authResult.user
                withContext(Dispatchers.Main) {
                    updateUI(user)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("FirebaseAuth", "Error en la autenticación con Firebase", e)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error en la autenticación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
        }

    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val displayName = user.displayName
            Toast.makeText(
                this,
                "Bienvenido ${displayName}!",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            Toast.makeText(baseContext, "Autenticación fallida.",
                Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}