package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.domain.AuthError
import com.example.elearning.domain.model.AuthUser
import com.example.elearning.domain.validation.ValidationError
import com.example.elearning.ui.screens.auth.messageRes
import com.example.elearning.ui.theme.ElearningTheme
import com.example.platformaELearningowa.R

@Composable
fun ProfileScreen(
    user: AuthUser,
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    isLoading: Boolean,
    nameError: ValidationError?,
    authError: AuthError?,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.profile_back)) }
            Text(stringResource(R.string.profile_title), style = MaterialTheme.typography.titleLarge)
        }
        Column(
            Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(user.name, style = MaterialTheme.typography.headlineSmall)
            Text(user.email)
            OutlinedTextField(
                value = name, onValueChange = onNameChange, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !isLoading,
                label = { Text(stringResource(R.string.auth_name)) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(stringResource(it.messageRes())) } }
            )
            authError?.let { Text(stringResource(it.messageRes()), color = MaterialTheme.colorScheme.error) }
            Button(onClick = onSave, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.profile_save))
            }
            OutlinedButton(onClick = onSignOut, enabled = !isLoading) {
                Text(stringResource(R.string.profile_sign_out))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    ElearningTheme {
        val user = AuthUser("1", "Anna", "anna@example.com")
        ProfileScreen(user, user.name, {}, {}, false, null, null, {}, {})
    }
}
