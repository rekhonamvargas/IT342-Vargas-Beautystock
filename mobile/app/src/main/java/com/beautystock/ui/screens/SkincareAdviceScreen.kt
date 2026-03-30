package com.beautystock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.WeatherResponse
import com.beautystock.ui.components.AdviceBanner
import com.beautystock.ui.components.EssentialCard
import com.beautystock.ui.components.SectionHeader
import com.beautystock.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkincareAdviceScreen(authViewModel: AuthViewModel, onBack: () -> Unit) {
    val user by authViewModel.user.collectAsState()
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme
    val role = if (user?.role == "ROLE_YOUTH") "Youth" else "Adult"

    LaunchedEffect(Unit) {
        try {
            val res = if (user?.role == "ROLE_YOUTH") RetrofitClient.api.getYouthWeather()
                      else RetrofitClient.api.getAdultWeather()
            if (res.isSuccessful) weather = res.body()
        } catch (_: Exception) {}
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Skincare Advice",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface)
            )
        },
        containerColor = colorScheme.background
    ) { padding ->
        if (loading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Role chip
                item {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colorScheme.primaryContainer
                    ) {
                        Text(
                            "\uD83C\uDF3F $role Skincare",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Weather advice banner
                weather?.let { w ->
                    item {
                        AdviceBanner(
                            role = role,
                            tempC = w.temperature?.toInt() ?: 0,
                            humidity = w.humidity ?: 0,
                            condition = w.city ?: "",
                            adviceText = w.advice ?: "Set your location in Profile to get personalized advice."
                        )
                    }
                }

                // Weather details
                weather?.let { w ->
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                WeatherStat("\uD83C\uDF21", "${w.temperature?.toInt() ?: "\u2014"}\u00B0C", "Temp")
                                WeatherStat("\uD83D\uDCA7", "${w.humidity ?: "\u2014"}%", "Humidity")
                                WeatherStat("\uD83D\uDCCD", w.city ?: "\u2014", "City")
                            }
                        }
                    }
                }

                // Tips section
                item {
                    SectionHeader(title = "Daily essentials")
                }

                item {
                    EssentialCard(
                        emoji = "\uD83E\uDDF4",
                        title = "Moisturizer",
                        body = "Apply a hydrating moisturizer suited to your skin type every morning and night."
                    )
                }
                item {
                    EssentialCard(
                        emoji = "\u2600\uFE0F",
                        title = "Sunscreen",
                        body = "Use SPF 30+ daily, even on cloudy days. Reapply every 2 hours when outdoors."
                    )
                }
                item {
                    EssentialCard(
                        emoji = "\uD83D\uDCA6",
                        title = "Hydration",
                        body = "Drink at least 8 glasses of water daily for glowing, healthy skin."
                    )
                }
                item {
                    EssentialCard(
                        emoji = "\uD83C\uDF19",
                        title = "Night Routine",
                        body = "Always remove makeup before bed and apply a nourishing night serum."
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherStat(emoji: String, value: String, label: String) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.titleMedium)
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant
        )
    }
}
