package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun QuizResultScreen(
    quizId: String,
    onProgress: () -> Unit
) {
    Column {
        Text("Wynik quizu")
        Text("quizId = $quizId")
        Button(onClick = onProgress) { Text("Postępy") }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizResultPreview() {
    ElearningTheme { QuizResultScreen("demo-1", {}) }
}
