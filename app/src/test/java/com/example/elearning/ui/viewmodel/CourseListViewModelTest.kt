package com.example.elearning.ui.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeCourseRepository()
    private lateinit var model: CourseListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        model = CourseListViewModel(repository)
    }

    @After
    fun cleanup() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun categoryFiltersCoursesAndAllClearsFilter() = runTest {
        model.selectCategory("Mobilne")
        runCurrent()
        assertEquals(listOf(repository.android), model.uiState.value.courses)
        model.selectCategory(null)
        runCurrent()
        assertEquals(repository.courses, model.uiState.value.courses)
    }

    @Test
    fun searchWaitsForLastQueryAndKeepsAllCategories() = runTest {
        runCurrent()
        model.setQuery("k")
        advanceTimeBy(200)
        model.setQuery("kot")
        advanceTimeBy(299)
        assertEquals(listOf(""), repository.queries)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf("", "kot"), repository.queries)
        assertEquals(listOf(repository.kotlin), model.uiState.value.courses)
        assertEquals(listOf("Języki", "Mobilne"), model.uiState.value.categories)
        model.selectCategory("Mobilne")
        runCurrent()
        assertTrue(model.uiState.value.courses.isEmpty())
    }

    @Test
    fun missingQueryReturnsEmptyList() = runTest {
        model.setQuery("SQL")
        advanceTimeBy(300)
        runCurrent()
        assertFalse(model.uiState.value.isLoading)
        assertTrue(model.uiState.value.courses.isEmpty())
    }
}
