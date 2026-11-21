package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun QuizScreen(
    quizId: String,
    onQuizResult: (String) -> Unit
) {
    Column {
        Text("Quiz")
        Text("quizId = $quizId")
        Button(onClick = { onQuizResult(quizId) }) { Text("Wynik quizu") }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizPreview() {
    ElearningTheme { QuizScreen("demo-1", {}) }
}
