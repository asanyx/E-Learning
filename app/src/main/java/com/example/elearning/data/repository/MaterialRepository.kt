package com.example.elearning.data.repository

import com.example.elearning.domain.model.Material
import kotlinx.coroutines.flow.Flow

interface MaterialRepository {
    fun observeMaterials(courseId: String, userId: String): Flow<List<Material>>
    suspend fun setCompleted(userId: String, materialId: String, completed: Boolean)
}
