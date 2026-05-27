package com.beautystock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.ui.components.BeautyButton
import com.beautystock.ui.components.BeautyTextField
import com.beautystock.ui.components.ErrorDialog
import com.beautystock.ui.components.GoogleAuthButton
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing
import com.beautystock.viewmodel.AuthUiState
import com.beautystock.viewmodel.AuthViewModel
import com.beautystock.viewmodel.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val authUiState by authViewModel.uiState.collectAsState()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedAgeRange by remember { mutableStateOf("YOUTH") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ageGroups = listOf(
        AgeGroup("YOUTH", "Youth", "Age 13-24"),
        AgeGroup("ADULT", "Adult", "Age 25-44")
    )

    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> errorMessage = state.message
            else -> Unit
        }
    }

    AuthScaffold(
        title = "Create Account",
        subtitle = "Join and start tracking your beauty collection.",
        onFooterClick = { navController.popBackStack() },
        footerText = "Already have an account? ",
        footerAction = "Sign in"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BeautySpacing.md),
            horizontalArrangement = Arrangement.spacedBy(BeautySpacing.sm)
        ) {
            BeautyTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = "First Name",
                modifier = Modifier.weight(1f)
            )
            BeautyTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Last Name",
                modifier = Modifier.weight(1f)
            )
        }

        BeautyTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isPassword = true,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            isPassword = true,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        Text(
            text = "Age Group",
            color = BeautyColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = BeautySpacing.sm)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = BeautySpacing.xxxl)
        ) {
            ageGroups.forEach { ageGroup ->
                AgeGroupCard(
                    ageGroup = ageGroup,
                    isSelected = selectedAgeRange == ageGroup.value,
                    onClick = { selectedAgeRange = ageGroup.value },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = BeautySpacing.xs)
                )
            }
        }

        BeautyButton(
            text = "Create Account",
            onClick = {
                authViewModel.register(firstName, lastName, email, password, confirmPassword, selectedAgeRange)
            },
            isLoading = authUiState is AuthUiState.Loading,
            enabled = authUiState !is AuthUiState.Loading,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        SocialDivider(text = "or sign up with")

        GoogleAuthButton(
            text = "Sign up with Google",
            isLoading = authUiState is AuthUiState.Loading,
            onGoogleLogin = { idToken ->
                authViewModel.googleLogin(idToken, selectedAgeRange)
            },
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
    }

    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}
