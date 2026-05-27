package com.beautystock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.beautystock.network.AuthTokenManager
import com.beautystock.network.BackendUrlProvider
import com.beautystock.network.RetrofitClient
import com.beautystock.ui.screens.MainApp
import com.beautystock.ui.theme.BeautyStockTheme
import com.beautystock.utils.BeautyColors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.initialize(this)
        RetrofitClient.updateBaseUrl(BuildConfig.BASE_URL.takeIf { it.isNotBlank() } ?: BackendUrlProvider.defaultBaseUrl())

        // Persist any OAuth token that arrives via deep link. Capture presence early
        // so the UI can immediately navigate to the authenticated app without
        // racing the async DataStore write.
        val deepLinkToken = intent?.data?.getQueryParameter("token")?.takeIf { it.isNotBlank() }
        deepLinkToken?.let { token ->
            lifecycleScope.launch { AuthTokenManager.saveToken(this@MainActivity, token) }
        }

        setContent {
            BeautyStockTheme {
                val context = LocalContext.current
                // If we already detected a token on the intent, start as authenticated
                // to avoid a race between saving the token and Compose's LaunchedEffect.
                var startDestination by remember { mutableStateOf<String?>(if (deepLinkToken != null) "app" else null) }

                LaunchedEffect(Unit) {
                    AuthTokenManager.initialize(context)
                    startDestination = if (AuthTokenManager.isTokenAvailable(context)) "app" else "auth"
                }

                if (startDestination != null) {
                    MainApp(startDestination = startDestination!!)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BeautyColors.Primary)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.data?.getQueryParameter("token")?.takeIf { it.isNotBlank() }?.let { token ->
            lifecycleScope.launch { AuthTokenManager.saveToken(this@MainActivity, token) }
        }
    }
}
