package com.example.elearning.data.repository

import com.example.elearning.domain.model.QuizResult
import com.example.elearning.domain.quiz.QuizOutcome
import kotlinx.coroutines.flow.Flow

interface QuizResultRepository {
    suspend fun saveResult(
        userId: String,
        quizId: String,
        outcome: QuizOutcome,
        completedAt: Long = System.currentTimeMillis()
    )

    fun observeResults(userId: String): Flow<List<QuizResult>>
    fun observeResultsForQuiz(userId: String, quizId: String): Flow<List<QuizResult>>
}
