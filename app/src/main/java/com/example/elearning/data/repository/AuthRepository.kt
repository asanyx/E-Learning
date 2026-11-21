package com.example.elearning.data.repository

import com.example.elearning.domain.AuthError
import com.example.elearning.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data class Success(val user: AuthUser) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun signOut()
    suspend fun updateDisplayName(name: String): AuthResult
}
