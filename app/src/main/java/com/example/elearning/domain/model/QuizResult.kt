package com.example.elearning.domain.model

data class QuizResult(
    val id: Long,
    val userId: String,
    val quizId: String,
    val correctCount: Int,
    val total: Int,
    val percent: Int,
    val passed: Boolean,
    val completedAt: Long
)
