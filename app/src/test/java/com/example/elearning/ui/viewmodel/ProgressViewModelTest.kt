package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.elearning.domain.quiz.QuizOutcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModelTest {
    private val results = FakeQuizResultRepository()
    private val materials = FakeMaterialRepository()
    private lateinit var model: ProgressViewModel
    private val passed = QuizOutcome(4, 5, 80, true, emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        model = ProgressViewModel("anna", FakeCourseRepository(), materials, FakeQuizRepository(), results)
    }

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun progressTracksCurrentUsersMaterialsAndQuizAttemptsPerCourse() = runTest {
        backgroundScope.launch { model.uiState.collect {} }
        results.saveResult("jan", "q", passed, 10)
        materials.setCompleted("jan", "m", true)
        runCurrent()
        assertTrue(model.uiState.value.history.isEmpty())
        assertEquals(listOf(0, 0), model.uiState.value.courses.map { it.progress.percent })

        materials.setCompleted("anna", "m", true)
        runCurrent()
        assertEquals(50, model.uiState.value.courses.first().progress.percent)
        results.saveResult("anna", "q", passed, 20)
        runCurrent()
        val state = model.uiState.value
        val course = state.courses.single { it.courseId == "1" }
        assertTrue(course.progress.isCompleted)
        assertEquals(100, course.progress.percent)
        assertEquals(mapOf("q" to 80), course.progress.bestPercentByQuiz)
        assertEquals(0, state.courses.single { it.courseId == "2" }.progress.percent)
        assertEquals(ResultItem("Quiz", "Kotlin", 80, true, 4, 5, 20), state.history.single())
    }

    @Test
    fun historyOrdersByCompletionTimeAndIncludesMissingQuizzes() = runTest {
        backgroundScope.launch { model.uiState.collect {} }
        results.saveResult("anna", "missing", passed, 30)
        results.saveResult("anna", "q", passed, 10)
        runCurrent()
        val history = model.uiState.value.history
        assertEquals(listOf(30L, 10L), history.map { it.completedAt })
        assertNull(history.first().quizTitle)
        assertNull(history.first().courseTitle)
        assertEquals("Kotlin", history.last().courseTitle)
    }
}
