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
    private var uid: String? = "anna"
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
        model = QuizViewModel("q", repository, results, syncer, backgroundScope) { uid }
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
        model = QuizViewModel("q", repository, results, syncer, backgroundScope) { uid }
        runCurrent()
        model.selectAnswer(0)
        model.finish()
        assertNull(model.uiState.value.outcome)
        model.retrySave()
        runCurrent()
        assertEquals(0, results.saveCalls)
        model.next()
        model.selectAnswer(0)
        remote.onUpload = { assertEquals(SaveStatus.SAVED, model.uiState.value.saveStatus) }
        model.finish()
        val outcome = requireNotNull(model.uiState.value.outcome)
        assertEquals(SaveStatus.SAVING, model.uiState.value.saveStatus)
        model.retrySave()
        assertEquals(50, outcome.percent)
        assertTrue(outcome.passed)
        model.selectAnswer(1)
        model.finish()
        assertSame(outcome, model.uiState.value.outcome)
        runCurrent()
        assertEquals(1, results.saveCalls)
        assertEquals(results.observeResults("anna").first(), remote.uploads.single().second)
        val saved = results.observeResultsForQuiz("anna", "q").first().single()
        assertTrue(saved.completedAt > 0)
        assertEquals(SaveStatus.SAVED, model.uiState.value.saveStatus)
        model.retrySave()
        runCurrent()
        assertEquals(1, results.saveCalls)
        assertEquals(QuizResult(1, "anna", "q", 1, 2, 50, true, saved.completedAt), saved)
    }

    @Test
    fun restartReturnsToFirstQuestionAndAllowsNewAttempt() = runTest {
        model = QuizViewModel("q", repository, results, syncer, backgroundScope) { uid }
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
        model.next()
        model.selectAnswer(0)
        model.finish()
        runCurrent()
        assertEquals(2, results.saveCalls)
        assertTrue(results.attemptedTimes[1] > results.attemptedTimes[0])
        assertEquals(listOf(0, 100), results.observeResults("anna").first().map { it.percent })
    }

    @Test
    fun failedSaveKeepsOutcomeAndDoesNotRetryOnRepeatedFinish() = runTest {
        results.failOnSave = true
        completeQuiz("anna")
        runCurrent()
        assertEquals(100, model.uiState.value.outcome?.percent)
        assertEquals(SaveStatus.FAILED, model.uiState.value.saveStatus)
        assertTrue(remote.uploads.isEmpty())
        model.finish()
        runCurrent()
        assertEquals(1, results.saveCalls)

        results.failOnSave = false
        uid = "jan"
        model.retrySave()
        runCurrent()
        assertEquals(SaveStatus.FAILED, model.uiState.value.saveStatus)
        assertEquals(1, results.saveCalls)
        uid = "anna"
        model.retrySave()
        model.retrySave()
        runCurrent()
        assertEquals(SaveStatus.SAVED, model.uiState.value.saveStatus)
        assertEquals(2, results.saveCalls)
        assertEquals(results.attemptedTimes.first(), results.attemptedTimes.last())
        val saved = results.observeResults("anna").first().single()
        assertEquals(saved, remote.uploads.single().second.single())
    }

    @Test
    fun anonymousFinishShowsOutcomeWithoutSaving() = runTest {
        completeQuiz(null)
        runCurrent()
        assertEquals(SaveStatus.FAILED, model.uiState.value.saveStatus)
        assertEquals(100, model.uiState.value.outcome?.percent)
        assertEquals(0, results.saveCalls)
    }

    @Test
    fun sessionChangeBeforeFinishCannotTransferAttempt() = runTest {
        completeQuiz("jan")
        runCurrent()
        assertEquals(SaveStatus.FAILED, model.uiState.value.saveStatus)
        assertEquals(0, results.saveCalls)
        assertTrue(remote.uploads.isEmpty())
    }

    private fun TestScope.completeQuiz(userId: String?) {
        model = QuizViewModel("q", repository, results, syncer, backgroundScope) { uid }
        runCurrent()
        model.selectAnswer(0)
        model.next()
        model.selectAnswer(1)
        uid = userId
        model.finish()
    }

    @Test
    fun emptyQuizCannotFinish() = runTest {
        repository.questions = emptyList()
        model = QuizViewModel("q", repository, results, syncer, backgroundScope) { uid }
        runCurrent()
        model.finish()
        assertFalse(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
        assertNull(model.uiState.value.currentQuestion)
        assertNull(model.uiState.value.outcome)
    }

    @Test
    fun missingQuizEndsLoading() = runTest {
        model = QuizViewModel("missing", repository, results, syncer, backgroundScope) { uid }
        runCurrent()
        assertTrue(model.uiState.value.notFound)
        assertFalse(model.uiState.value.isLoading)
    }
}
