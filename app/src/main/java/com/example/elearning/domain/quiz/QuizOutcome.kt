package com.example.elearning.domain.quiz

data class QuestionOutcome(
    val questionId: String,
    val selectedIndex: Int?,
    val correctIndex: Int,
    val isCorrect: Boolean
)

data class QuizOutcome(
    val correctCount: Int,
    val total: Int,
    val percent: Int,
    val passed: Boolean,
    val perQuestion: List<QuestionOutcome>
)
