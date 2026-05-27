package com.beautystock.ui.screens

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beautystock.model.WeatherResponse
import com.beautystock.network.AuthTokenManager
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing
import com.beautystock.viewmodel.WeatherUiState
import com.beautystock.viewmodel.WeatherViewModel
import com.beautystock.viewmodel.WeatherViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SkincareAdviceScreen() {
    val context = LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(repository))
    val scope = rememberCoroutineScope()

    val uiState by weatherViewModel.uiState.collectAsState()
    val cityInput by weatherViewModel.cityInput.collectAsState()

    var selectedRole by remember { mutableStateOf("ROLE_USER") }
    var isGpsLoading by remember { mutableStateOf(false) }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

    // Determine user role from saved token to pre-select the right role
    LaunchedEffect(Unit) {
        val role = AuthTokenManager.getUserRole(context).first()
        if (!role.isNullOrBlank()) {
            selectedRole = role
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(BeautySpacing.xl)
    ) {
        Text(
            text = "Weather Advice",
            style = MaterialTheme.typography.headlineMedium,
            color = BeautyColors.TextPrimary
        )
        Text(
            text = "Get skincare tips based on real-time weather.",
            style = MaterialTheme.typography.bodyMedium,
            color = BeautyColors.TextSecondary,
            modifier = Modifier.padding(top = BeautySpacing.xs, bottom = BeautySpacing.xl)
        )

        // City search row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BeautySpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = { weatherViewModel.setCityInput(it) },
                label = { Text("Search city...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BeautyColors.Primary,
                    unfocusedBorderColor = BeautyColors.Divider,
                    focusedLabelColor = BeautyColors.Primary
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        weatherViewModel.searchByCity(cityInput, selectedRole)
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = BeautyColors.Primary)
                    }
                }
            )

            // GPS button
            FilledTonalIconButton(
                onClick = {
                    if (locationPermission.status.isGranted) {
                        isGpsLoading = true
                        scope.launch {
                            val city = getCurrentCity(context)
                            isGpsLoading = false
                            if (city != null) {
                                weatherViewModel.searchByLocation(city, selectedRole)
                            } else {
                                weatherViewModel.searchByLocation("", selectedRole)
                            }
                        }
                    } else {
                        locationPermission.launchPermissionRequest()
                    }
                },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = BeautyColors.PrimaryLight.copy(alpha = 0.3f)
                )
            ) {
                if (isGpsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BeautyColors.Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Use my location",
                        tint = BeautyColors.Primary
                    )
                }
            }
        }

        Spacer(Modifier.height(BeautySpacing.lg))

        // Role selector
        Text(
            text = "Skin Profile",
            style = MaterialTheme.typography.labelLarge,
            color = BeautyColors.TextSecondary,
            modifier = Modifier.padding(bottom = BeautySpacing.sm)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BeautySpacing.sm)
        ) {
            listOf("ROLE_YOUTH" to "Youth (13-24)", "ROLE_ADULT" to "Adult (25+)").forEach { (roleValue, label) ->
                FilterChip(
                    selected = selectedRole == roleValue,
                    onClick = { selectedRole = roleValue },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BeautyColors.PrimaryLight.copy(alpha = 0.4f),
                        selectedLabelColor = BeautyColors.Primary
                    )
                )
            }
        }

        Spacer(Modifier.height(BeautySpacing.lg))

        // Quick fetch from stored profile city
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BeautySpacing.sm)
        ) {
            OutlinedButton(
                onClick = {
                    if (selectedRole == "ROLE_YOUTH") weatherViewModel.getYouthWeatherAdvice()
                    else weatherViewModel.getAdultWeatherAdvice()
                },
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, BeautyColors.Primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BeautyColors.Primary)
            ) {
                Text("Use My Profile City")
            }
        }

        Spacer(Modifier.height(BeautySpacing.xl))

        // Result
        when (uiState) {
            is WeatherUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BeautyColors.Primary) }

            is WeatherUiState.Success -> {
                WeatherCard(weather = (uiState as WeatherUiState.Success).weather)
            }

            is WeatherUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BeautyColors.Error.copy(alpha = 0.1f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = (uiState as WeatherUiState.Error).message,
                        color = BeautyColors.Error,
                        modifier = Modifier.padding(BeautySpacing.xl)
                    )
                }
            }

            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BeautyColors.PrimaryLight.copy(alpha = 0.15f)),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = "Search for a city or tap the location icon to get real-time weather-based skincare advice.",
                        color = BeautyColors.TextSecondary,
                        modifier = Modifier.padding(BeautySpacing.xl)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(weather: WeatherResponse) {
    val conditionColor = when {
        weather.condition?.contains("Rain", ignoreCase = true) == true -> BeautyColors.Info
        weather.condition?.contains("Clear", ignoreCase = true) == true ||
                weather.condition?.contains("Sun", ignoreCase = true) == true -> BeautyColors.Warning
        weather.condition?.contains("Cloud", ignoreCase = true) == true -> BeautyColors.TextSecondary
        else -> BeautyColors.Primary
    }
    val advice = buildAdvice(weather)
    val tips = buildRoutineTips(weather)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = BeautyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(BeautySpacing.xl)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = weather.city ?: "Unknown city",
                        style = MaterialTheme.typography.titleLarge,
                        color = BeautyColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (weather.condition != null) {
                        Text(
                            text = weather.condition,
                            style = MaterialTheme.typography.bodyMedium,
                            color = conditionColor
                        )
                    }
                }
                if (weather.temperature != null) {
                    Text(
                        text = "${String.format("%.1f", weather.temperature)}°C",
                        style = MaterialTheme.typography.displaySmall,
                        color = BeautyColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (weather.humidity != null) {
                Spacer(Modifier.height(BeautySpacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = BeautyColors.Info.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Humidity: ${weather.humidity}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = BeautyColors.Info
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = BeautySpacing.lg),
                color = BeautyColors.Divider
            )

            Text(
                text = "Skincare Advice",
                style = MaterialTheme.typography.labelLarge,
                color = BeautyColors.TextSecondary,
                modifier = Modifier.padding(bottom = BeautySpacing.sm)
            )
            Text(
                text = advice,
                style = MaterialTheme.typography.bodyMedium,
                color = BeautyColors.TextPrimary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            if (tips.isNotEmpty()) {
                Spacer(Modifier.height(BeautySpacing.lg))
                Text(
                    text = "Recommended routine",
                    style = MaterialTheme.typography.labelLarge,
                    color = BeautyColors.TextSecondary,
                    modifier = Modifier.padding(bottom = BeautySpacing.sm)
                )
                Column(verticalArrangement = Arrangement.spacedBy(BeautySpacing.xs)) {
                    tips.forEach { tip ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "•",
                                color = BeautyColors.Primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = BeautySpacing.sm)
                            )
                            Text(
                                text = tip,
                                color = BeautyColors.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildAdvice(weather: WeatherResponse): String {
    weather.advice?.trim()?.takeIf { it.isNotBlank() }?.let { return it }

    val condition = weather.condition.orEmpty()
    return when {
        condition.contains("Rain", ignoreCase = true) ->
            "Rainy air can make skin feel damp but not hydrated. Keep your routine gentle and finish with a light moisturizer."

        condition.contains("Clear", ignoreCase = true) || condition.contains("Sun", ignoreCase = true) ->
            "Sunny weather increases UV and heat exposure. Prioritize sunscreen, hydrating layers, and a non-greasy finish."

        condition.contains("Cloud", ignoreCase = true) ->
            "Cloud cover can hide the sun but not the UV. Stick to your sunscreen and maintain a balanced moisturizer."

        weather.humidity != null && weather.humidity >= 75 ->
            "High humidity can clog pores and make products feel heavier. Use lighter layers and keep cleansing consistent."

        weather.temperature != null && weather.temperature >= 30 ->
            "Hot weather increases sweat and oil. Focus on hydration, sunscreen, and a simplified routine that won’t feel heavy."

        weather.temperature != null && weather.temperature <= 22 ->
            "Cooler weather can dry the skin faster. Add a richer moisturizer and avoid over-cleansing."

        else -> "No advice available yet. Try another city or refresh with your current location for a more specific recommendation."
    }
}

private fun buildRoutineTips(weather: WeatherResponse): List<String> {
    val tips = mutableListOf<String>()
    val condition = weather.condition.orEmpty()

    if (weather.temperature != null && weather.temperature >= 30) {
        tips += "Use lightweight products and keep a hydrating mist nearby if you spend time outdoors."
    }
    if (weather.temperature != null && weather.temperature <= 22) {
        tips += "Choose a richer moisturizer to reduce dryness caused by cooler air."
    }
    if (weather.humidity != null && weather.humidity >= 75) {
        tips += "Go for non-comedogenic textures so humidity does not overwhelm the skin."
    }
    if (condition.contains("Rain", ignoreCase = true)) {
        tips += "Keep skin clean and dry after exposure to damp weather to avoid irritation."
    }
    if (condition.contains("Clear", ignoreCase = true) || condition.contains("Sun", ignoreCase = true)) {
        tips += "Reapply sunscreen when you expect prolonged daylight exposure."
    }

    if (tips.isEmpty()) {
        tips += "Stay hydrated and keep your routine consistent for the best results."
    }

    return tips.distinct()
}

private suspend fun getCurrentCity(context: Context): String? = withContext(Dispatchers.IO) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        @Suppress("MissingPermission")
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: return@withContext null

        val geocoder = Geocoder(context, Locale.getDefault())

        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        addresses?.firstOrNull()?.locality
            ?: addresses?.firstOrNull()?.adminArea
    } catch (e: Exception) {
        null
    }
}
