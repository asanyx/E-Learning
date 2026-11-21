package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.CourseRepository
import com.example.elearning.domain.model.Course
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CourseListUiState(
    val courses: List<Course> = emptyList(),
    val categories: List<String> = emptyList(),
    val query: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class CourseListViewModel(repository: CourseRepository) : ViewModel() {
    private val state = MutableStateFlow(CourseListUiState())
    val uiState = state.asStateFlow()

    init {
        val all = repository.observeCourses("").shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)
        val matches = state.map { it.query.trim() }.distinctUntilChanged().flatMapLatest { query ->
            if (query.isEmpty()) all else flow {
                delay(300)
                emitAll(repository.observeCourses(query))
            }
        }
        viewModelScope.launch {
            combine(all, matches, state.map { it.selectedCategory }.distinctUntilChanged()) { full, found, category ->
                val visible = if (category == null) found else found.filter { it.category == category }
                visible to full.map { it.category }.distinct()
            }.collect { (courses, categories) ->
                state.update { it.copy(courses = courses, categories = categories, isLoading = false) }
            }
        }
    }

    fun setQuery(text: String) { state.update { it.copy(query = text) } }

    fun selectCategory(category: String?) { state.update { it.copy(selectedCategory = category) } }

    class Factory(private val repository: CourseRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CourseListViewModel::class.java))
            return modelClass.cast(CourseListViewModel(repository))!!
        }
    }
}
