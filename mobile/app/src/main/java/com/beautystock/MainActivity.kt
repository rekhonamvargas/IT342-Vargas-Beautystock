package com.beautystock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.local.TokenManager
import com.beautystock.ui.xml.DashboardActivity
import com.beautystock.ui.xml.LandingActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)

        val token = runBlocking { TokenManager(this@MainActivity).token.first() }
        val intent = if (token.isNullOrBlank()) {
            Intent(this, LandingActivity::class.java)
        } else {
            Intent(this, DashboardActivity::class.java)
        }

        startActivity(intent)
        finish()
    }
}
