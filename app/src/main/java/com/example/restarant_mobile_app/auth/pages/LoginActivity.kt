package com.example.restarant_mobile_app.auth.pages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.restarant_mobile_app.R
import com.example.restarant_mobile_app.products.pages.MenuActivity

class LoginActivity : ComponentActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        emailInput = findViewById(R.id.editTextEmail)
        passwordInput = findViewById(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.buttonLogin)
        val goToSignup = findViewById<TextView>(R.id.textGoToSignup)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (!validate(email, password)) {
                return@setOnClickListener
            }

            // TODO: verify credentials and user role against DB via UsersDAO
            if (email.equals("admin", ignoreCase = true)) {
                startActivity(Intent(this, AdminDashActivity::class.java))
            } else {
                startActivity(Intent(this, MenuActivity::class.java))
            }
            finish()
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show()
            return false
        }
        if (!email.equals("admin", ignoreCase = true) &&
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            emailInput.error = getString(R.string.error_invalid_email)
            return false
        }
        return true
    }
}
