package com.beautystock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.model.ProductDTO
import com.beautystock.repository.BeautyStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Idle : FavoritesUiState()
    object Loading : FavoritesUiState()
    data class Success(val products: List<ProductDTO>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

class FavoriteViewModel(private val repository: BeautyStockRepository) : ViewModel() {

    private val _favoritesUiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Idle)
    val favoritesUiState: StateFlow<FavoritesUiState> = _favoritesUiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _favoritesUiState.value = FavoritesUiState.Loading
            repository.getFavorites()
                .onSuccess { _favoritesUiState.value = FavoritesUiState.Success(it) }
                .onFailure { _favoritesUiState.value = FavoritesUiState.Error(it.message ?: "Failed to load favorites") }
        }
    }

    fun addFavorite(productId: Long) {
        viewModelScope.launch {
            repository.addFavorite(productId)
                .onFailure { _favoritesUiState.value = FavoritesUiState.Error(it.message ?: "Failed to add favorite") }
        }
    }

    fun removeFavorite(productId: Long) {
        viewModelScope.launch {
            repository.removeFavorite(productId).onSuccess {
                val current = (_favoritesUiState.value as? FavoritesUiState.Success)?.products ?: return@onSuccess
                _favoritesUiState.value = FavoritesUiState.Success(current.filter { it.id != productId })
            }.onFailure {
                _favoritesUiState.value = FavoritesUiState.Error(it.message ?: "Failed to remove favorite")
            }
        }
    }
}
