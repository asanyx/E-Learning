package com.example.elearning

import android.app.Application
import com.example.elearning.data.remote.FirebaseAuthRepository
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.data.repository.QuizRepository
import com.example.elearning.data.repository.RoomMaterialRepository
import com.example.elearning.data.repository.RoomQuizRepository
import com.example.elearning.data.repository.AuthRepository
import com.example.elearning.data.repository.CourseRepository
import com.example.elearning.data.repository.RoomCourseRepository
import com.example.elearning.data.local.AppDatabase
import com.example.elearning.data.local.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.elearning.data.repository.QuizResultRepository
import com.example.elearning.data.repository.RoomQuizResultRepository
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(application: Application) {
    val database = AppDatabase.getInstance(application)
    val courseRepository: CourseRepository = RoomCourseRepository(database.courseDao())
    val materialRepository: MaterialRepository = RoomMaterialRepository(database.materialDao(), database.completedMaterialDao())
    val quizRepository: QuizRepository = RoomQuizRepository(database.quizDao())
    val quizResultRepository: QuizResultRepository = RoomQuizResultRepository(
        database.quizResultDao(), CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
}

class ElearningApp : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            seedIfEmpty(container.database)
        }
    }
}
