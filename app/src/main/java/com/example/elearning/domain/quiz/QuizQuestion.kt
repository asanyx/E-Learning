package com.example.elearning.domain.quiz

data class QuizQuestion(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int
)
