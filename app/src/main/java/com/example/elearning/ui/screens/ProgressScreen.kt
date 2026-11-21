package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun ProgressScreen(
    onProfile: () -> Unit
) {
    Column {
        Text("Postępy")
        Button(onClick = onProfile) { Text("Profil") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview() {
    ElearningTheme { ProgressScreen({}) }
}
