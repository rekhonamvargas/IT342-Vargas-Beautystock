package com.beautystock.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.beautystock.network.RetrofitClient
import com.beautystock.repository.BeautyStockRepository
import com.beautystock.ui.components.BeautyButton
import com.beautystock.ui.components.BeautyTextField
import com.beautystock.ui.components.ErrorDialog
import com.beautystock.ui.components.GoogleAuthButton
import com.beautystock.utils.BeautySpacing
import com.beautystock.utils.ValidationUtils
import com.beautystock.viewmodel.AuthUiState
import com.beautystock.viewmodel.AuthViewModel
import com.beautystock.viewmodel.AuthViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = BeautyStockRepository(RetrofitClient.getApiService(), context)
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val authUiState by authViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> errorMessage = state.message
            else -> Unit
        }
    }

    AuthScaffold(
        title = "Welcome Back",
        subtitle = "Your inventory is ready for you.",
        onFooterClick = { navController.navigate("register") },
        footerText = "Don't have an account? ",
        footerAction = "Create one"
    ) {
        BeautyTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            label = "Email Address",
            keyboardType = KeyboardType.Email,
            isError = emailError,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = false
            },
            label = "Password",
            isPassword = true,
            isError = passwordError,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        BeautyButton(
            text = "Sign In",
            onClick = {
                emailError = !ValidationUtils.isValidEmail(email)
                passwordError = password.isEmpty()
                if (!emailError && !passwordError) {
                    authViewModel.login(email, password)
                }
            },
            isLoading = authUiState is AuthUiState.Loading,
            enabled = authUiState !is AuthUiState.Loading,
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )

        SocialDivider()

        GoogleAuthButton(
            text = "Continue with Google",
            isLoading = authUiState is AuthUiState.Loading,
            onGoogleLogin = { idToken ->
                authViewModel.googleLogin(idToken)
            },
            modifier = Modifier.padding(bottom = BeautySpacing.lg)
        )
    }

    if (errorMessage != null) {
        ErrorDialog(message = errorMessage!!, onDismiss = { errorMessage = null })
    }
}
