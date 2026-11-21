package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.AuthResult
import com.example.elearning.domain.AuthError
import com.example.elearning.domain.validation.ValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeAuthRepository()
    private lateinit var model: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        model = AuthViewModel(repository)
    }

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun invalidFormsSkipRepository() = runTest {
        model.login("", "")
        assertEquals(listOf(ValidationError.EMPTY_EMAIL, ValidationError.EMPTY_PASSWORD), model.uiState.value.validationErrors)
        model.register("", "", "", "")
        runCurrent()
        assertTrue(ValidationError.EMPTY_NAME in model.uiState.value.validationErrors)
        assertEquals(0, repository.calls)
        model.clearErrors()
        assertTrue(model.uiState.value.validationErrors.isEmpty())
    }

    @Test
    fun loginIgnoresRepeatedSubmissionsAndPublishesUser() = runTest {
        model.login(repository.account.email, "x")
        model.login(repository.account.email, "x")
        model.register("Jan", repository.account.email, "1234567a", "1234567a")
        runCurrent()
        assertTrue(model.uiState.value.isLoading)
        assertEquals(1, repository.calls)
        repository.responseReady.complete(Unit)
        runCurrent()
        assertEquals(repository.account, model.uiState.value.user)
        assertFalse(model.uiState.value.isLoading)
    }

    @Test
    fun invalidCredentialsEndLoadingAndCanBeCleared() = runTest {
        repository.result = AuthResult.Failure(AuthError.INVALID_CREDENTIALS)
        repository.responseReady.complete(Unit)
        model.login(repository.account.email, "x")
        runCurrent()
        assertEquals(AuthError.INVALID_CREDENTIALS, model.uiState.value.authError)
        assertFalse(model.uiState.value.isLoading)
        model.clearErrors()
        assertNull(model.uiState.value.authError)
    }

    @Test
    fun restoredSessionIsClearedOnSignOut() = runTest {
        repository.currentUser.value = repository.account
        runCurrent()
        assertEquals(repository.account, model.uiState.value.user)
        model.signOut()
        runCurrent()
        assertNull(model.uiState.value.user)
    }

    @Test
    fun initializationEndsAfterFirstAnonymousEmission() {
        assertTrue(model.uiState.value.isInitializing)
        dispatcher.scheduler.runCurrent()
        assertFalse(model.uiState.value.isInitializing)
        assertNull(model.uiState.value.user)
    }

    @Test
    fun invalidNameSkipsRepository() = runTest {
        model.updateName(" J ")
        runCurrent()
        assertEquals(listOf(ValidationError.NAME_TOO_SHORT), model.uiState.value.validationErrors)
        assertEquals(0, repository.calls)
    }

    @Test
    fun savedNameUpdatesUser() = runTest {
        repository.currentUser.value = repository.account
        model.updateName(" Anna ")
        runCurrent()
        assertTrue(model.uiState.value.isLoading)
        repository.responseReady.complete(Unit)
        runCurrent()
        assertEquals(repository.account.copy(name = "Anna"), model.uiState.value.user)
        assertFalse(model.uiState.value.isLoading)
    }

    @Test
    fun failedNameUpdateKeepsUserAndShowsError() = runTest {
        repository.currentUser.value = repository.account
        repository.result = AuthResult.Failure(AuthError.NETWORK)
        repository.responseReady.complete(Unit)
        model.updateName("Anna")
        runCurrent()
        assertEquals(AuthError.NETWORK, model.uiState.value.authError)
        assertEquals(repository.account, model.uiState.value.user)
        assertFalse(model.uiState.value.isLoading)
    }
}
