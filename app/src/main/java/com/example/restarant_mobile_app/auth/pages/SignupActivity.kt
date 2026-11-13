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

class SignupActivity : ComponentActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        val nameInput = findViewById<EditText>(R.id.editTextName)
        emailInput = findViewById(R.id.editTextEmail)
        passwordInput = findViewById(R.id.editTextPassword)
        confirmPasswordInput = findViewById(R.id.editTextConfirmPassword)
        val signupButton = findViewById<Button>(R.id.buttonSignup)
        val goToLogin = findViewById<TextView>(R.id.textGoToLogin)

        signupButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (!validate(name, email, password, confirmPassword)) {
                return@setOnClickListener
            }

            Toast.makeText(this, R.string.signup_success, Toast.LENGTH_SHORT).show()
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validate(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = getString(R.string.error_invalid_email)
            return false
        }
        if (password.length < 6) {
            passwordInput.error = getString(R.string.error_short_password)
            return false
        }
        if (password != confirmPassword) {
            confirmPasswordInput.error = getString(R.string.error_password_mismatch)
            return false
        }
        return true
    }
}
