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
import com.example.elearning.domain.progress.CourseProgress
import com.example.elearning.ui.theme.ElearningTheme
import com.example.elearning.ui.viewmodel.CourseProgressItem
import com.example.elearning.ui.viewmodel.ProgressUiState
import com.example.elearning.ui.viewmodel.ResultItem
import com.example.platformaELearningowa.R
import java.text.DateFormat
import java.util.Date

@Composable
fun ProgressScreen(state: ProgressUiState, onBack: () -> Unit, onCourseClick: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_quiz_back), stringResource(R.string.profile_back))
            }
            Text(stringResource(R.string.progress_title), style = MaterialTheme.typography.titleLarge)
        }
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                item { Text(stringResource(R.string.courses_title), style = MaterialTheme.typography.titleMedium) }
                items(state.courses, key = { it.courseId }) { course ->
                    ProgressCard(course) { onCourseClick(course.courseId) }
                }
                item { Text(stringResource(R.string.progress_history), style = MaterialTheme.typography.titleMedium) }
                if (state.history.isEmpty()) item { Text(stringResource(R.string.progress_empty)) }
                items(state.history) { result -> HistoryRow(result) }
            }
        }
    }
}

@Composable
private fun ProgressCard(item: CourseProgressItem, onClick: () -> Unit) {
    val progress = item.progress
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title.ifBlank { stringResource(R.string.progress_unknown_course) }, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.progress_percent, progress.percent))
            LinearProgressIndicator(progress = { progress.percent / 100f }, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.progress_materials, progress.completedMaterials, progress.totalMaterials))
            Text(stringResource(R.string.progress_quizzes, progress.passedQuizzes, progress.totalQuizzes))
            if (progress.isCompleted) Text(stringResource(R.string.progress_completed))
            progress.bestPercentByQuiz.forEach { (id, percent) ->
                val title = item.quizTitles[id].orEmpty().ifBlank { stringResource(R.string.progress_unknown_quiz) }
                Text(stringResource(R.string.progress_best, title, percent))
            }
        }
    }
}

@Composable
private fun HistoryRow(result: ResultItem) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(result.quizTitle.orEmpty().ifBlank { stringResource(R.string.progress_unknown_quiz) },
            style = MaterialTheme.typography.titleSmall)
        Text(result.courseTitle.orEmpty().ifBlank { stringResource(R.string.progress_unknown_course) })
        Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(result.completedAt)))
        Text(stringResource(R.string.progress_score, result.correctCount, result.total, result.percent))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painterResource(if (result.passed) R.drawable.ic_quiz_check else R.drawable.ic_clear_search), null,
                tint = if (result.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Text(stringResource(if (result.passed) R.string.quiz_passed else R.string.quiz_failed))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview() {
    val course = CourseProgressItem("k", "Podstawy Kotlina",
        CourseProgress(1, 1, 1, 1, 100, true, mapOf("q" to 80)), mapOf("q" to "Typy"))
    val result = ResultItem("Typy", course.title, 80, true, 4, 5, 1700000000000L)
    ElearningTheme { ProgressScreen(ProgressUiState(listOf(course), listOf(result), false), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun EmptyProgressPreview() {
    ElearningTheme { ProgressScreen(ProgressUiState(isLoading = false), {}, {}) }
}
