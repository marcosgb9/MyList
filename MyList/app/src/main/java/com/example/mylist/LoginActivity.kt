package com.example.mylist

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegistrar: Button
    private lateinit var tvOlvidar: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Vincular vistas
        etEmail = findViewById(R.id.etEmail)
        etContrasena = findViewById(R.id.etContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvOlvidar = findViewById(R.id.tvOlvidar)

        // Botón Login
        btnLogin.setOnClickListener { loginUsuario() }

        // Botón Registrar -> abrir la pantalla de registro
        btnRegistrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Olvidaste contraseña
        tvOlvidar.setOnClickListener {
            val emailInput = EditText(this).apply { hint = "Introduce tu correo" }

            AlertDialog.Builder(this)
                .setTitle("Restablecer contraseña")
                .setMessage("Ingresa tu correo electrónico para recibir el enlace:")
                .setView(emailInput)
                .setPositiveButton("Enviar") { _, _ ->
                    val email = emailInput.text.toString().trim()
                    if (email.isEmpty()) {
                        Toast.makeText(this, "Debes ingresar un correo", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            val mensaje = if (task.isSuccessful)
                                "Correo de recuperación de contraseña enviado a $email"
                            else
                                "Error: ${task.exception?.message}"
                            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun loginUsuario() {
        val email = etEmail.text.toString().trim()
        val password = etContrasena.text.toString().trim()

        // Validación
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Iniciar sesión con Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                    // Navega a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // Mostrar error
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
