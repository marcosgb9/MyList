package com.example.mylist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // No está logueado
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // Ya logueado
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
