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
import com.beautystock.data.model.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegisterActivity : ComponentActivity() {

    private var selectedAgeGroup: String = "YOUTH"
    private lateinit var btnYouth: Button
    private lateinit var btnAdult: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_register)
            RetrofitClient.init(this)

            val firstNameInput = findViewById<EditText>(R.id.etFirstName)
            val lastNameInput = findViewById<EditText>(R.id.etLastName)
            val emailInput = findViewById<EditText>(R.id.etEmail)
            val passwordInput = findViewById<EditText>(R.id.etPassword)
            val confirmPasswordInput = findViewById<EditText>(R.id.etConfirmPassword)
            val googleButton = findViewById<Button>(R.id.btnGoogle)
            val registerButton = findViewById<Button>(R.id.btnRegister)
            val loginLink = findViewById<TextView>(R.id.tvGoLogin)
            val loginContainer = findViewById<LinearLayout>(R.id.containerGoLogin)
            btnYouth = findViewById<Button>(R.id.btnYouth)
            btnAdult = findViewById<Button>(R.id.btnAdult)

            // Set initial selection to Youth
            updateAgeGroupButton("YOUTH")

            // Age group button listeners
            btnYouth.setOnClickListener {
                selectedAgeGroup = "YOUTH"
                updateAgeGroupButton("YOUTH")
            }

            btnAdult.setOnClickListener {
                selectedAgeGroup = "ADULT"
                updateAgeGroupButton("ADULT")
            }

            loginLink.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            loginContainer.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            googleButton.setOnClickListener {
                Toast.makeText(this, "Google sign-up is not configured yet", Toast.LENGTH_SHORT).show()
            }

            registerButton.setOnClickListener {
                val firstName = firstNameInput.text.toString().trim()
                val lastName = lastNameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val password = passwordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                val ageRange = selectedAgeGroup
                val finalFullName = "$firstName $lastName".trim()

                if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$").matches(password)) {
                    Toast.makeText(this, "Password needs 8+ chars, 1 uppercase, 1 digit", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                registerButton.isEnabled = false
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.api.register(
                            RegisterRequest(finalFullName, email, password, confirmPassword, ageRange)
                        )
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@RegisterActivity, "✓ Account created successfully!", Toast.LENGTH_SHORT).show()
                            val loginIntent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            loginIntent.putExtra("email", email)
                            startActivity(loginIntent)
                            finish()
                        } else {
                            val rawError = response.errorBody()?.string()
                            val parsedError = parseApiError(rawError)
                            val message = parsedError ?: "Registration failed (${response.code()})"
                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, e.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                    } finally {
                        registerButton.isEnabled = true
                    }
                }
            }
        } catch (t: Throwable) {
            Toast.makeText(this, "Register screen error: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun updateAgeGroupButton(selected: String) {
        if (selected == "YOUTH") {
            btnYouth.setBackgroundResource(R.drawable.bg_btn_secondary)
            btnYouth.setTextColor(getColor(R.color.bs_pink))
            btnAdult.setBackgroundResource(R.drawable.bg_card_surface)
            btnAdult.setTextColor(getColor(R.color.bs_muted_dark))
        } else {
            btnYouth.setBackgroundResource(R.drawable.bg_card_surface)
            btnYouth.setTextColor(getColor(R.color.bs_muted_dark))
            btnAdult.setBackgroundResource(R.drawable.bg_btn_secondary)
            btnAdult.setTextColor(getColor(R.color.bs_pink))
        }
    }

    private fun parseApiError(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null

        return try {
            val json = JSONObject(rawError)
            when {
                json.has("message") -> json.optString("message")
                json.has("error") -> {
                    val errorValue = json.opt("error")
                    when (errorValue) {
                        is JSONObject -> errorValue.optString("message").takeIf { it.isNotBlank() }
                        is String -> errorValue
                        else -> null
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            rawError.take(120)
        }
    }
}
