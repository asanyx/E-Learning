package com.example.elearning.domain.progress

data class CourseProgress(
    val completedMaterials: Int,
    val totalMaterials: Int,
    val passedQuizzes: Int,
    val totalQuizzes: Int,
    val percent: Int,
    val isCompleted: Boolean,
    val bestPercentByQuiz: Map<String, Int>
)
