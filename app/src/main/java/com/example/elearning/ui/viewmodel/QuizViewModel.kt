package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.data.repository.QuizResultRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import com.example.elearning.data.repository.QuizResultSyncer
import com.example.elearning.domain.quiz.QuizOutcome
import com.example.elearning.domain.quiz.QuizQuestion
import com.example.elearning.domain.quiz.QuizScorer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SaveStatus { NOT_SAVED, SAVING, SAVED, FAILED }

data class QuizUiState(
    val quizTitle: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, Int?> = emptyMap(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val outcome: QuizOutcome? = null,
    val saveStatus: SaveStatus = SaveStatus.NOT_SAVED
) {
    val currentQuestion get() = questions.getOrNull(currentIndex)
    val isLastQuestion get() = currentIndex == questions.lastIndex
    val canGoNext get() = answers[currentQuestion?.id] != null
    val answeredCount get() = answers.values.count { it != null }
}

class QuizViewModel(
    private val quizId: String,
    repository: QuizRepository,
    private val results: QuizResultRepository,
    private val syncer: QuizResultSyncer,
    private val applicationScope: CoroutineScope,
    private val currentUserId: () -> String?
) : ViewModel() {
    private val state = MutableStateFlow(QuizUiState())
    val uiState = state.asStateFlow()
    private var threshold = 60
    private var ownerId = currentUserId()
    private var completedAt = 0L

    init {
        viewModelScope.launch {
            val quiz = repository.getQuiz(quizId)
            if (quiz == null) {
                state.value = QuizUiState(isLoading = false, notFound = true)
            } else {
                threshold = quiz.passThresholdPercent
                state.value = QuizUiState(quiz.title, repository.getQuestions(quizId), isLoading = false)
            }
        }
    }

    fun selectAnswer(optionIndex: Int) = state.update {
        val question = it.currentQuestion ?: return@update it
        if (it.outcome != null || optionIndex !in question.options.indices) it
        else it.copy(answers = it.answers + (question.id to optionIndex))
    }

    fun next() = state.update {
        if (it.outcome == null && it.canGoNext && !it.isLastQuestion) it.copy(currentIndex = it.currentIndex + 1) else it
    }

    fun previous() = state.update {
        if (it.outcome == null && it.currentIndex > 0) it.copy(currentIndex = it.currentIndex - 1) else it
    }

    fun finish() {
        val current = state.value
        if (current.outcome != null || current.questions.isEmpty() || current.answeredCount != current.questions.size) return

        val outcome = QuizScorer.score(current.questions, current.answers, threshold)
        completedAt = maxOf(System.currentTimeMillis(), completedAt + 1)
        save(current, outcome)
    }

    fun retrySave() {
        val current = state.value
        if (current.saveStatus != SaveStatus.FAILED) return
        save(current, current.outcome ?: return)
    }

    private fun save(current: QuizUiState, outcome: QuizOutcome) {
        val owner = ownerId
        if (owner.isNullOrBlank() || currentUserId() != owner) {
            state.compareAndSet(current, current.copy(outcome = outcome, saveStatus = SaveStatus.FAILED))
            return
        }
        val saving = current.copy(outcome = outcome, saveStatus = SaveStatus.SAVING)
        if (!state.compareAndSet(current, saving)) return

        val attemptTime = completedAt
        applicationScope.launch {
            if (currentUserId() != owner) {
                updateSaveStatus(outcome, SaveStatus.FAILED)
                return@launch
            }
            try {
                results.saveResult(owner, quizId, outcome, attemptTime)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateSaveStatus(outcome, SaveStatus.FAILED)
                return@launch
            }
            updateSaveStatus(outcome, SaveStatus.SAVED)
            syncer.syncAll(owner)
        }
    }

    private fun updateSaveStatus(outcome: QuizOutcome, status: SaveStatus) {
        // Zapis poprzedniej próby może zakończyć się już po restarcie.
        state.update { if (it.outcome === outcome) it.copy(saveStatus = status) else it }
    }

    fun restart() {
        ownerId = currentUserId()
        state.update { it.copy(currentIndex = 0, answers = emptyMap(), outcome = null, saveStatus = SaveStatus.NOT_SAVED) }
    }

    class Factory(
        private val quizId: String,
        private val repository: QuizRepository,
        private val results: QuizResultRepository,
        private val syncer: QuizResultSyncer,
        private val applicationScope: CoroutineScope,
        private val currentUserId: () -> String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(QuizViewModel::class.java))
            return modelClass.cast(QuizViewModel(quizId, repository, results, syncer, applicationScope, currentUserId))!!
        }
    }
}
