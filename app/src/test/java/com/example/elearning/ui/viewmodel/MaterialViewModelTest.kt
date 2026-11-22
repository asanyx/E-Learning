package com.example.elearning.ui.viewmodel

import com.example.elearning.data.download.DownloadState
import com.example.elearning.data.download.MaterialDownloadRepository
import com.example.elearning.ui.screens.downloadFailureMessage
import com.example.platformaELearningowa.R
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.domain.model.Material
import com.example.elearning.domain.model.MaterialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MaterialViewModelTest {
    private val repository = FakeMaterialRepository()
    private val downloads = FakeMaterialDownloadRepository()
    private lateinit var model: MaterialViewModel

    @Before
    fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    private fun TestScope.subscribe(id: String) {
        model = MaterialViewModel(id, "anna", repository, downloads)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.uiState.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.downloadState.collect {} }
    }

    private fun usePdf() {
        repository.lesson = repository.lesson.copy(type = MaterialType.PDF, contentUrl = "https://example.org/m.pdf")
    }

    @Test
    fun actionsDelegateAndStatesPropagate() = runTest {
        usePdf()
        subscribe("m")
        model.startDownload()
        model.cancelDownload()
        model.retryDownload()
        model.deleteDownload()
        runCurrent()
        assertEquals(listOf("enqueue:m", "cancel:m", "enqueue:m", "delete:m"), downloads.calls)
        for (state in listOf(DownloadState.Downloading(25), DownloadState.Downloaded("/m.pdf"), DownloadState.Failed("Brak miejsca"))) {
            downloads.state.value = state
            runCurrent()
            assertEquals(state, model.downloadState.value)
        }
    }

    @Test
    fun textHasNoDownloads() = runTest {
        downloads.state.value = DownloadState.Downloading(50)
        subscribe("m")
        model.startDownload()
        runCurrent()
        assertEquals(DownloadState.NotDownloaded, model.downloadState.value)
        assertTrue(downloads.calls.isEmpty())
    }

    @Test
    fun failedDownloadActionEmitsError() = runTest {
        usePdf()
        subscribe("m")
        downloads.fail = true
        model.startDownload()
        runCurrent()
        assertEquals(Unit, model.downloadErrors.first())
    }

    @Test
    fun loadingEndsWhenMaterialArrives() = runTest {
        subscribe("m")
        assertTrue(model.uiState.value.isLoading)
        runCurrent()
        assertEquals(MaterialUiState(repository.lesson, isLoading = false), model.uiState.value)
    }

    @Test
    fun toggleChangesCompletionForCurrentUser() = runTest {
        subscribe("m")
        runCurrent()
        model.toggleCompleted()
        runCurrent()
        assertTrue(model.uiState.value.material!!.isCompleted)
        assertEquals(setOf("anna" to "m"), repository.completed.value)
        model.toggleCompleted()
        runCurrent()
        assertFalse(model.uiState.value.material!!.isCompleted)
    }

    @Test
    fun failedToggleKeepsConfirmedStateAndEmitsError() = runTest {
        subscribe("m")
        runCurrent()
        val confirmed = model.uiState.value
        repository.failOnSave = true
        model.toggleCompleted()
        runCurrent()
        assertEquals(confirmed, model.uiState.value)
        assertEquals(Unit, model.saveErrors.first())
    }

    @Test
    fun missingMaterialEndsLoading() = runTest {
        subscribe("missing")
        runCurrent()
        assertEquals(MaterialUiState(isLoading = false, notFound = true), model.uiState.value)
    }
}

internal class FakeMaterialRepository : MaterialRepository {
    var failOnSave = false
    var lesson = Material("m", "1", "Lekcja", MaterialType.TEXT, false, false, "Treść lekcji")
    val completed = MutableStateFlow(emptySet<Pair<String, String>>())

    override fun observeMaterial(materialId: String, userId: String) = completed.map { ids ->
        lesson.takeIf { it.id == materialId }?.copy(isCompleted = (userId to materialId) in ids)
    }

    override fun observeAllMaterials(userId: String) = observeMaterials(lesson.courseId, userId)

    override fun observeMaterials(courseId: String, userId: String) =
        observeMaterial(lesson.id, userId).map { listOfNotNull(it).filter { row -> row.courseId == courseId } }

    override suspend fun setCompleted(userId: String, materialId: String, completed: Boolean) {
        check(!failOnSave)
        val key = userId to materialId
        if (completed) this.completed.value += key else this.completed.value -= key
    }
}

private class FakeMaterialDownloadRepository : MaterialDownloadRepository {
    val calls = mutableListOf<String>()
    val state = MutableStateFlow<DownloadState>(DownloadState.NotDownloaded)
    var fail = false

    override fun observeState(materialId: String) = state
    override suspend fun enqueue(materialId: String) {
        check(!fail)
        calls += "enqueue:$materialId"
    }
    override suspend fun cancel(materialId: String) { calls += "cancel:$materialId" }
    override suspend fun delete(materialId: String) { calls += "delete:$materialId" }
}

class DownloadFailureMessageTest {
    @Test
    fun mapsWorkerReasonsToPolishMessages() {
        assertEquals(R.string.too_large, downloadFailureMessage("Plik przekracza limit 100 MB"))
        assertEquals(R.string.no_space, downloadFailureMessage("Brak miejsca"))
        assertEquals(R.string.unsafe_url, downloadFailureMessage("Niebezpieczny adres"))
        assertEquals(R.string.network_failure, downloadFailureMessage("Wyczerpano próby pobierania"))
        assertEquals(R.string.http_failure, downloadFailureMessage("HTTP 404"))
        assertEquals(R.string.download_failure, downloadFailureMessage("Inny błąd"))
    }
}
