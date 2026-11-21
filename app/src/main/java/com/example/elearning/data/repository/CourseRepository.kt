package com.example.elearning.data.repository

import com.example.elearning.domain.model.Course
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeCourses(query: String): Flow<List<Course>>
    fun observeCourse(id: String): Flow<Course?>
}
