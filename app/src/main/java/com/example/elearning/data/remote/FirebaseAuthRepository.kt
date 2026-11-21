package com.example.elearning.data.remote

import com.example.elearning.data.repository.AuthRepository
import com.example.elearning.data.repository.AuthResult
import com.example.elearning.domain.AuthError
import com.example.elearning.domain.model.AuthUser
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    // Aktualizacja profilu nie musi wywołać AuthStateListener.
    private val profileRevision = MutableStateFlow(0L)

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(Unit) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.combine(profileRevision) { _, _ -> auth.currentUser?.toAuthUser() }.distinctUntilChanged()

    override suspend fun signIn(email: String, password: String) = authResult {
        auth.signInWithEmailAndPassword(email.trim(), password).awaitValue().user?.toAuthUser()
    }

    override suspend fun register(name: String, email: String, password: String) = authResult {
        auth.createUserWithEmailAndPassword(email.trim(), password).awaitValue().user?.saveName(name)
    }

    override suspend fun updateDisplayName(name: String) = authResult {
        auth.currentUser?.saveName(name)
    }

    override suspend fun signOut() = auth.signOut()

    private suspend fun FirebaseUser.saveName(name: String): AuthUser {
        updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).awaitValue()
        profileRevision.update { it + 1 }
        return toAuthUser()
    }

    private fun FirebaseUser.toAuthUser() = AuthUser(uid, displayName.orEmpty(), email.orEmpty())

    private suspend fun authResult(action: suspend () -> AuthUser?): AuthResult = try {
        action()?.let(AuthResult::Success) ?: AuthResult.Failure(AuthError.UNKNOWN)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        AuthResult.Failure(when (error) {
            is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
            is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
            is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
            is FirebaseNetworkException -> AuthError.NETWORK
            else -> AuthError.UNKNOWN
        })
    }
}

private suspend fun <T> Task<T>.awaitValue(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        val error = task.exception
        when {
            task.isCanceled -> continuation.cancel()
            error != null -> continuation.resumeWithException(error)
            else -> continuation.resume(task.result)
        }
    }
}
