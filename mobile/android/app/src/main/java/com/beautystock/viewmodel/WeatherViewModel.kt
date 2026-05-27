package com.beautystock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.model.WeatherResponse
import com.beautystock.repository.BeautyStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(private val repository: BeautyStockRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _cityInput = MutableStateFlow("")
    val cityInput: StateFlow<String> = _cityInput.asStateFlow()

    fun setCityInput(city: String) {
        _cityInput.value = city
    }

    fun getYouthWeatherAdvice() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getYouthWeatherAdvice()
                .onSuccess { _uiState.value = WeatherUiState.Success(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Failed to load weather advice") }
        }
    }

    fun getAdultWeatherAdvice() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getAdultWeatherAdvice()
                .onSuccess { _uiState.value = WeatherUiState.Success(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Failed to load weather advice") }
        }
    }

    fun searchByCity(city: String, role: String = "ROLE_USER") {
        if (city.isBlank()) {
            _uiState.value = WeatherUiState.Error("Please enter a city name")
            return
        }
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getWeatherByCity(city.trim(), role)
                .onSuccess { _uiState.value = WeatherUiState.Success(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Failed to fetch weather") }
        }
    }

    fun searchByLocation(cityName: String, role: String = "ROLE_USER") {
        if (cityName.isBlank()) {
            _uiState.value = WeatherUiState.Error("Could not determine city from location")
            return
        }
        _cityInput.value = cityName
        searchByCity(cityName, role)
    }

    fun reset() {
        _uiState.value = WeatherUiState.Idle
    }
}
