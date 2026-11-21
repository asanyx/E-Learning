package com.example.elearning.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.domain.model.*
import com.example.elearning.ui.theme.ElearningTheme
import com.example.elearning.ui.viewmodel.CourseDetailUiState
import com.example.platformaELearningowa.R

@Composable
fun CourseDetailScreen(
    uiState: CourseDetailUiState,
    onBack: () -> Unit,
    onToggleCompleted: (String) -> Unit,
    onMaterialClick: (String) -> Unit,
    onQuizClick: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.profile_back)) }
            Text(uiState.course?.title ?: stringResource(R.string.detail_title), style = MaterialTheme.typography.titleLarge)
        }
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            uiState.notFound -> Text(stringResource(R.string.detail_missing))
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(uiState.course?.description.orEmpty())
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.detail_progress, uiState.completedCount, uiState.totalCount))
                    LinearProgressIndicator(
                        progress = { uiState.completedCount.toFloat() / uiState.totalCount.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Text(stringResource(R.string.detail_materials), style = MaterialTheme.typography.titleMedium) }
                if (uiState.materials.isEmpty()) item { Text(stringResource(R.string.detail_no_materials)) }
                items(uiState.materials) { material ->
                    MaterialRow(material, { onMaterialClick(material.id) }, { onToggleCompleted(material.id) })
                }
                item { Text(stringResource(R.string.detail_quizzes), style = MaterialTheme.typography.titleMedium) }
                if (uiState.quizzes.isEmpty()) item { Text(stringResource(R.string.detail_no_quizzes)) }
                items(uiState.quizzes) { quiz ->
                    TextButton(onClick = { onQuizClick(quiz.id) }) { Text(quiz.title) }
                }
            }
        }
    }
}

@Composable
private fun MaterialRow(material: Material, onOpen: () -> Unit, onToggle: () -> Unit) {
    val (icon, label) = when (material.type) {
        MaterialType.TEXT -> R.drawable.ic_detail_text to R.string.detail_text
        MaterialType.PDF -> R.drawable.ic_detail_pdf to R.string.detail_pdf
        MaterialType.VIDEO -> R.drawable.ic_detail_video to R.string.detail_video
    }
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), stringResource(label))
        Text(material.title, Modifier.weight(1f))
        if (material.isDownloaded) {
            Icon(painterResource(R.drawable.ic_detail_downloaded), stringResource(R.string.detail_downloaded))
        }
        val completion = stringResource(R.string.detail_completion, material.title)
        Checkbox(material.isCompleted, onCheckedChange = { onToggle() },
            modifier = Modifier.semantics { contentDescription = completion })
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    val state = CourseDetailUiState(
        course = Course("1", "Podstawy Kotlina", "Zmienne, typy i funkcje.", "Programowanie"),
        materials = listOf(Material("m", "1", "Zmienne", MaterialType.TEXT, true, true)),
        quizzes = listOf(Quiz("q", "1", "Sprawdź podstawy", 60)),
        completedCount = 1, totalCount = 1, isLoading = false
    )
    ElearningTheme { CourseDetailScreen(state, {}, {}, {}, {}) }
}
