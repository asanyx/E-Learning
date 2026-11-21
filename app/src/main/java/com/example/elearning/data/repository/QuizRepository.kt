package com.example.elearning.data.repository

import com.example.elearning.domain.model.Quiz
import com.example.elearning.domain.quiz.QuizQuestion
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    fun observeAllQuizzes(): Flow<List<Quiz>>
    suspend fun getQuiz(quizId: String): Quiz?
    suspend fun getQuestions(quizId: String): List<QuizQuestion>
    fun observeQuizzes(courseId: String): Flow<List<Quiz>>
}
