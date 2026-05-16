package com.beautystock.ui.xml

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.beautystock.R
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.local.TokenManager
import com.beautystock.data.model.WeatherResponse
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
        val weatherStatusText = findViewById<TextView>(R.id.tvWeatherStatus)
        val weatherErrorText = findViewById<TextView>(R.id.tvWeatherError)
        val weatherContent = findViewById<LinearLayout>(R.id.weatherContent)
        val weatherCityText = findViewById<TextView>(R.id.tvWeatherCity)
        val weatherTemperatureText = findViewById<TextView>(R.id.tvWeatherTemperature)
        val humidityProgress = findViewById<ProgressBar>(R.id.pbHumidity)
        val humidityText = findViewById<TextView>(R.id.tvHumidity)
        val weatherAdviceText = findViewById<TextView>(R.id.tvWeatherAdvice)

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
                    loadWeatherForRole(
                        role = user.role,
                        statusText = weatherStatusText,
                        errorText = weatherErrorText,
                        content = weatherContent,
                        cityText = weatherCityText,
                        temperatureText = weatherTemperatureText,
                        humidityProgress = humidityProgress,
                        humidityText = humidityText,
                        adviceText = weatherAdviceText
                    )
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

    private fun loadWeatherForRole(
        role: String,
        statusText: TextView,
        errorText: TextView,
        content: LinearLayout,
        cityText: TextView,
        temperatureText: TextView,
        humidityProgress: ProgressBar,
        humidityText: TextView,
        adviceText: TextView,
    ) {
        lifecycleScope.launch {
            statusText.text = "Loading weather data..."
            statusText.visibility = View.VISIBLE
            errorText.visibility = View.GONE
            content.visibility = View.GONE

            try {
                val response = if (role == "ROLE_YOUTH") {
                    RetrofitClient.api.getYouthWeather()
                } else {
                    RetrofitClient.api.getAdultWeather()
                }

                if (response.isSuccessful && response.body() != null) {
                    renderWeather(response.body()!!, statusText, errorText, content, cityText, temperatureText, humidityProgress, humidityText, adviceText)
                } else {
                    showWeatherError(statusText, errorText, content)
                }
            } catch (_: Exception) {
                showWeatherError(statusText, errorText, content)
            }
        }
    }

    private fun renderWeather(
        weather: WeatherResponse,
        statusText: TextView,
        errorText: TextView,
        content: LinearLayout,
        cityText: TextView,
        temperatureText: TextView,
        humidityProgress: ProgressBar,
        humidityText: TextView,
        adviceText: TextView,
    ) {
        statusText.visibility = View.GONE
        errorText.visibility = View.GONE
        content.visibility = View.VISIBLE

        cityText.text = weather.city ?: "Unknown location"
        temperatureText.text = weather.temperature?.let { "${it.toInt()}°C" } ?: "—"
        val humidity = weather.humidity ?: 0
        humidityProgress.progress = humidity.coerceIn(0, 100)
        humidityText.text = "$humidity% humidity"
        adviceText.text = weather.advice ?: "Set your city in Profile to get personalized weather-based skincare tips."
    }

    private fun showWeatherError(statusText: TextView, errorText: TextView, content: LinearLayout) {
        statusText.visibility = View.GONE
        errorText.text = "Unable to load weather advice. Set your city in Profile and try again."
        errorText.visibility = View.VISIBLE
        content.visibility = View.GONE
    }
}
