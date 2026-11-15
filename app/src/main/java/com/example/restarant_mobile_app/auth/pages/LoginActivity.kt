package com.example.restarant_mobile_app.auth.pages

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.restarant_mobile_app.R
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.auth.Entities.User
import com.example.restarant_mobile_app.products.pages.MenuActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        seedAdminUser()

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (!validate(email, password)) {
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            lifecycleScope.launch {
                val loggedIn = withContext(Dispatchers.IO) {
                    RestaurantDatabase.getInstance(applicationContext)
                        .userDao()
                        .login(email, password)
                }
                loginButton.isEnabled = true

                if (loggedIn == null) {
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.error_invalid_credentials,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                val isAdmin = email.equals(ADMIN_EMAIL, ignoreCase = true)
                if (isAdmin) {
                    startActivity(Intent(this@LoginActivity, AdminDashActivity::class.java))
                } else {
                    startActivity(
                        Intent(this@LoginActivity, MenuActivity::class.java).apply {
                            putExtra(EXTRA_USER_ID, loggedIn.id)
                        }
                    )
                }
                finish()
            }
        }

        goToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    /**
     * Makes sure the admin account exists so the admin side
     * is reachable. Credentials: admin@rest.com / admin
     */
    private fun seedAdminUser() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = RestaurantDatabase.getInstance(applicationContext).userDao()
            if (dao.getUserByEmail(ADMIN_EMAIL) == null) {
                dao.insertUser(
                    User(
                        name = "Admin",
                        address = "",
                        password = ADMIN_PASSWORD,
                        email = ADMIN_EMAIL
                    )
                )
            }
        }
    }

    private fun validate(email: String, password: String): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = getString(R.string.error_invalid_email)
            return false
        }
        return true
    }

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val ADMIN_EMAIL = "admin@rest.com"
        const val ADMIN_PASSWORD = "admin"
    }
}
