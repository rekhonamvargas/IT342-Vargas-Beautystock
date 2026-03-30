package com.beautystock.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.model.DashboardDTO
import com.beautystock.data.model.WeatherResponse
import com.beautystock.ui.components.AdviceBanner
import com.beautystock.ui.components.SectionHeader
import com.beautystock.ui.components.StatCard
import com.beautystock.ui.theme.OrangeWarn
import com.beautystock.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    onNavigateToProducts: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val user by authViewModel.user.collectAsState()
    var stats by remember { mutableStateOf<DashboardDTO?>(null) }
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val fullName = user?.fullName.orEmpty()
    val initials = fullName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
    val greetingName = fullName.substringBefore(" ").ifBlank { "there" }

    LaunchedEffect(Unit) {
        try {
            val dashRes = RetrofitClient.api.getDashboard()
            if (dashRes.isSuccessful) stats = dashRes.body()
        } catch (_: Exception) {}
        try {
            val wRes = if (user?.role == "ROLE_YOUTH") RetrofitClient.api.getYouthWeather()
                       else RetrofitClient.api.getAdultWeather()
            if (wRes.isSuccessful) weather = wRes.body()
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "\u2726 BeautyStock",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                },
                actions = {
                    TextButton(onClick = { authViewModel.logout() }) {
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.labelLarge,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                initials,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product", tint = colorScheme.onPrimary)
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting
            item {
                Text(
                    "Hello, $greetingName \u2728",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    "Here's your beauty inventory at a glance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            // Stats row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("\uD83D\uDCE6", "${stats?.totalProducts ?: 0}", "Products", modifier = Modifier.weight(1f))
                    StatCard("\u2764\uFE0F", "${stats?.favoritesCount ?: 0}", "Favorites", modifier = Modifier.weight(1f))
                    StatCard("\u23F0", "${stats?.expiringCount ?: 0}", "Expiring", modifier = Modifier.weight(1f), countColor = OrangeWarn)
                }
            }

            // Total spent
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total Spent",
                            style = MaterialTheme.typography.titleSmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%.2f", stats?.totalSpent ?: 0.0)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }
            }

            // Weather advice
            weather?.let { w ->
                item {
                    AdviceBanner(
                        role = if (user?.role == "ROLE_YOUTH") "Youth" else "Adult",
                        tempC = w.temperature?.toInt() ?: 0,
                        humidity = w.humidity ?: 0,
                        condition = w.city ?: "",
                        adviceText = w.advice ?: ""
                    )
                }
            }

            // Quick Actions
            item {
                SectionHeader(title = "Quick actions")
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionCard("\u2795", "Add Product", Modifier.weight(1f)) { onNavigateToAddProduct() }
                    ActionCard("\uD83D\uDCE6", "My Products", Modifier.weight(1f)) { onNavigateToProducts() }
                    ActionCard("\u2764\uFE0F", "Favorites", Modifier.weight(1f)) { onNavigateToFavorites() }
                }
            }
        }
    }
}

@Composable
private fun ActionCard(icon: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
        }
    }
}
