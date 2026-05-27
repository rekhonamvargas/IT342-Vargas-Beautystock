package com.beautystock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.model.DashboardDTO
import com.beautystock.repository.BeautyStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DashboardUiState {
    object Idle : DashboardUiState()
    object Loading : DashboardUiState()
    data class Success(val dashboard: DashboardDTO) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(private val repository: BeautyStockRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    fun getDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            val result = repository.getDashboard()
            
            result.onSuccess { dashboard ->
                _uiState.value = DashboardUiState.Success(dashboard)
            }.onFailure { exception ->
                _uiState.value = DashboardUiState.Error(exception.message ?: "Failed to load dashboard")
            }
        }
    }
    
    fun refresh() {
        getDashboard()
    }
}
