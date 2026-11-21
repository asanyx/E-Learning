package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun ProfileScreen(
    onBack: () -> Unit
) {
    Column {
        Text("Profil")
        Button(onClick = onBack) { Text("Wstecz") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreview() {
    ElearningTheme { ProfileScreen({}) }
}
