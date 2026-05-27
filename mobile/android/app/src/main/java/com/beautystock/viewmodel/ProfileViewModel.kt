package com.beautystock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.model.UserProfileDTO
import com.beautystock.repository.BeautyStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfileDTO) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(private val repository: BeautyStockRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _profile = MutableStateFlow<UserProfileDTO?>(null)
    val profile: StateFlow<UserProfileDTO?> = _profile.asStateFlow()
    
    fun getProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            val result = repository.getProfile()
            
            result.onSuccess { profile ->
                _profile.value = profile
                _uiState.value = ProfileUiState.Success(profile)
            }.onFailure { exception ->
                _uiState.value = ProfileUiState.Error(exception.message ?: "Failed to load profile")
            }
        }
    }
    
    fun updateProfile(firstName: String?, lastName: String?, city: String?) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            val profileUpdate = com.beautystock.model.ProfileUpdateDTO(
                firstName = firstName,
                lastName = lastName,
                city = city
            )
            
            val result = repository.updateProfile(profileUpdate)
            
            result.onSuccess { profile ->
                _profile.value = profile
                _uiState.value = ProfileUiState.Success(profile)
            }.onFailure { exception ->
                _uiState.value = ProfileUiState.Error(exception.message ?: "Failed to update profile")
            }
        }
    }
    
    fun updateLocation(city: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            val result = repository.updateLocation(city)
            
            result.onSuccess {
                getProfile() // Refresh profile
            }.onFailure { exception ->
                _uiState.value = ProfileUiState.Error(exception.message ?: "Failed to update location")
            }
        }
    }
    
    fun uploadProfileImage(imageFile: File) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            val result = repository.uploadProfileImage(imageFile)
            
            result.onSuccess { profile ->
                _profile.value = profile
                _uiState.value = ProfileUiState.Success(profile)
            }.onFailure { exception ->
                _uiState.value = ProfileUiState.Error(exception.message ?: "Failed to upload profile image")
            }
        }
    }
}
