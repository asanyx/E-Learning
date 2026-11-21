package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.elearning.ui.theme.ElearningTheme

@Composable
fun CourseDetailScreen(
    courseId: String,
    onMaterial: (String) -> Unit
) {
    Column {
        Text("Szczegóły kursu")
        Text("courseId = $courseId")
        Button(onClick = { onMaterial("material-1") }) { Text("Materiał") }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreview() {
    ElearningTheme { CourseDetailScreen("demo-1", {}) }
}
