package com.example.elearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elearning.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    fun observeById(id: String): Flow<CourseEntity?>

    @Query("""
        SELECT * FROM courses
        WHERE title LIKE '%' || REPLACE(REPLACE(REPLACE(:query, '!', '!!'), '%', '!%'), '_', '!_') || '%' ESCAPE '!'
           OR category LIKE '%' || REPLACE(REPLACE(REPLACE(:query, '!', '!!'), '%', '!%'), '_', '!_') || '%' ESCAPE '!'
    """)
    fun search(query: String): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CourseEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM courses")
    fun count(): Flow<Int>
}
