package com.example.elearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elearning.data.local.entity.CompletedMaterialEntity
import com.example.elearning.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedMaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markCompleted(entity: CompletedMaterialEntity): Long

    @Query("DELETE FROM completed_materials WHERE userId = :userId AND materialId = :materialId")
    suspend fun unmark(userId: String, materialId: String): Int

    @Query("SELECT materialId FROM completed_materials WHERE userId = :userId")
    fun observeCompletedIds(userId: String): Flow<List<String>>
}
