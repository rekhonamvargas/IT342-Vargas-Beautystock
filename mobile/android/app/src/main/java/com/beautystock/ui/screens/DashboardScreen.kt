package com.beautystock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.beautystock.model.DashboardDTO
import com.beautystock.network.AuthTokenManager
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing
import com.beautystock.utils.PriceFormatter
import com.beautystock.viewmodel.DashboardUiState
import com.beautystock.viewmodel.DashboardViewModel
import com.beautystock.viewmodel.DashboardViewModelFactory
import kotlinx.coroutines.flow.first

@Composable
fun DashboardScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(repository))
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by dashboardViewModel.uiState.collectAsState()
    var greeting by remember { mutableStateOf("Dashboard") }

    // Resolve a friendly greeting from the stored email
    LaunchedEffect(Unit) {
        val email = AuthTokenManager.getUserEmail(context).first()
        if (!email.isNullOrBlank()) {
            greeting = "Hi, ${email.substringBefore("@")} ✨"
        }
    }

    // Refresh stats every time this screen comes back into view
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) dashboardViewModel.getDashboard()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BeautySpacing.xl)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BeautySpacing.xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    color = BeautyColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your beauty inventory at a glance",
                    style = MaterialTheme.typography.bodySmall,
                    color = BeautyColors.TextSecondary
                )
            }
            IconButton(onClick = { dashboardViewModel.refresh() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = BeautyColors.Primary)
            }
        }

        when (uiState) {
            is DashboardUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BeautyColors.Primary) }

            is DashboardUiState.Success -> {
                DashboardContent(
                    dashboard = (uiState as DashboardUiState.Success).dashboard,
                    navController = navController
                )
            }

            is DashboardUiState.Error -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = (uiState as DashboardUiState.Error).message,
                    color = BeautyColors.Error,
                    modifier = Modifier.padding(BeautySpacing.xl)
                )
                Button(
                    onClick = { dashboardViewModel.refresh() },
                    colors = ButtonDefaults.buttonColors(containerColor = BeautyColors.Primary)
                ) { Text("Retry") }
            }

            is DashboardUiState.Idle -> {}
        }
    }
}

@Composable
private fun DashboardContent(dashboard: DashboardDTO, navController: NavHostController?) {
    DashboardStatCard(
        title = "Total Products",
        value = dashboard.totalProducts.toString(),
        subtitle = "in your collection",
        color = BeautyColors.Primary,
        modifier = Modifier.padding(bottom = BeautySpacing.lg)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = BeautySpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(BeautySpacing.md)
    ) {
        DashboardStatCard(
            title = "Expiring Soon",
            value = dashboard.expiringProducts.toString(),
            subtitle = "within 15 days",
            color = BeautyColors.Warning,
            modifier = Modifier.weight(1f)
        )
        DashboardStatCard(
            title = "Expired",
            value = dashboard.expiredProducts.toString(),
            subtitle = "need disposal",
            color = BeautyColors.Error,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = BeautySpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(BeautySpacing.md)
    ) {
        DashboardStatCard(
            title = "Favorites",
            value = dashboard.favoritesCount.toString(),
            subtitle = "saved products",
            color = BeautyColors.Secondary,
            modifier = Modifier.weight(1f)
        )
        DashboardStatCard(
            title = "Running Low",
            value = dashboard.runningOutProducts.toString(),
            subtitle = "opened > 30 days",
            color = BeautyColors.Info,
            modifier = Modifier.weight(1f)
        )
    }

    if (dashboard.totalSpent != null && dashboard.totalSpent > 0) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BeautySpacing.lg),
            colors = CardDefaults.cardColors(
                containerColor = BeautyColors.SecondaryLight.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(BeautySpacing.xl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Invested",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BeautyColors.TextSecondary
                )
                Text(
                    text = PriceFormatter.formatPrice(dashboard.totalSpent),
                    style = MaterialTheme.typography.titleLarge,
                    color = BeautyColors.Secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium,
        color = BeautyColors.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = BeautySpacing.md)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BeautySpacing.md)
    ) {
        OutlinedButton(
            onClick = { navController?.navigate("add_product") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BeautyColors.Primary),
            border = androidx.compose.foundation.BorderStroke(1.dp, BeautyColors.Primary)
        ) { Text("+ Add Product") }

        OutlinedButton(
            onClick = { navController?.navigate("weather") },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BeautyColors.Secondary),
            border = androidx.compose.foundation.BorderStroke(1.dp, BeautyColors.Secondary)
        ) { Text("Weather Tips") }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BeautySpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = BeautyColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = BeautyColors.TextSecondary
            )
        }
    }
}
