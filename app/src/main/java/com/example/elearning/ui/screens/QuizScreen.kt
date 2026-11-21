package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.domain.quiz.QuizQuestion
import com.example.elearning.ui.theme.ElearningTheme
import com.example.elearning.ui.viewmodel.QuizUiState
import com.example.platformaELearningowa.R

@Composable
fun QuizScreen(
    state: QuizUiState, onBack: () -> Unit, onAnswer: (Int) -> Unit,
    onPrevious: () -> Unit, onNext: () -> Unit, onFinish: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.profile_back)) }
            Text(state.quizTitle.ifEmpty { stringResource(R.string.quiz_title) }, style = MaterialTheme.typography.titleLarge)
        }
        val question = state.currentQuestion
        when {
            state.isLoading -> CircularProgressIndicator()
            state.notFound -> Text(stringResource(R.string.quiz_missing))
            question == null -> Text(stringResource(R.string.quiz_empty))
            else -> {
                Text(stringResource(R.string.quiz_position, state.currentIndex + 1, state.questions.size))
                LinearProgressIndicator(progress = { (state.currentIndex + 1f) / state.questions.size }, modifier = Modifier.fillMaxWidth())
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(question.text, style = MaterialTheme.typography.titleMedium)
                    Column(Modifier.selectableGroup()) {
                        question.options.forEachIndexed { index, option ->
                            val selected = state.answers[question.id] == index
                            Row(
                                Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = { onAnswer(index) })
                                    .heightIn(min = 48.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(option)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    if (state.currentIndex > 0) {
                        TextButton(onClick = onPrevious) { Text(stringResource(R.string.quiz_previous)) }
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = if (state.isLastQuestion) onFinish else onNext, enabled = state.canGoNext) {
                        Text(stringResource(if (state.isLastQuestion) R.string.quiz_finish else R.string.quiz_next))
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuitQuizDialog(onQuit: () -> Unit, onStay: () -> Unit) {
    AlertDialog(
        onDismissRequest = onStay,
        title = { Text(stringResource(R.string.quiz_quit)) },
        text = { Text(stringResource(R.string.quiz_lose_progress)) },
        confirmButton = { TextButton(onClick = onQuit) { Text(stringResource(R.string.quiz_yes)) } },
        dismissButton = { TextButton(onClick = onStay) { Text(stringResource(R.string.quiz_no)) } }
    )
}

@Preview(showBackground = true)
@Composable
private fun QuizPreview() {
    val question = QuizQuestion("1", "Który typ przechowuje tekst?", listOf("String", "Int"), 0)
    ElearningTheme {
        QuizScreen(QuizUiState("Podstawy Kotlina", listOf(question), isLoading = false), {}, {}, {}, {}, {})
    }
}
