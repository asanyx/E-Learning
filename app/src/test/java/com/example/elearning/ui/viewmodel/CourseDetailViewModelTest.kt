package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.elearning.data.local.dao.CompletedMaterialDao
import com.example.elearning.data.local.dao.MaterialDao
import com.example.elearning.data.local.entity.CompletedMaterialEntity
import com.example.elearning.data.local.entity.MaterialEntity
import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.data.repository.RoomMaterialRepository
import com.example.elearning.domain.model.MaterialType
import com.example.elearning.domain.model.Quiz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailViewModelTest {
    private val courses = FakeCourseRepository()
    private val completions = FakeCompletions()
    private val quizzes = object : QuizRepository {
        override fun observeQuizzes(courseId: String) = flowOf(listOf(Quiz("q", courseId, "Quiz", 60)))
    }
    private lateinit var model: CourseDetailViewModel

    @Before
    fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    private fun open(id: String) {
        model = CourseDetailViewModel(id, "anna", courses, RoomMaterialRepository(FakeMaterials(), completions), quizzes)
    }

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun progressUsesOnlyCurrentCourseAndUser() = runTest {
        completions.markCompleted(CompletedMaterialEntity("anna", "a", 1))
        completions.markCompleted(CompletedMaterialEntity("anna", "outside", 1))
        completions.markCompleted(CompletedMaterialEntity("jan", "b", 1))
        open("1")
        assertTrue(model.uiState.value.isLoading)
        runCurrent()
        val state = model.uiState.value
        assertEquals(courses.kotlin, state.course)
        assertEquals(listOf(true, false), state.materials.map { it.isCompleted })
        assertEquals(MaterialType.TEXT, state.materials.first().type)
        assertEquals(1, state.completedCount)
        assertEquals(2, state.totalCount)
        assertEquals("q", state.quizzes.single().id)
    }

    @Test
    fun toggleAddsAndRemovesCompletion() = runTest {
        open("1")
        runCurrent()
        model.toggleCompleted("a")
        runCurrent()
        assertEquals(1, model.uiState.value.completedCount)
        model.toggleCompleted("a")
        runCurrent()
        assertEquals(0, model.uiState.value.completedCount)
    }

    @Test
    fun missingCourseEndsLoading() = runTest {
        open("missing")
        runCurrent()
        assertTrue(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
    }
}

private class FakeMaterials : MaterialDao {
    private val rows = listOf(
        MaterialEntity("a", "1", "Lekcja", "UNKNOWN", ""),
        MaterialEntity("b", "1", "Dokument", "PDF", ""),
        MaterialEntity("outside", "2", "Film", "VIDEO", "")
    )
    override fun observeByCourse(courseId: String) = flowOf(rows.filter { it.courseId == courseId })
    override fun observeById(id: String) = flowOf(rows.find { it.id == id })
    override suspend fun insertAll(list: List<MaterialEntity>): List<Long> = error("unused")
    override suspend fun markDownloaded(id: String, localPath: String): Int = error("unused")
    override suspend fun clearDownload(id: String): Int = error("unused")
}

private class FakeCompletions : CompletedMaterialDao {
    private val keys = MutableStateFlow(emptySet<Pair<String, String>>())
    override fun observeCompletedIds(userId: String) = keys.map { entries ->
        entries.filter { it.first == userId }.map { it.second }
    }
    override suspend fun markCompleted(entity: CompletedMaterialEntity): Long {
        keys.value += entity.userId to entity.materialId
        return 1
    }
    override suspend fun unmark(userId: String, materialId: String): Int {
        keys.value -= userId to materialId
        return 1
    }
}
