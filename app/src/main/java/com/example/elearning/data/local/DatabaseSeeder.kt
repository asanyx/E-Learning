package com.example.elearning.data.local

import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

suspend fun seedIfEmpty(db: AppDatabase) {
    db.withTransaction {
        if (db.courseDao().count().first() == 0) {
            db.courseDao().insertAll(SeedData.courses)
            db.materialDao().insertAll(SeedData.materials)
            db.quizDao().insertQuizzes(SeedData.quizzes)
            db.quizDao().insertQuestions(SeedData.questions)
        }
    }
}
