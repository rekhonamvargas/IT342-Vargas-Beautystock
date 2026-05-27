package com.beautystock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.model.AuthResponseDTO
import com.beautystock.model.LoginDTO
import com.beautystock.model.RegisterDTO
import com.beautystock.model.UserProfileDTO
import com.beautystock.repository.BeautyStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val auth: AuthResponseDTO) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val repository: BeautyStockRepository) : ViewModel() {

    private companion object {
        const val AUTH_REQUEST_TIMEOUT_MS = 15_000L
    }
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserProfileDTO?>(null)
    val currentUser: StateFlow<UserProfileDTO?> = _currentUser.asStateFlow()
    
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        ageRange: String
    ) {
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
                    try {
                        val result = withTimeout(AUTH_REQUEST_TIMEOUT_MS) {
                            repository.register(
                                RegisterDTO(firstName, lastName, email, password, confirmPassword, ageRange)
                            )
                        }

                        result.onSuccess { auth ->
                            _currentUser.value = auth.user
                            _uiState.value = AuthUiState.Success(auth)
                        }.onFailure { exception ->
                            _uiState.value = AuthUiState.Error(exception.message ?: "Registration failed")
                        }
                    } catch (timeout: TimeoutCancellationException) {
                        _uiState.value = AuthUiState.Error("Registration timed out. Check your network connection and try again.")
                    } catch (exception: Exception) {
                        _uiState.value = AuthUiState.Error(exception.message ?: "Registration failed")
            }
        }
    }
    
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
                    try {
                        val result = withTimeout(AUTH_REQUEST_TIMEOUT_MS) {
                            repository.login(LoginDTO(email, password))
                        }

                        result.onSuccess { auth ->
                            _currentUser.value = auth.user
                            _uiState.value = AuthUiState.Success(auth)
                        }.onFailure { exception ->
                            _uiState.value = AuthUiState.Error(exception.message ?: "Login failed")
                        }
                    } catch (timeout: TimeoutCancellationException) {
                        _uiState.value = AuthUiState.Error("Login timed out. Check your network connection and try again.")
                    } catch (exception: Exception) {
                        _uiState.value = AuthUiState.Error(exception.message ?: "Login failed")
            }
        }
    }

    fun googleLogin(idToken: String, ageRange: String = "YOUTH") {
        if (idToken.isBlank()) {
            _uiState.value = AuthUiState.Error("Google sign-in failed")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = withTimeout(AUTH_REQUEST_TIMEOUT_MS) {
                    repository.googleAuth(idToken, ageRange)
                }

                result.onSuccess { auth ->
                    _currentUser.value = auth.user
                    _uiState.value = AuthUiState.Success(auth)
                }.onFailure { exception ->
                    _uiState.value = AuthUiState.Error(exception.message ?: "Google sign-in failed")
                }
            } catch (timeout: TimeoutCancellationException) {
                _uiState.value = AuthUiState.Error("Google sign-in timed out. Check your network connection and try again.")
            } catch (exception: Exception) {
                _uiState.value = AuthUiState.Error(exception.message ?: "Google sign-in failed")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.logout()
            
            result.onSuccess {
                _currentUser.value = null
                _uiState.value = AuthUiState.Idle
            }.onFailure { exception ->
                _uiState.value = AuthUiState.Error(exception.message ?: "Logout failed")
            }
        }
    }
    
    fun getMe() {
        viewModelScope.launch {
            val result = repository.getMe()
            
            result.onSuccess { user ->
                _currentUser.value = user
            }.onFailure { exception ->
                // Silent fail, user might not be logged in
            }
        }
    }
    
    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
    }
}
