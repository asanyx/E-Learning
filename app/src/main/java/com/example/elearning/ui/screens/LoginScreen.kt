package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun LoginScreen(
    onRegister: () -> Unit,
    onCourseList: () -> Unit
) {
    Column {
        Text("Logowanie")
        Button(onClick = onRegister) { Text("Rejestracja") }
        Button(onClick = onCourseList) { Text("Kursy") }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    ElearningTheme { LoginScreen({}, {}) }
}
