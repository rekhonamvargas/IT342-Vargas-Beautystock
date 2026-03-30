package com.beautystock.ui.xml

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.beautystock.R
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.local.TokenManager
import com.beautystock.data.model.LoginRequest
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        RetrofitClient.init(this)

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.etPassword)
        val googleLoginButton = findViewById<Button>(R.id.btnGoogleLogin)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val registerLink = findViewById<TextView>(R.id.tvGoRegister)
        val registerContainer = findViewById<LinearLayout>(R.id.containerGoRegister)

        intent.getStringExtra("email")?.let { emailInput.setText(it) }

        registerLink.setOnClickListener {
            goToRegister()
        }

        registerContainer.setOnClickListener {
            goToRegister()
        }

        googleLoginButton.setOnClickListener {
            Toast.makeText(this, "Google sign-in is not configured yet", Toast.LENGTH_SHORT).show()
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.login(LoginRequest(email, password))
                    if (response.isSuccessful && response.body() != null) {
                        TokenManager(this@LoginActivity).saveToken(response.body()!!.token)
                        Toast.makeText(this@LoginActivity, "✓ Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finishAffinity()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, e.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                } finally {
                    loginButton.isEnabled = true
                }
            }
        }
    }

    private fun goToRegister() {
        try {
            val registerIntent = Intent().setClassName(
                packageName,
                "com.beautystock.ui.xml.RegisterActivity"
            )

            if (registerIntent.resolveActivity(packageManager) != null) {
                startActivity(registerIntent)
            } else {
                Toast.makeText(this, "Register page is not available", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open Register: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
