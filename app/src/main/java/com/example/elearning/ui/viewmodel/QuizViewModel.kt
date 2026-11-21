package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.domain.quiz.QuizOutcome
import com.example.elearning.domain.quiz.QuizQuestion
import com.example.elearning.domain.quiz.QuizScorer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val quizTitle: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val answers: Map<String, Int?> = emptyMap(),
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val outcome: QuizOutcome? = null
) {
    val currentQuestion get() = questions.getOrNull(currentIndex)
    val isLastQuestion get() = currentIndex == questions.lastIndex
    val canGoNext get() = answers[currentQuestion?.id] != null
    val answeredCount get() = answers.values.count { it != null }
}

class QuizViewModel(quizId: String, repository: QuizRepository) : ViewModel() {
    private val state = MutableStateFlow(QuizUiState())
    val uiState = state.asStateFlow()
    private var threshold = 60

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

    fun finish() = state.update {
        if (it.outcome != null || it.questions.isEmpty() || it.answeredCount != it.questions.size) it
        else it.copy(outcome = QuizScorer.score(it.questions, it.answers, threshold))
    }

    fun restart() = state.update { it.copy(currentIndex = 0, answers = emptyMap(), outcome = null) }

    class Factory(private val quizId: String, private val repository: QuizRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(QuizViewModel::class.java))
            return modelClass.cast(QuizViewModel(quizId, repository))!!
        }
    }
}
