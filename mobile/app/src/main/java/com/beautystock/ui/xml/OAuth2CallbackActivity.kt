package com.beautystock.ui.xml

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.beautystock.R
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.local.TokenManager
import kotlinx.coroutines.launch

class OAuth2CallbackActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth_callback)
        RetrofitClient.init(this)

        val statusText = findViewById<TextView>(R.id.tvOAuthStatus)
        val errorText = findViewById<TextView>(R.id.tvOAuthError)

        val data = intent.data
        val token = data?.getQueryParameter("token")
        val error = data?.getQueryParameter("error")
        val isNewUser = data?.getQueryParameter("isNewUser") == "true"

        if (!error.isNullOrBlank()) {
            errorText.text = Uri.decode(error)
            goToLogin()
            return
        }

        if (token.isNullOrBlank()) {
            errorText.text = "No token received from OAuth2 provider"
            goToLogin()
            return
        }

        statusText.text = "Processing your Google authentication"

        lifecycleScope.launch {
            try {
                TokenManager(this@OAuth2CallbackActivity).saveToken(token)

                if (isNewUser) {
                    startActivity(Intent(this@OAuth2CallbackActivity, RoleSelectionActivity::class.java))
                    finish()
                    return@launch
                }

                val meResponse = RetrofitClient.api.getMe()
                if (meResponse.isSuccessful && meResponse.body() != null) {
                    startActivity(Intent(this@OAuth2CallbackActivity, DashboardActivity::class.java))
                    finishAffinity()
                } else {
                    errorText.text = "Failed to complete OAuth2 login"
                    goToLogin()
                }
            } catch (e: Exception) {
                errorText.text = e.message ?: "Failed to complete OAuth2 login"
                goToLogin()
            }
        }
    }

    private fun goToLogin() {
        lifecycleScope.launch {
            TokenManager(this@OAuth2CallbackActivity).clearToken()
            startActivity(Intent(this@OAuth2CallbackActivity, LoginActivity::class.java))
            finishAffinity()
        }
    }
}