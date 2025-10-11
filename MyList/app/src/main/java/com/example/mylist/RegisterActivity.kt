package com.example.mylist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPass: EditText
    private lateinit var etPass2: EditText
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolverLogin: Button
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Vincular vistas
        etEmail = findViewById(R.id.etEmail)
        etPass = findViewById(R.id.etPass)
        etPass2 = findViewById(R.id.etPass2)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVolverLogin = findViewById(R.id.btnVolverLogin)

        // Botón para volver al Login
        btnVolverLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Botón para registrar usuario
        btnRegistrar.setOnClickListener { registrarUsuario() }
    }

    private fun registrarUsuario() {
        val email = etEmail.text.toString().trim()
        val pass = etPass.text.toString().trim()
        val pass2 = etPass2.text.toString().trim()

        // Validaciones
        when {
            email.isEmpty() || pass.isEmpty() || pass2.isEmpty() -> {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return
            }
            pass != pass2 -> {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return
            }
            pass.length < 6 -> {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Crear usuario en Firebase
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        // Guardar información opcional en Firestore
                        val userData = hashMapOf(
                            "email" to email
                        )
                        db.collection("users").document(user.uid)
                            .set(userData)
                    }
                    Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
