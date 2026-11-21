package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun CourseListScreen(
    onCourseDetail: (String) -> Unit,
    onProgress: () -> Unit,
    onProfile: () -> Unit
) {
    Column {
        Text("Kursy")
        Button(onClick = { onCourseDetail("course-1") }) { Text("Szczegóły kursu") }
        Button(onClick = onProgress) { Text("Postępy") }
        Button(onClick = onProfile) { Text("Profil") }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseListPreview() {
    ElearningTheme { CourseListScreen({}, {}, {}) }
}
