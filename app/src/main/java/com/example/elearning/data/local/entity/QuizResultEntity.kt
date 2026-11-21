package com.example.elearning.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_results"
)
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val quizId: String,
    val correctCount: Int,
    val total: Int,
    val percent: Int,
    val passed: Boolean,
    val completedAt: Long
)
