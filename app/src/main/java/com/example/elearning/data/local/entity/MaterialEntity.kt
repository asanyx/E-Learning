package com.example.elearning.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "materials",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("courseId")]
)
data class MaterialEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val title: String,
    val type: String,
    val contentUrl: String,
    val localPath: String? = null,
    val isDownloaded: Boolean = false
)
