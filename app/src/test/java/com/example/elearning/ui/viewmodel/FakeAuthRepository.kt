package com.example.elearning.ui.viewmodel

import com.example.elearning.data.repository.AuthRepository
import com.example.elearning.data.repository.AuthResult
import com.example.elearning.domain.model.AuthUser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAuthRepository : AuthRepository {
    val account = AuthUser("1", "Jan", "jan@example.com")
    override val currentUser = MutableStateFlow<AuthUser?>(null)
    var calls = 0
    var result: AuthResult = AuthResult.Success(account)
    val responseReady = CompletableDeferred<Unit>()

    private suspend fun authenticate(): AuthResult {
        calls++
        responseReady.await()
        (result as? AuthResult.Success)?.let { currentUser.value = it.user }
        return result
    }

    override suspend fun signIn(email: String, password: String) = authenticate()
    override suspend fun register(name: String, email: String, password: String) = authenticate()
    override suspend fun signOut() { currentUser.value = null }
    override suspend fun updateDisplayName(name: String): AuthResult {
        if (result is AuthResult.Success) result = AuthResult.Success(account.copy(name = name))
        return authenticate()
    }
}
