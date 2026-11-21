package com.example.elearning

import android.app.Application
import com.example.elearning.data.remote.FirebaseAuthRepository
import com.example.elearning.data.repository.AuthRepository
import com.example.elearning.data.repository.CourseRepository
import com.example.elearning.data.repository.RoomCourseRepository
import com.example.elearning.data.local.AppDatabase
import com.example.elearning.data.local.seedIfEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(application: Application) {
    val database = AppDatabase.getInstance(application)
    val courseRepository: CourseRepository = RoomCourseRepository(database.courseDao())
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
