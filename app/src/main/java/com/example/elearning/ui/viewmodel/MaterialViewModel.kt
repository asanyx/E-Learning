package com.example.elearning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.domain.model.Material
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class MaterialUiState(
    val material: Material? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

class MaterialViewModel(
    materialId: String,
    private val userId: String,
    private val repository: MaterialRepository
) : ViewModel() {
    private val material = repository.observeMaterial(materialId, userId)
    private val completionLock = Mutex()
    val uiState = material.map {
        MaterialUiState(material = it, isLoading = false, notFound = it == null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MaterialUiState())

    fun toggleCompleted() {
        viewModelScope.launch {
            completionLock.withLock {
                val current = material.first() ?: return@withLock
                repository.setCompleted(userId, current.id, !current.isCompleted)
            }
        }
    }

    class Factory(
        private val materialId: String,
        private val userId: String,
        private val repository: MaterialRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MaterialViewModel::class.java))
            return modelClass.cast(MaterialViewModel(materialId, userId, repository))!!
        }
    }
}
