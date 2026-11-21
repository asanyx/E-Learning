package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.AuthRepository
import com.example.elearning.data.repository.AuthResult
import com.example.elearning.domain.AuthError
import com.example.elearning.domain.model.AuthUser
import com.example.elearning.domain.validation.AuthValidator
import com.example.elearning.domain.validation.ValidationError
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val validationErrors: List<ValidationError> = emptyList(),
    val authError: AuthError? = null,
    val user: AuthUser? = null,
    val isInitializing: Boolean = true
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val state = MutableStateFlow(AuthUiState())
    val uiState = state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUser.collect { user -> state.update { it.copy(user = user, isInitializing = false) } }
        }
    }

    fun login(email: String, password: String) =
        authenticate(AuthValidator.validateLogin(email, password)) { repository.signIn(email, password) }

    fun register(name: String, email: String, password: String, confirmation: String) =
        authenticate(AuthValidator.validateRegistration(name, email, password, confirmation)) {
            repository.register(name, email, password)
        }

    fun updateName(name: String) =
        authenticate(listOfNotNull(AuthValidator.validateName(name))) {
            repository.updateDisplayName(name.trim())
        }

    private fun authenticate(errors: List<ValidationError>, action: suspend () -> AuthResult) {
        if (state.value.isLoading) return
        state.update { it.copy(validationErrors = errors, authError = null, isLoading = errors.isEmpty()) }
        if (errors.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val result = action()
                if (result is AuthResult.Failure) state.update { it.copy(authError = result.error) }
            } finally {
                state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { repository.signOut() }
    }

    fun clearErrors() {
        state.update { it.copy(validationErrors = emptyList(), authError = null) }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AuthViewModel::class.java))
            return modelClass.cast(AuthViewModel(repository))!!
        }
    }
}
