package com.example.elearning.ui.viewmodel

import com.example.elearning.data.repository.CourseRepository
import com.example.elearning.domain.model.Course
import kotlinx.coroutines.flow.flowOf

internal class FakeCourseRepository : CourseRepository {
    val kotlin = Course("1", "Kotlin", "", "Języki")
    val android = Course("2", "Android", "", "Mobilne")
    val courses = listOf(kotlin, android)
    val queries = mutableListOf<String>()

    override fun observeCourses(query: String) = flowOf(courses.filter {
        it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
    }).also { queries.add(query) }

    override fun observeCourse(id: String) = flowOf(courses.find { it.id == id })
}
