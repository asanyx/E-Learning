package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.CourseRepository
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.domain.model.Course
import com.example.elearning.domain.model.Material
import com.example.elearning.domain.model.Quiz
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CourseDetailUiState(
    val course: Course? = null,
    val materials: List<Material> = emptyList(),
    val quizzes: List<Quiz> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

class CourseDetailViewModel(
    courseId: String,
    private val userId: String,
    courses: CourseRepository,
    private val materialRepository: MaterialRepository,
    quizzes: QuizRepository
) : ViewModel() {
    private val materials = materialRepository.observeMaterials(courseId, userId)
    private val errors = Channel<Unit>(Channel.BUFFERED)
    val saveErrors = errors.receiveAsFlow()
    private val completionLock = Mutex()
    val uiState = combine(courses.observeCourse(courseId), materials, quizzes.observeQuizzes(courseId)) { course, rows, tests ->
        CourseDetailUiState(course, rows, tests, rows.count { it.isCompleted }, rows.size,
            isLoading = false, notFound = course == null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CourseDetailUiState())

    fun toggleCompleted(materialId: String) {
        viewModelScope.launch {
            try {
                completionLock.withLock {
                    val material = materials.first().find { it.id == materialId } ?: return@withLock
                    materialRepository.setCompleted(userId, materialId, !material.isCompleted)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                errors.send(Unit)
            }
        }
    }

    class Factory(
        private val courseId: String,
        private val userId: String,
        private val courses: CourseRepository,
        private val materials: MaterialRepository,
        private val quizzes: QuizRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CourseDetailViewModel::class.java))
            return modelClass.cast(CourseDetailViewModel(courseId, userId, courses, materials, quizzes))!!
        }
    }
}
