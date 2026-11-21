package com.example.elearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.elearning.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {
    @Insert
    suspend fun insert(result: QuizResultEntity): Long

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC, id DESC")
    fun observeByUser(userId: String): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE userId = :userId AND quizId = :quizId ORDER BY completedAt DESC, id DESC")
    fun observeByQuiz(userId: String, quizId: String): Flow<List<QuizResultEntity>>
}
