package com.beautystock.ui.xml

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.beautystock.R
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        RetrofitClient.init(this)

        val welcomeText = findViewById<TextView>(R.id.tvWelcome)
        val roleText = findViewById<TextView>(R.id.tvRole)
        val logoutButton = findViewById<Button>(R.id.btnLogout)

        lifecycleScope.launch {
            val token = TokenManager(this@DashboardActivity).token.first()
            if (token.isNullOrBlank()) {
                goToLogin()
                return@launch
            }

            try {
                val me = RetrofitClient.api.getMe()
                if (me.isSuccessful && me.body() != null) {
                    val user = me.body()!!
                    val firstName = user.fullName.substringBefore(" ").ifBlank { user.fullName }
                    welcomeText.text = "Welcome, $firstName"
                    roleText.text = if (user.role == "ROLE_YOUTH") "Youth account" else "Adult account"
                }
            } catch (_: Exception) {
                // Keep static fallback text when profile fetch fails.
            }
        }

        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                try {
                    RetrofitClient.api.logout()
                } catch (_: Exception) {
                    // Ignore network errors during logout.
                } finally {
                    TokenManager(this@DashboardActivity).clearToken()
                    goToLogin()
                }
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}
