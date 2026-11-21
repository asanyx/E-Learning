package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.domain.model.Material
import com.example.elearning.domain.model.MaterialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MaterialViewModelTest {
    private val repository = FakeMaterialRepository()
    private lateinit var model: MaterialViewModel

    @Before
    fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun loadingEndsWhenMaterialArrives() = runTest {
        model = MaterialViewModel("m", "anna", repository)
        assertTrue(model.uiState.value.isLoading)
        runCurrent()
        assertEquals(MaterialUiState(repository.lesson, isLoading = false), model.uiState.value)
    }

    @Test
    fun toggleChangesCompletionForCurrentUser() = runTest {
        model = MaterialViewModel("m", "anna", repository)
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
    fun missingMaterialEndsLoading() = runTest {
        model = MaterialViewModel("missing", "anna", repository)
        runCurrent()
        assertEquals(MaterialUiState(isLoading = false, notFound = true), model.uiState.value)
    }
}

internal class FakeMaterialRepository : MaterialRepository {
    val lesson = Material("m", "1", "Lekcja", MaterialType.TEXT, false, false, "Treść lekcji")
    val completed = MutableStateFlow(emptySet<Pair<String, String>>())

    override fun observeMaterial(materialId: String, userId: String) = completed.map { ids ->
        lesson.takeIf { it.id == materialId }?.copy(isCompleted = (userId to materialId) in ids)
    }

    override fun observeAllMaterials(userId: String) = observeMaterials(lesson.courseId, userId)

    override fun observeMaterials(courseId: String, userId: String) =
        observeMaterial(lesson.id, userId).map { listOfNotNull(it).filter { row -> row.courseId == courseId } }

    override suspend fun setCompleted(userId: String, materialId: String, completed: Boolean) {
        val key = userId to materialId
        if (completed) this.completed.value += key else this.completed.value -= key
    }
}
