package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminAuthRepository {
    companion object {
        const val ADMIN_PASSKEY = "6992"
    }

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun authenticate(passkey: String): Boolean {
        return if (passkey == ADMIN_PASSKEY) {
            _isAuthenticated.value = true
            true
        } else {
            false
        }
    }

    fun logout() {
        _isAuthenticated.value = false
    }
}
