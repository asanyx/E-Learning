package com.example.elearning.ui.viewmodel

import com.example.elearning.data.repository.QuizResultRepository
import com.example.elearning.domain.model.QuizResult
import com.example.elearning.domain.quiz.QuizOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeQuizResultRepository : QuizResultRepository {
    private val results = MutableStateFlow(emptyList<QuizResult>())
    var saveCalls = 0
    var failOnSave = false

    override suspend fun saveResult(userId: String, quizId: String, outcome: QuizOutcome, completedAt: Long) {
        saveCalls++
        check(!failOnSave) { "Błąd zapisu" }
        results.value = listOf(QuizResult(saveCalls.toLong(), userId, quizId, outcome.correctCount,
            outcome.total, outcome.percent, outcome.passed, completedAt)) + results.value
    }

    override fun observeResults(userId: String) =
        results.map { rows -> rows.filter { it.userId == userId } }

    override fun observeResultsForQuiz(userId: String, quizId: String) =
        observeResults(userId).map { rows -> rows.filter { it.quizId == quizId } }
}
