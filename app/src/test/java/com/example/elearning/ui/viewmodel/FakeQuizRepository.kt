package com.example.elearning.ui.viewmodel

import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.domain.model.Quiz
import com.example.elearning.domain.quiz.QuizQuestion
import kotlinx.coroutines.flow.flowOf

internal class FakeQuizRepository : QuizRepository {
    val quiz = Quiz("q", "1", "Quiz", 50)
    var questions = listOf(
        QuizQuestion("a", "Pierwsze?", listOf("A", "B"), 0),
        QuizQuestion("b", "Drugie?", listOf("A", "B"), 1)
    )

    override fun observeQuizzes(courseId: String) = flowOf(listOf(quiz).filter { it.courseId == courseId })
    override suspend fun getQuiz(quizId: String) = quiz.takeIf { it.id == quizId }
    override suspend fun getQuestions(quizId: String) = if (quizId == quiz.id) questions else emptyList()
}
