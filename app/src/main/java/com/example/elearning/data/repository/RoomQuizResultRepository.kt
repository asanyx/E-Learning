package com.example.elearning.data.repository

import com.example.elearning.data.local.dao.QuizResultDao
import com.example.elearning.data.local.entity.QuizResultEntity
import com.example.elearning.domain.model.QuizResult
import com.example.elearning.domain.quiz.QuizOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map

class RoomQuizResultRepository(
    private val dao: QuizResultDao,
    private val applicationScope: CoroutineScope
) : QuizResultRepository {
    override suspend fun saveResult(userId: String, quizId: String, outcome: QuizOutcome, completedAt: Long) {
        val result = QuizResult(0L, userId, quizId, outcome.correctCount, outcome.total,
            outcome.percent, outcome.passed, completedAt)
        // Zapis może się dokończyć po zamknięciu ekranu wyniku.
        applicationScope.async { dao.insert(result.toEntity()) }.await()
    }

    override fun observeResults(userId: String) =
        dao.observeByUser(userId).map { rows -> rows.map { it.toDomain() } }

    override fun observeResultsForQuiz(userId: String, quizId: String) =
        dao.observeByQuiz(userId, quizId).map { rows -> rows.map { it.toDomain() } }

    private fun QuizResult.toEntity() =
        QuizResultEntity(id, userId, quizId, correctCount, total, percent, passed, completedAt)

    private fun QuizResultEntity.toDomain() =
        QuizResult(id, userId, quizId, correctCount, total, percent, passed, completedAt)
}
