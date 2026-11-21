package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.platformaELearningowa.R
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
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.courses_title), Modifier.weight(1f))
            IconButton(onClick = onProfile) {
                Icon(painterResource(R.drawable.ic_profile), stringResource(R.string.profile_title))
            }
        }
        Button(onClick = { onCourseDetail("course-1") }) { Text("Szczegóły kursu") }
        Button(onClick = onProgress) { Text("Postępy") }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseListPreview() {
    ElearningTheme { CourseListScreen({}, {}, {}) }
}
