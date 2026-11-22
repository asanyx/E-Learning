package com.example.elearning

import com.example.elearning.data.network.ConnectivityNetworkMonitor
import android.app.Application
import android.util.Log
import androidx.work.WorkManager
import com.example.elearning.data.download.HttpUrlConnectionFetcher
import com.example.elearning.data.download.MaterialDownloadRepository
import com.example.elearning.data.download.MaterialDownloader
import com.example.elearning.data.download.RoomDownloadStore
import com.example.elearning.data.download.WorkManagerMaterialDownloadRepository
import java.io.File
import kotlinx.coroutines.CancellationException
import com.example.elearning.data.remote.FirestoreResultsDataSource
import com.example.elearning.data.repository.QuizResultSyncer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val networkMonitor = ConnectivityNetworkMonitor(application, applicationScope)
    val database = AppDatabase.getInstance(application)
    val materialDownloader = MaterialDownloader(
        HttpUrlConnectionFetcher(), File(application.filesDir, "materials"), RoomDownloadStore(database.materialDao())
    )
    val materialDownloadRepository: MaterialDownloadRepository by lazy {
        WorkManagerMaterialDownloadRepository(WorkManager.getInstance(application), database.materialDao(), materialDownloader)
    }
    val courseRepository: CourseRepository = RoomCourseRepository(database.courseDao())
    val materialRepository: MaterialRepository = RoomMaterialRepository(database.materialDao(), database.completedMaterialDao())
    val quizRepository: QuizRepository = RoomQuizRepository(database.quizDao())
    val quizResultRepository: QuizResultRepository = RoomQuizResultRepository(
        database.quizResultDao(), applicationScope
    )
    private val remoteResults = FirestoreResultsDataSource()
    val quizResultSyncer = QuizResultSyncer(quizResultRepository, remoteResults)
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
}

class ElearningApp : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        container.applicationScope.launch {
            container.authRepository.currentUser.map { it?.uid }.distinctUntilChanged().collectLatest { uid ->
                if (uid != null) container.quizResultSyncer.syncAll(uid)
            }
        }
        container.applicationScope.launch {
            seedIfEmpty(container.database)
            try {
                container.materialDownloader.verifyDownloads()
                container.materialDownloader.cleanupOrphans()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w("ElearningApp", "Błąd porządkowania pobranych materiałów", error)
            }
        }
    }
}
