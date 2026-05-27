package com.beautystock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.ui.components.BeautyButton
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing
import com.beautystock.viewmodel.AuthViewModel
import com.beautystock.viewmodel.AuthViewModelFactory
import com.beautystock.viewmodel.ProfileViewModel
import com.beautystock.viewmodel.ProfileUiState
import com.beautystock.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController? = null,
    onLogout: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository))

    val profileUiState by profileViewModel.uiState.collectAsState()
    val profile by profileViewModel.profile.collectAsState()

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showEditCity by remember { mutableStateOf(false) }
    var cityInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { profileViewModel.getProfile() }

    LaunchedEffect(profile) {
        cityInput = profile?.city ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = BeautyColors.Error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(BeautySpacing.xl)
        ) {
            when (profileUiState) {
                is ProfileUiState.Loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BeautyColors.Primary)
                }

                is ProfileUiState.Success -> {
                    profile?.let { profileData ->
                        val roleLabel = when (profileData.role) {
                            "ROLE_YOUTH" -> "Youth (13-24)"
                            "ROLE_ADULT" -> "Adult (25+)"
                            else -> profileData.role
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BeautyColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(BeautySpacing.xl),
                                verticalArrangement = Arrangement.spacedBy(BeautySpacing.sm)
                            ) {
                                Text(
                                    text = profileData.fullName ?: "${profileData.firstName} ${profileData.lastName}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = BeautyColors.TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profileData.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BeautyColors.TextSecondary
                                )
                                Surface(
                                    color = BeautyColors.PrimaryLight.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(
                                        text = roleLabel,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = BeautyColors.Primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(BeautySpacing.lg))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = BeautyColors.Surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(BeautySpacing.xl),
                                verticalArrangement = Arrangement.spacedBy(BeautySpacing.md)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = BeautyColors.Primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(BeautySpacing.sm))
                                        Column {
                                            Text(
                                                text = "City",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = BeautyColors.TextSecondary
                                            )
                                            Text(
                                                text = profileData.city ?: "Not set",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (profileData.city != null) BeautyColors.TextPrimary else BeautyColors.TextHint
                                            )
                                        }
                                    }
                                    IconButton(onClick = { showEditCity = true }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit city", tint = BeautyColors.Primary)
                                    }
                                }

                                Text(
                                    text = "Keep your city updated so the Weather tab can show more relevant skincare advice.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BeautyColors.TextSecondary
                                )

                                FilledTonalButton(
                                    onClick = { navController?.navigate("weather") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = navController != null
                                ) {
                                    Icon(Icons.Filled.WbSunny, contentDescription = null)
                                    Spacer(Modifier.width(BeautySpacing.sm))
                                    Text("Open Weather Advice")
                                }
                            }
                        }
                    }
                }

                is ProfileUiState.Error -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BeautyColors.Error.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(BeautySpacing.xl)) {
                        Text(
                            text = (profileUiState as ProfileUiState.Error).message,
                            color = BeautyColors.Error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(BeautySpacing.md))
                        BeautyButton(
                            text = "Retry",
                            onClick = { profileViewModel.getProfile() }
                        )
                    }
                }

                is ProfileUiState.Idle -> Unit
            }
        }
    }

    // Logout dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeautyColors.Error)
                ) { Text("Logout") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // Edit city dialog
    if (showEditCity) {
        AlertDialog(
            onDismissRequest = { showEditCity = false },
            title = { Text("Update City") },
            text = {
                Column {
                    Text(
                        text = "Enter your city for weather-based skincare advice:",
                        style = MaterialTheme.typography.bodySmall,
                        color = BeautyColors.TextSecondary,
                        modifier = Modifier.padding(bottom = BeautySpacing.md)
                    )
                    OutlinedTextField(
                        value = cityInput,
                        onValueChange = { cityInput = it },
                        label = { Text("City name") },
                        placeholder = { Text("e.g. Manila, Cebu") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BeautyColors.Primary,
                            focusedLabelColor = BeautyColors.Primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cityInput.isNotBlank()) {
                            profileViewModel.updateLocation(cityInput.trim())
                            showEditCity = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeautyColors.Primary)
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditCity = false }) { Text("Cancel") }
            }
        )
    }
}
