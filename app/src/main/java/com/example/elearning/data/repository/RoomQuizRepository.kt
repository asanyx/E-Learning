package com.example.elearning.data.repository

import com.example.elearning.data.local.dao.QuizDao
import com.example.elearning.domain.model.Quiz
import com.example.elearning.data.local.entity.QuizEntity
import com.example.elearning.domain.quiz.QuizQuestion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomQuizRepository(private val dao: QuizDao) : QuizRepository {
    private fun QuizEntity.toQuiz() = Quiz(id, courseId, title, passThresholdPercent)

    override suspend fun getQuiz(quizId: String) = dao.getQuiz(quizId).first()?.toQuiz()

    override suspend fun getQuestions(quizId: String) = dao.getQuestions(quizId).map {
        QuizQuestion(it.id, it.text, it.options, it.correctIndex)
    }

    override fun observeQuizzes(courseId: String) = dao.observeByCourse(courseId).map { rows ->
        rows.map { it.toQuiz() }
    }
}
