package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.elearning.data.remote.FakeRemoteResultsDataSource
import com.example.elearning.data.repository.QuizResultSyncer
import com.example.elearning.domain.model.QuizResult
import kotlinx.coroutines.flow.first
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
    private val results = FakeQuizResultRepository()
    private val remote = FakeRemoteResultsDataSource()
    private val syncer = QuizResultSyncer(results, remote)
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
        model = QuizViewModel("q", repository, results, syncer, backgroundScope)
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
        model = QuizViewModel("q", repository, results, syncer, backgroundScope)
        runCurrent()
        model.selectAnswer(0)
        model.finish("anna")
        assertNull(model.uiState.value.outcome)
        runCurrent()
        assertEquals(0, results.saveCalls)
        model.next()
        model.selectAnswer(0)
        val startedAt = System.currentTimeMillis()
        model.finish("anna")
        val outcome = requireNotNull(model.uiState.value.outcome)
        assertEquals(50, outcome.percent)
        assertTrue(outcome.passed)
        model.selectAnswer(1)
        model.finish("anna")
        assertSame(outcome, model.uiState.value.outcome)
        runCurrent()
        assertEquals(1, results.saveCalls)
        assertEquals(results.observeResults("anna").first(), remote.uploads.single().second)
        val saved = results.observeResultsForQuiz("anna", "q").first().single()
        assertTrue(saved.completedAt in startedAt..System.currentTimeMillis())
        assertEquals(QuizResult(1, "anna", "q", 1, 2, 50, true, saved.completedAt), saved)
    }

    @Test
    fun restartReturnsToFirstQuestionAndAllowsNewAttempt() = runTest {
        model = QuizViewModel("q", repository, results, syncer, backgroundScope)
        runCurrent()
        val initial = model.uiState.value
        model.selectAnswer(0)
        model.next()
        model.selectAnswer(1)
        model.finish("anna")
        assertNotNull(model.uiState.value.outcome)

        model.restart()
        assertEquals(initial, model.uiState.value)
        model.selectAnswer(1)
        assertTrue(model.uiState.value.canGoNext)
        model.next()
        model.selectAnswer(0)
        model.finish("anna")
        runCurrent()
        assertEquals(2, results.saveCalls)
        assertEquals(listOf(0, 100), results.observeResults("anna").first().map { it.percent })
    }

    @Test
    fun failedSaveKeepsOutcomeAndDoesNotRetryOnRepeatedFinish() = runTest {
        results.failOnSave = true
        completeQuiz("anna")
        runCurrent()
        assertEquals(100, model.uiState.value.outcome?.percent)
        model.finish("anna")
        runCurrent()
        assertEquals(1, results.saveCalls)
    }

    @Test
    fun anonymousFinishShowsOutcomeWithoutSaving() = runTest {
        completeQuiz(null)
        runCurrent()
        assertEquals(100, model.uiState.value.outcome?.percent)
        assertEquals(0, results.saveCalls)
    }

    private fun TestScope.completeQuiz(userId: String?) {
        model = QuizViewModel("q", repository, results, syncer, backgroundScope)
        runCurrent()
        model.selectAnswer(0)
        model.next()
        model.selectAnswer(1)
        model.finish(userId)
    }

    @Test
    fun emptyQuizCannotFinish() = runTest {
        repository.questions = emptyList()
        model = QuizViewModel("q", repository, results, syncer, backgroundScope)
        runCurrent()
        model.finish("anna")
        assertFalse(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
        assertNull(model.uiState.value.currentQuestion)
        assertNull(model.uiState.value.outcome)
    }

    @Test
    fun missingQuizEndsLoading() = runTest {
        model = QuizViewModel("missing", repository, results, syncer, backgroundScope)
        runCurrent()
        assertTrue(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
    }
}
