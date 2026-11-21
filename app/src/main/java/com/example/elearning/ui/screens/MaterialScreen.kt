package com.example.elearning.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.elearning.domain.model.Material
import com.example.elearning.domain.model.MaterialType
import com.example.elearning.ui.theme.ElearningTheme
import com.example.elearning.ui.viewmodel.MaterialUiState
import com.example.platformaELearningowa.R

@Composable
fun MaterialScreen(uiState: MaterialUiState, onToggleCompleted: () -> Unit, onBack: () -> Unit) {
    val material = uiState.material
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.profile_back)) }
            Text(material?.title ?: stringResource(R.string.material_title), style = MaterialTheme.typography.titleLarge)
        }
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            uiState.notFound -> Text(stringResource(R.string.material_missing))
            material != null -> {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    when (material.type) {
                        MaterialType.TEXT -> Text(material.contentUrl)
                        MaterialType.PDF, MaterialType.VIDEO -> {
                            val label = if (material.type == MaterialType.PDF) R.string.material_pdf else R.string.material_video
                            Button(onClick = { openMaterial(context, material) }) { Text(stringResource(label)) }
                        }
                    }
                }
                val label = if (material.isCompleted) R.string.material_unmark else R.string.material_mark
                Button(onClick = onToggleCompleted, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(stringResource(label))
                }
            }
        }
    }
}

private fun openMaterial(context: Context, material: Material) {
    val uri = Uri.parse(material.contentUrl).normalizeScheme()
    val supportedScheme = uri.scheme == "https" || (material.type == MaterialType.VIDEO && uri.scheme == "http")
    if (!supportedScheme || uri.host.isNullOrBlank()) {
        Toast.makeText(context, R.string.material_invalid_link, Toast.LENGTH_SHORT).show()
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (error: ActivityNotFoundException) {
        Toast.makeText(context, R.string.material_no_app, Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
private fun TextMaterialPreview() {
    val material = Material("1", "kurs", "Zmienne", MaterialType.TEXT, false, false, "Słowo val oznacza wartość tylko do odczytu.")
    ElearningTheme { MaterialScreen(MaterialUiState(material, isLoading = false), {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun PdfMaterialPreview() {
    val material = Material("2", "kurs", "Dokument", MaterialType.PDF, false, true, "https://example.org/lekcja.pdf")
    ElearningTheme { MaterialScreen(MaterialUiState(material, isLoading = false), {}, {}) }
}
