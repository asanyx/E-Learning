package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun MaterialScreen(
    materialId: String,
    onQuiz: (String) -> Unit
) {
    Column {
        Text("Materiał")
        Text("materialId = $materialId")
        Button(onClick = { onQuiz("quiz-1") }) { Text("Quiz") }
    }
}

@Preview(showBackground = true)
@Composable
private fun MaterialPreview() {
    ElearningTheme { MaterialScreen("demo-1", {}) }
}
