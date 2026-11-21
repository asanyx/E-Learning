package com.example.elearning.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "completed_materials",
    primaryKeys = ["userId", "materialId"]
)
data class CompletedMaterialEntity(
    val userId: String,
    val materialId: String,
    val completedAt: Long
)
