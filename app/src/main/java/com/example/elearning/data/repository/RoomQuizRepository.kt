package com.example.elearning.data.repository

import com.example.elearning.data.local.dao.QuizDao
import com.example.elearning.domain.model.Quiz
import kotlinx.coroutines.flow.map

class RoomQuizRepository(private val dao: QuizDao) : QuizRepository {
    override fun observeQuizzes(courseId: String) = dao.observeByCourse(courseId).map { rows ->
        rows.map { Quiz(it.id, it.courseId, it.title, it.passThresholdPercent) }
    }
}
