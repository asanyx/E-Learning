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
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (name: String, email: String, password: String, confirmation: String) -> Unit,
    onLogin: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    val submit = { if (!uiState.isLoading) onRegister(name, email, password, confirmation) }
    AuthForm(uiState, R.string.auth_register_title, R.string.auth_register, R.string.auth_to_login,
        submit, onLogin) {
        AuthField(name, { name = it }, R.string.auth_name, KeyboardType.Text,
            error = uiState.validationErrors.firstOrNull {
                it == ValidationError.EMPTY_NAME || it == ValidationError.NAME_TOO_SHORT
            })
        AuthField(email, { email = it }, R.string.auth_email, KeyboardType.Email,
            error = uiState.validationErrors.firstOrNull {
                it == ValidationError.EMPTY_EMAIL || it == ValidationError.INVALID_EMAIL
            })
        AuthField(password, { password = it }, R.string.auth_password, KeyboardType.Password,
            error = uiState.validationErrors.firstOrNull {
                it == ValidationError.EMPTY_PASSWORD || it == ValidationError.PASSWORD_TOO_SHORT || it == ValidationError.PASSWORD_NEEDS_LETTER_AND_DIGIT
            })
        AuthField(confirmation, { confirmation = it }, R.string.auth_confirmation, KeyboardType.Password,
            error = uiState.validationErrors.firstOrNull {
                it == ValidationError.PASSWORDS_DO_NOT_MATCH
            }, onDone = submit)
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterEmptyPreview() {
    ElearningTheme {
        RegisterScreen(AuthUiState(), { _, _, _, _ -> }, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterErrorsPreview() {
    ElearningTheme {
        RegisterScreen(AuthUiState(validationErrors = listOf(ValidationError.EMPTY_EMAIL), authError = AuthError.NETWORK), { _, _, _, _ -> }, {})
    }
}
