package com.beautystock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.model.CreateProductDTO
import com.beautystock.model.ProductDTO
import com.beautystock.repository.BeautyStockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class ProductsUiState {
    object Idle : ProductsUiState()
    object Loading : ProductsUiState()
    data class Success(val products: List<ProductDTO>) : ProductsUiState()
    data class Error(val message: String) : ProductsUiState()
}

sealed class ProductDetailUiState {
    object Idle : ProductDetailUiState()
    object Loading : ProductDetailUiState()
    data class Success(val product: ProductDTO) : ProductDetailUiState()
    data class Error(val message: String) : ProductDetailUiState()
}

class ProductViewModel(private val repository: BeautyStockRepository) : ViewModel() {
    
    private val _productsUiState = MutableStateFlow<ProductsUiState>(ProductsUiState.Idle)
    val productsUiState: StateFlow<ProductsUiState> = _productsUiState.asStateFlow()
    
    private val _productDetailUiState = MutableStateFlow<ProductDetailUiState>(ProductDetailUiState.Idle)
    val productDetailUiState: StateFlow<ProductDetailUiState> = _productDetailUiState.asStateFlow()
    
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()
    
    fun getProducts() {
        viewModelScope.launch {
            _productsUiState.value = ProductsUiState.Loading
            val result = repository.getProducts()
            
            result.onSuccess { products ->
                _productsUiState.value = ProductsUiState.Success(products)
            }.onFailure { exception ->
                _productsUiState.value = ProductsUiState.Error(exception.message ?: "Failed to load products")
            }
        }
    }
    
    fun getProductById(id: Long) {
        viewModelScope.launch {
            _productDetailUiState.value = ProductDetailUiState.Loading
            val result = repository.getProductById(id)
            
            result.onSuccess { product ->
                _productDetailUiState.value = ProductDetailUiState.Success(product)
                checkIsFavorite(id)
            }.onFailure { exception ->
                _productDetailUiState.value = ProductDetailUiState.Error(exception.message ?: "Failed to load product")
            }
        }
    }
    
    fun createProduct(
        name: String,
        brand: String,
        category: String,
        description: String,
        price: Double?,
        purchaseLocation: String,
        expirationDate: String?,
        openedDate: String? = null
    ) {
        viewModelScope.launch {
            _productDetailUiState.value = ProductDetailUiState.Loading

            val createDTO = CreateProductDTO(
                name = name,
                brand = brand,
                category = category,
                description = description.takeIf { it.isNotEmpty() },
                price = price,
                purchaseLocation = purchaseLocation.takeIf { it.isNotEmpty() },
                expirationDate = expirationDate?.takeIf { it.isNotBlank() },
                openedDate = openedDate?.takeIf { it.isNotBlank() },
                status = "AVAILABLE"
            )
            
            val result = repository.createProduct(createDTO)
            
            result.onSuccess { product ->
                _productDetailUiState.value = ProductDetailUiState.Success(product)
                getProducts() // Refresh list
            }.onFailure { exception ->
                _productDetailUiState.value = ProductDetailUiState.Error(exception.message ?: "Failed to create product")
            }
        }
    }
    
    fun updateProduct(
        id: Long,
        name: String,
        brand: String,
        category: String,
        description: String,
        price: Double?,
        purchaseLocation: String,
        expirationDate: String?,
        openedDate: String? = null
    ) {
        viewModelScope.launch {
            _productDetailUiState.value = ProductDetailUiState.Loading

            val createDTO = CreateProductDTO(
                name = name,
                brand = brand,
                category = category,
                description = description.takeIf { it.isNotEmpty() },
                price = price,
                purchaseLocation = purchaseLocation.takeIf { it.isNotEmpty() },
                expirationDate = expirationDate?.takeIf { it.isNotBlank() },
                openedDate = openedDate?.takeIf { it.isNotBlank() },
                status = "AVAILABLE"
            )
            
            val result = repository.updateProduct(id, createDTO)
            
            result.onSuccess { product ->
                _productDetailUiState.value = ProductDetailUiState.Success(product)
                getProducts() // Refresh list
            }.onFailure { exception ->
                _productDetailUiState.value = ProductDetailUiState.Error(exception.message ?: "Failed to update product")
            }
        }
    }
    
    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            _productDetailUiState.value = ProductDetailUiState.Loading
            
            val result = repository.deleteProduct(id)
            
            result.onSuccess {
                _productDetailUiState.value = ProductDetailUiState.Idle
                getProducts() // Refresh list
            }.onFailure { exception ->
                _productDetailUiState.value = ProductDetailUiState.Error(exception.message ?: "Failed to delete product")
            }
        }
    }
    
    fun uploadProductImage(productId: Long, imageFile: File) {
        viewModelScope.launch {
            _productDetailUiState.value = ProductDetailUiState.Loading
            
            val result = repository.uploadProductImage(productId, imageFile)
            
            result.onSuccess { product ->
                _productDetailUiState.value = ProductDetailUiState.Success(product)
            }.onFailure { exception ->
                _productDetailUiState.value = ProductDetailUiState.Error(exception.message ?: "Failed to upload image")
            }
        }
    }
    
    fun searchProducts(query: String) {
        viewModelScope.launch {
            _productsUiState.value = ProductsUiState.Loading
            
            if (query.isEmpty()) {
                getProducts()
                return@launch
            }
            
            val result = repository.searchProducts(query)
            
            result.onSuccess { products ->
                _productsUiState.value = ProductsUiState.Success(products)
            }.onFailure { exception ->
                _productsUiState.value = ProductsUiState.Error(exception.message ?: "Search failed")
            }
        }
    }
    
    fun getExpiringProducts() {
        viewModelScope.launch {
            _productsUiState.value = ProductsUiState.Loading
            
            val result = repository.getExpiringProducts()
            
            result.onSuccess { products ->
                _productsUiState.value = ProductsUiState.Success(products)
            }.onFailure { exception ->
                _productsUiState.value = ProductsUiState.Error(exception.message ?: "Failed to load expiring products")
            }
        }
    }
    
    fun getProductsByCategory(category: String) {
        viewModelScope.launch {
            _productsUiState.value = ProductsUiState.Loading
            
            val result = repository.getProductsByCategory(category)
            
            result.onSuccess { products ->
                _productsUiState.value = ProductsUiState.Success(products)
            }.onFailure { exception ->
                _productsUiState.value = ProductsUiState.Error(exception.message ?: "Failed to load products by category")
            }
        }
    }
    
    fun checkIsFavorite(productId: Long) {
        viewModelScope.launch {
            repository.isFavorite(productId)
                .onSuccess { _isFavorite.value = it }
                .onFailure { _isFavorite.value = false }
        }
    }

    fun addFavoriteToDetail(productId: Long) {
        viewModelScope.launch {
            repository.addFavorite(productId).onSuccess {
                _isFavorite.value = true
            }
        }
    }

    fun removeFavoriteFromDetail(productId: Long) {
        viewModelScope.launch {
            repository.removeFavorite(productId).onSuccess {
                _isFavorite.value = false
            }
        }
    }
}
