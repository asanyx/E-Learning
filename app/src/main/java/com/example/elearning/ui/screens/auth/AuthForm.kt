package com.example.elearning.ui.screens.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.elearning.domain.validation.ValidationError
import com.example.elearning.ui.viewmodel.AuthUiState
import com.example.platformaELearningowa.R

@Composable
internal fun AuthForm(
    state: AuthUiState, @StringRes title: Int, @StringRes button: Int, @StringRes link: Int,
    submit: () -> Unit, switchForm: () -> Unit, fields: @Composable () -> Unit
) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
        fields()
        state.authError?.let { Text(stringResource(it.messageRes()), color = MaterialTheme.colorScheme.error) }
        Button(onClick = submit, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(stringResource(button))
        }
        TextButton(onClick = switchForm, enabled = !state.isLoading) { Text(stringResource(link)) }
    }
}

@Composable
internal fun AuthField(
    value: String, change: (String) -> Unit, @StringRes label: Int, type: KeyboardType,
    error: ValidationError?, onDone: (() -> Unit)? = null
) {
    val secret = type == KeyboardType.Password
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value, onValueChange = change, modifier = Modifier.fillMaxWidth(),
        singleLine = true, label = { Text(stringResource(label)) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it.messageRes())) } },
        keyboardOptions = KeyboardOptions(keyboardType = type, imeAction = if (onDone == null) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (secret) ({
            val description = stringResource(if (visible) R.string.auth_hide else R.string.auth_show)
            IconButton(onClick = { visible = !visible }, modifier = Modifier.semantics { contentDescription = description }) {
                val color = LocalContentColor.current
                Canvas(Modifier.size(22.dp, 14.dp)) {
                    drawOval(color, style = Stroke(1.5.dp.toPx()))
                    drawCircle(color, radius = 3.dp.toPx())
                    if (visible) drawLine(color, Offset.Zero, Offset(size.width, size.height), 2.dp.toPx())
                }
            }
        }) else null
    )
}
