package com.example.elearning.data.repository

import com.example.elearning.data.local.dao.CourseDao
import com.example.elearning.data.local.entity.CourseEntity
import com.example.elearning.domain.model.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCourseRepository(private val dao: CourseDao) : CourseRepository {
    override fun observeCourses(query: String): Flow<List<Course>> {
        val rows = if (query.isBlank()) dao.observeAll() else dao.search(query.trim())
        return rows.map { courses -> courses.map { it.toCourse() } }
    }

    override fun observeCourse(id: String): Flow<Course?> =
        dao.observeById(id).map { it?.toCourse() }

    private fun CourseEntity.toCourse() = Course(id, title, description, category)
}
