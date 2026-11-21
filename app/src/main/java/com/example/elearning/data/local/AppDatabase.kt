package com.example.elearning.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.elearning.data.local.dao.CourseDao
import com.example.elearning.data.local.dao.MaterialDao
import com.example.elearning.data.local.dao.QuizDao
import com.example.elearning.data.local.dao.QuizResultDao
import com.example.elearning.data.local.dao.CompletedMaterialDao
import com.example.elearning.data.local.entity.CourseEntity
import com.example.elearning.data.local.entity.MaterialEntity
import com.example.elearning.data.local.entity.QuizEntity
import com.example.elearning.data.local.entity.QuestionEntity
import com.example.elearning.data.local.entity.QuizResultEntity
import com.example.elearning.data.local.entity.CompletedMaterialEntity

@Database(
    entities = [
        CourseEntity::class,
        MaterialEntity::class,
        QuizEntity::class,
        QuestionEntity::class,
        QuizResultEntity::class,
        CompletedMaterialEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun materialDao(): MaterialDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun completedMaterialDao(): CompletedMaterialDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elearning.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
