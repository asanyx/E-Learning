package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    private val repository = FakeQuizRepository()
    private lateinit var model: QuizViewModel

    @Before
    fun setup() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun navigationPreservesAnswersAndRequiresValidSelection() = runTest {
        model = QuizViewModel("q", repository)
        assertTrue(model.uiState.value.isLoading)
        runCurrent()
        assertEquals(repository.questions, model.uiState.value.questions)
        assertEquals(repository.quiz.title, model.uiState.value.quizTitle)
        assertFalse(model.uiState.value.isLoading)
        model.selectAnswer(2)
        model.next()
        assertEquals(0, model.uiState.value.currentIndex)
        assertFalse(model.uiState.value.canGoNext)
        model.selectAnswer(1)
        assertTrue(model.uiState.value.canGoNext)
        model.next()
        assertTrue(model.uiState.value.isLastQuestion)
        assertFalse(model.uiState.value.canGoNext)
        model.previous()
        assertEquals(repository.questions.first(), model.uiState.value.currentQuestion)
        assertEquals(1, model.uiState.value.answers["a"])
        model.selectAnswer(0)
        assertEquals(1, model.uiState.value.answeredCount)
    }

    @Test
    fun finishRequiresAllAnswersAndScoresOnceWithQuizThreshold() = runTest {
        model = QuizViewModel("q", repository)
        runCurrent()
        model.selectAnswer(0)
        model.finish()
        assertNull(model.uiState.value.outcome)
        model.next()
        model.selectAnswer(0)
        model.finish()
        val outcome = requireNotNull(model.uiState.value.outcome)
        assertEquals(50, outcome.percent)
        assertTrue(outcome.passed)
        model.selectAnswer(1)
        model.finish()
        assertSame(outcome, model.uiState.value.outcome)
    }

    @Test
    fun restartReturnsToFirstQuestionAndAllowsNewAttempt() = runTest {
        model = QuizViewModel("q", repository)
        runCurrent()
        val initial = model.uiState.value
        model.selectAnswer(0)
        model.next()
        model.selectAnswer(1)
        model.finish()
        assertNotNull(model.uiState.value.outcome)

        model.restart()
        assertEquals(initial, model.uiState.value)
        model.selectAnswer(1)
        assertTrue(model.uiState.value.canGoNext)
    }

    @Test
    fun emptyQuizCannotFinish() = runTest {
        repository.questions = emptyList()
        model = QuizViewModel("q", repository)
        runCurrent()
        model.finish()
        assertFalse(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
        assertNull(model.uiState.value.currentQuestion)
        assertNull(model.uiState.value.outcome)
    }

    @Test
    fun missingQuizEndsLoading() = runTest {
        model = QuizViewModel("missing", repository)
        runCurrent()
        assertTrue(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
    }
}
