package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.domain.model.Course
import com.example.elearning.ui.theme.ElearningTheme
import com.example.elearning.ui.viewmodel.CourseListUiState
import com.example.platformaELearningowa.R

@Composable
fun CourseListScreen(
    uiState: CourseListUiState,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onCourseClick: (String) -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.courses_title), Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onProgress) { Text(stringResource(R.string.courses_progress)) }
            IconButton(onClick = onProfile) {
                Icon(painterResource(R.drawable.ic_profile), stringResource(R.string.profile_title))
            }
        }
        OutlinedTextField(
            value = uiState.query, onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text(stringResource(R.string.courses_search)) },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(painterResource(R.drawable.ic_clear_search), stringResource(R.string.courses_clear))
                    }
                }
            }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(selected = uiState.selectedCategory == null, onClick = { onCategoryChange(null) },
                    label = { Text(stringResource(R.string.courses_all)) })
            }
            items(uiState.categories) { category ->
                FilterChip(selected = uiState.selectedCategory == category, onClick = { onCategoryChange(category) },
                    label = { Text(category) })
            }
        }
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            uiState.courses.isEmpty() -> Text(stringResource(R.string.courses_empty))
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.courses, key = { it.id }) { course ->
                    Card(onClick = { onCourseClick(course.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(course.title, style = MaterialTheme.typography.titleMedium)
                            Text(course.category, style = MaterialTheme.typography.labelMedium)
                            Text(course.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseListPreview() {
    val course = Course("kotlin", "Podstawy Kotlina", "Zmienne, typy i funkcje.", "Programowanie")
    ElearningTheme {
        CourseListScreen(CourseListUiState(listOf(course), listOf(course.category), isLoading = false), {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyCourseListPreview() {
    ElearningTheme { CourseListScreen(CourseListUiState(isLoading = false), {}, {}, {}, {}, {}) }
}
