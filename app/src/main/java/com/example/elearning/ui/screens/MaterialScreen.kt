package com.example.elearning.ui.screens

import android.content.ActivityNotFoundException
import com.example.elearning.domain.network.Availability
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import com.example.elearning.data.download.DownloadState
import com.example.elearning.domain.download.DownloadPolicy
import com.example.elearning.domain.download.isSafeExternalUrl
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
fun MaterialScreen(
    uiState: MaterialUiState,
    onToggleCompleted: () -> Unit,
    onBack: () -> Unit,
    saveErrors: Flow<Unit> = emptyFlow(),
    downloadState: DownloadState = DownloadState.NotDownloaded,
    downloadErrors: Flow<Unit> = emptyFlow(),
    onStart: () -> Unit = {},
    onCancel: () -> Unit = {},
    onDelete: () -> Unit = {},
    onRetry: () -> Unit = {},
    onReload: () -> Unit = {}
) {
    val material = uiState.material
    val context = LocalContext.current
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val failure = stringResource(R.string.download_failure)
    LaunchedEffect(downloadErrors, failure) {
        downloadErrors.collect { host.showSnackbar(failure) }
    }
    CompletionSnackbar(saveErrors, host) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.profile_back)) }
                Text(material?.title ?: stringResource(R.string.material_title), style = MaterialTheme.typography.titleLarge)
            }
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                uiState.loadError -> {
                    Text(stringResource(R.string.load_material_error))
                    TextButton(onClick = onReload) { Text(stringResource(R.string.retry_download)) }
                }
                uiState.notFound -> Text(stringResource(R.string.material_missing))
                material != null -> {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        when (material.type) {
                            MaterialType.TEXT -> Text(material.contentUrl)
                            MaterialType.PDF, MaterialType.VIDEO -> {
                                val label = if (material.type == MaterialType.PDF) R.string.material_pdf else R.string.material_video
                                val allowed = DownloadPolicy.isDownloadable(material.type, material.contentUrl)
                                val canOpen = uiState.availability == Availability.AVAILABLE
                                Button(enabled = canOpen, onClick = {
                                    val result = openMaterial(context, material, downloadState.takeIf { allowed })
                                    if (result != null) {
                                        if (result == R.string.missing_file) onDelete()
                                        scope.launch { host.showSnackbar(context.getString(result)) }
                                    }
                                }) { Text(stringResource(label)) }
                                if (!canOpen) Text(stringResource(R.string.material_internet_hint))
                                if (allowed) OfflineSection(downloadState, onStart, onCancel, onDelete, onRetry)
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
}

@StringRes
internal fun downloadFailureMessage(reason: String): Int = when (reason) {
    "Plik przekracza limit 100 MB" -> R.string.too_large
    "Brak miejsca" -> R.string.no_space
    "Niebezpieczny adres", "Niedozwolony materiał lub adres" -> R.string.unsafe_url
    "Wyczerpano próby pobierania" -> R.string.network_failure
    else -> if (reason.startsWith("HTTP ")) R.string.http_failure else R.string.download_failure
}

@Composable
private fun OfflineSection(
    state: DownloadState, onStart: () -> Unit, onCancel: () -> Unit,
    onDelete: () -> Unit, onRetry: () -> Unit
) {
    Text(stringResource(R.string.offline), Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleMedium)
    when (state) {
        DownloadState.NotDownloaded -> Button(onClick = onStart) { Text(stringResource(R.string.download)) }
        DownloadState.Queued, is DownloadState.Downloading -> {
            if (state is DownloadState.Downloading) {
                val percent = state.percent
                if (percent == null) {
                    Text(stringResource(R.string.downloading))
                    LinearProgressIndicator()
                } else {
                    Text(stringResource(R.string.percent, percent))
                    LinearProgressIndicator(progress = { percent / 100f })
                }
            } else Text(stringResource(R.string.queued))
            TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel_download)) }
        }
        is DownloadState.Downloaded -> {
            val available = stringResource(R.string.detail_downloaded)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_detail_downloaded), available)
                Text(available, Modifier.padding(start = 8.dp))
            }
            var confirm by rememberSaveable { mutableStateOf(false) }
            TextButton(onClick = { confirm = true }) { Text(stringResource(R.string.remove_download)) }
            if (confirm) AlertDialog(
                onDismissRequest = { confirm = false },
                title = { Text(stringResource(R.string.confirm_remove)) },
                confirmButton = {
                    TextButton(onClick = { confirm = false
                        onDelete()
                    }) { Text(stringResource(R.string.remove_download)) }
                },
                dismissButton = {
                    TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel_download)) }
                }
            )
        }
        is DownloadState.Failed -> {
            Text(stringResource(downloadFailureMessage(state.reason)))
            Button(onClick = onRetry) { Text(stringResource(R.string.retry_download)) }
        }
    }
}

@StringRes
private fun openMaterial(context: Context, material: Material, state: DownloadState?): Int? {
    val intent = Intent(Intent.ACTION_VIEW)
    if (state is DownloadState.Downloaded) {
        val file = File(state.path)
        if (!file.isFile) return R.string.missing_file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        intent.setDataAndType(uri, if (material.type == MaterialType.PDF) "application/pdf" else "video/mp4")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } else {
        if (!isSafeExternalUrl(material.contentUrl)) return R.string.material_invalid_link
        intent.data = Uri.parse(material.contentUrl).normalizeScheme()
    }
    return try {
        context.startActivity(intent)
        null
    } catch (_: ActivityNotFoundException) {
        R.string.material_no_app
    }
}

@Preview(showBackground = true)
@Composable
private fun TextMaterialPreview() {
    val material = Material("1", "kurs", "Zmienne", MaterialType.TEXT, false, false, "Słowo val oznacza wartość tylko do odczytu.")
    ElearningTheme { MaterialScreen(MaterialUiState(material, isLoading = false), {}, {}) }
}

@Composable
private fun PdfMaterialPreview(state: DownloadState = DownloadState.NotDownloaded) {
    val material = Material("2", "kurs", "Dokument", MaterialType.PDF, false, true, "https://example.org/lekcja.pdf")
    ElearningTheme { MaterialScreen(MaterialUiState(material, isLoading = false), {}, {}, downloadState = state) }
}

@Preview(showBackground = true)
@Composable
private fun NotDownloadedPreview() = PdfMaterialPreview(DownloadState.NotDownloaded)

@Preview(showBackground = true)
@Composable
private fun DownloadingPreview() = PdfMaterialPreview(DownloadState.Downloading(42))

@Preview(showBackground = true)
@Composable
private fun DownloadedPreview() = PdfMaterialPreview(DownloadState.Downloaded("plik.pdf"))

@Preview(showBackground = true)
@Composable
private fun FailedPreview() = PdfMaterialPreview(DownloadState.Failed("Brak miejsca"))
