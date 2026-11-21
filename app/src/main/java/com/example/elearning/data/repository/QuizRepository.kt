package com.example.elearning.data.repository

import com.example.elearning.domain.model.Quiz
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    fun observeQuizzes(courseId: String): Flow<List<Quiz>>
}
