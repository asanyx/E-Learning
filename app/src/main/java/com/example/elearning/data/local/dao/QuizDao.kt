package com.example.elearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.elearning.data.local.entity.QuestionEntity
import com.example.elearning.data.local.entity.QuizEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    fun observeByCourse(courseId: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    fun getQuiz(id: String): Flow<QuizEntity?>

    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestions(quizId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(list: List<QuizEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(list: List<QuestionEntity>): List<Long>
}
