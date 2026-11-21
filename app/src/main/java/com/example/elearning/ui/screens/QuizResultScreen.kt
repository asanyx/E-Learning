package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.ui.viewmodel.SaveStatus
import com.example.elearning.domain.quiz.QuizOutcome
import com.example.elearning.domain.quiz.ReviewItem
import com.example.elearning.ui.theme.ElearningTheme
import com.example.platformaELearningowa.R

@Composable
fun QuizResultScreen(
    outcome: QuizOutcome,
    review: List<ReviewItem>,
    onRetry: () -> Unit,
    onReturnToCourse: () -> Unit,
    saveStatus: SaveStatus,
    onRetrySave: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onReturnToCourse) {
                Icon(painterResource(R.drawable.ic_quiz_back), stringResource(R.string.profile_back))
            }
            Text(stringResource(R.string.quiz_result_title), style = MaterialTheme.typography.titleLarge)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Text(stringResource(R.string.quiz_result_count, outcome.correctCount, outcome.total))
                Text(stringResource(R.string.quiz_result_percent, outcome.percent), style = MaterialTheme.typography.headlineMedium)
                ResultStatus(outcome.passed, stringResource(if (outcome.passed) R.string.quiz_passed else R.string.quiz_failed))
            }
            if (saveStatus == SaveStatus.FAILED) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(stringResource(R.string.quiz_save_failed))
                            TextButton(onClick = onRetrySave) { Text(stringResource(R.string.quiz_retry)) }
                        }
                    }
                }
            } else if (saveStatus == SaveStatus.SAVING) {
                item { CircularProgressIndicator(Modifier.size(24.dp)) }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRetry, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.quiz_new_attempt)) }
                    OutlinedButton(onClick = onReturnToCourse, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.quiz_return)) }
                }
            }
            items(review) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.questionText, style = MaterialTheme.typography.titleMedium)
                        ResultStatus(item.isCorrect, stringResource(if (item.isCorrect) R.string.quiz_correct else R.string.quiz_incorrect))
                        Text(stringResource(R.string.quiz_selected, item.selectedAnswerText ?: stringResource(R.string.quiz_no_answer)))
                        Text(stringResource(R.string.quiz_correct_answer, item.correctAnswerText ?: stringResource(R.string.quiz_unavailable)))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultStatus(correct: Boolean, label: String) {
    val color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(painterResource(if (correct) R.drawable.ic_quiz_check else R.drawable.ic_clear_search), null, tint = color)
        Text(label, color = color, style = MaterialTheme.typography.titleSmall)
    }
}

@Preview(showBackground = true)
@Composable
private fun PassedQuizPreview() {
    ElearningTheme {
        QuizResultScreen(QuizOutcome(1, 1, 100, true, emptyList()),
            listOf(ReviewItem("Który typ przechowuje tekst?", "String", "String", true)), {}, {}, SaveStatus.SAVED, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FailedQuizPreview() {
    ElearningTheme {
        QuizResultScreen(QuizOutcome(0, 1, 0, false, emptyList()),
            listOf(ReviewItem("Który typ przechowuje tekst?", "Int", "String", false)), {}, {}, SaveStatus.FAILED, {})
    }
}
