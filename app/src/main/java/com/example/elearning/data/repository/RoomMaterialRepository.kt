package com.example.elearning.data.repository

import com.example.elearning.data.local.dao.CompletedMaterialDao
import com.example.elearning.data.local.dao.MaterialDao
import com.example.elearning.data.local.entity.CompletedMaterialEntity
import com.example.elearning.data.local.entity.MaterialEntity
import com.example.elearning.domain.model.Material
import com.example.elearning.domain.model.MaterialType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomMaterialRepository(
    private val materials: MaterialDao,
    private val completions: CompletedMaterialDao
) : MaterialRepository {
    override fun observeAllMaterials(userId: String) = withCompletions(materials.observeAll(), userId)

    override fun observeMaterials(courseId: String, userId: String) =
        withCompletions(materials.observeByCourse(courseId), userId)

    private fun withCompletions(source: Flow<List<MaterialEntity>>, userId: String) =
        combine(source, completions.observeCompletedIds(userId)) { rows, ids ->
            val completed = ids.toSet()
            rows.map { it.toMaterial(it.id in completed) }
        }

    override fun observeMaterial(materialId: String, userId: String) =
        combine(materials.observeById(materialId), completions.observeCompletedIds(userId)) { row, ids ->
            row?.toMaterial(materialId in ids)
        }

    override suspend fun setCompleted(userId: String, materialId: String, completed: Boolean) {
        if (completed) {
            completions.markCompleted(CompletedMaterialEntity(userId, materialId, System.currentTimeMillis()))
        } else {
            completions.unmark(userId, materialId)
        }
    }

    private fun MaterialEntity.toMaterial(completed: Boolean) = Material(
        id, courseId, title, MaterialType.entries.find { it.name == type } ?: MaterialType.TEXT,
        isDownloaded, completed, contentUrl, localPath
    )
}
