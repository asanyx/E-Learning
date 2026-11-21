package com.example.elearning.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.platformaELearningowa.R

@Composable
fun QuizResultScreen(percent: Int) {
    Text(stringResource(R.string.quiz_result_percent, percent))
}
