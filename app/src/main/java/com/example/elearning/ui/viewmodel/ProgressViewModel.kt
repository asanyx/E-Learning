package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.CourseRepository
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.data.repository.QuizResultRepository
import com.example.elearning.domain.progress.CourseProgress
import com.example.elearning.domain.progress.ProgressCalculator
import com.example.elearning.domain.progress.QuizAttempt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CourseProgressItem(
    val courseId: String,
    val title: String,
    val progress: CourseProgress,
    val quizTitles: Map<String, String> = emptyMap()
)

data class ResultItem(
    val quizTitle: String?,
    val courseTitle: String?,
    val percent: Int,
    val passed: Boolean,
    val correctCount: Int,
    val total: Int,
    val completedAt: Long
)

data class ProgressUiState(
    val courses: List<CourseProgressItem> = emptyList(),
    val history: List<ResultItem> = emptyList(),
    val isLoading: Boolean = true
)

class ProgressViewModel(
    userId: String,
    courses: CourseRepository,
    materials: MaterialRepository,
    quizzes: QuizRepository,
    results: QuizResultRepository
) : ViewModel() {
    val uiState = combine(
        courses.observeCourses(""), materials.observeAllMaterials(userId),
        quizzes.observeAllQuizzes(), results.observeResults(userId)
    ) { courseList, materialList, quizList, resultList ->
        val materialsByCourse = materialList.groupBy { it.courseId }
        val quizzesByCourse = quizList.groupBy { it.courseId }
        val quizzesById = quizList.associateBy { it.id }
        val coursesById = courseList.associateBy { it.id }
        val attemptsByQuiz = resultList.groupBy({ it.quizId }, { QuizAttempt(it.quizId, it.percent, it.passed) })
        val items = courseList.map { course ->
            val lessons = materialsByCourse[course.id].orEmpty()
            val titles = quizzesByCourse[course.id].orEmpty().associate { it.id to it.title }
            val progress = ProgressCalculator.calculate(
                materialIds = lessons.map { it.id },
                completedMaterialIds = lessons.filter { it.isCompleted }.map { it.id }.toSet(),
                quizIds = titles.keys.toList(),
                attempts = titles.keys.flatMap { attemptsByQuiz[it].orEmpty() }
            )
            CourseProgressItem(course.id, course.title, progress, titles)
        }
        val history = resultList.sortedByDescending { it.completedAt }.map { result ->
            val quiz = quizzesById[result.quizId]
            ResultItem(quiz?.title, coursesById[quiz?.courseId]?.title, result.percent, result.passed,
                result.correctCount, result.total, result.completedAt)
        }
        ProgressUiState(items, history, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressUiState())

    class Factory(
        private val userId: String,
        private val courses: CourseRepository,
        private val materials: MaterialRepository,
        private val quizzes: QuizRepository,
        private val results: QuizResultRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProgressViewModel::class.java))
            return modelClass.cast(ProgressViewModel(userId, courses, materials, quizzes, results))!!
        }
    }
}
