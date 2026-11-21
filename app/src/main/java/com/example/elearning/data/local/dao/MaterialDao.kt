package com.example.elearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elearning.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials WHERE courseId = :courseId")
    fun observeByCourse(courseId: String): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id")
    fun observeById(id: String): Flow<MaterialEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MaterialEntity>): List<Long>

    @Query("UPDATE materials SET localPath = :localPath, isDownloaded = 1 WHERE id = :id")
    suspend fun markDownloaded(id: String, localPath: String): Int

    @Query("UPDATE materials SET localPath = NULL, isDownloaded = 0 WHERE id = :id")
    suspend fun clearDownload(id: String): Int
}
