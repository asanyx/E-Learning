package com.example.elearning.ui.screens

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.screens.auth.*
import com.example.elearning.ui.theme.ElearningTheme
import com.example.elearning.ui.viewmodel.AuthUiState
import com.example.elearning.domain.AuthError
import com.example.elearning.domain.validation.ValidationError
import com.example.platformaELearningowa.R

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val submit = { if (!uiState.isLoading) onLogin(email, password) }
    AuthForm(uiState, R.string.auth_login_title, R.string.auth_login, R.string.auth_to_register,
        submit, onRegister) {
        AuthField(email, { email = it }, R.string.auth_email, KeyboardType.Email,
            error = uiState.validationErrors.firstOrNull {
                it == ValidationError.EMPTY_EMAIL || it == ValidationError.INVALID_EMAIL
            })
        AuthField(password, { password = it }, R.string.auth_password, KeyboardType.Password,
            error = uiState.validationErrors.firstOrNull {
                it == ValidationError.EMPTY_PASSWORD || it == ValidationError.PASSWORD_TOO_SHORT || it == ValidationError.PASSWORD_NEEDS_LETTER_AND_DIGIT
            }, onDone = submit)
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginEmptyPreview() {
    ElearningTheme {
        LoginScreen(AuthUiState(), { _, _ -> }, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginErrorsPreview() {
    ElearningTheme {
        LoginScreen(AuthUiState(validationErrors = listOf(ValidationError.EMPTY_EMAIL), authError = AuthError.NETWORK), { _, _ -> }, {})
    }
}
