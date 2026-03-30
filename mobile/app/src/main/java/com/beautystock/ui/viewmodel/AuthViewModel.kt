package com.beautystock.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beautystock.data.api.RetrofitClient
import com.beautystock.data.local.TokenManager
import com.beautystock.data.model.UserDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenManager = TokenManager(app)

    private val _user = MutableStateFlow<UserDTO?>(null)
    val user: StateFlow<UserDTO?> = _user

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _shouldOpenLoginAfterLogout = MutableStateFlow(false)
    val shouldOpenLoginAfterLogout: StateFlow<Boolean> = _shouldOpenLoginAfterLogout

    init {
        RetrofitClient.init(app)
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val token = tokenManager.token.first()
                if (!token.isNullOrEmpty()) {
                    val res = RetrofitClient.api.getMe()
                    if (res.isSuccessful) {
                        _user.value = res.body()
                    } else {
                        tokenManager.clearToken()
                    }
                }
            } catch (_: Exception) {
                tokenManager.clearToken()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setUser(user: UserDTO, token: String) {
        viewModelScope.launch {
            tokenManager.saveToken(token)
            _user.value = user
            _shouldOpenLoginAfterLogout.value = false
        }
    }

    fun updateUser(user: UserDTO) {
        _user.value = user
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _user.value = null
            _shouldOpenLoginAfterLogout.value = true
        }
    }

    fun consumeLogoutRedirect() {
        _shouldOpenLoginAfterLogout.value = false
    }
}
