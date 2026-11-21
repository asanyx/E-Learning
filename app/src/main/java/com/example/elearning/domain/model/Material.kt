package com.example.elearning.domain.model

enum class MaterialType { TEXT, PDF, VIDEO }

data class Material(
    val id: String,
    val courseId: String,
    val title: String,
    val type: MaterialType,
    val isDownloaded: Boolean,
    val isCompleted: Boolean
)
