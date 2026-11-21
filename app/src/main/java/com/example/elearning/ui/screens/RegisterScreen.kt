package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun RegisterScreen(
    onCourseList: () -> Unit
) {
    Column {
        Text("Rejestracja")
        Button(onClick = onCourseList) { Text("Kursy") }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterPreview() {
    ElearningTheme { RegisterScreen({}) }
}
