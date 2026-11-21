package com.example.elearning.data.repository

import android.util.Log
import com.example.elearning.data.remote.RemoteResultsDataSource
import com.example.elearning.domain.model.QuizResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuizResultSyncer(
    private val results: QuizResultRepository,
    private val remote: RemoteResultsDataSource,
    private val warn: () -> Unit = { Log.w("QuizResultSyncer", "Nie udało się wysłać wyników quizów.") }
) {
    private class ActiveSync {
        val mutex = Mutex()
        var callers = 0
        var uploaded: List<QuizResult>? = null
    }

    private val active = mutableMapOf<String, ActiveSync>()

    suspend fun syncAll(userId: String) {
        val sync = synchronized(active) {
            active.getOrPut(userId) { ActiveSync() }.also { it.callers++ }
        }
        try {
            sync.mutex.withLock {
                val snapshot = results.observeResults(userId).first()
                // Oczekujące wywołanie może już widzieć kolejny zapis w Room.
                if (snapshot.isNotEmpty() && snapshot != sync.uploaded) {
                    remote.upload(userId, snapshot)
                    sync.uploaded = snapshot
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            warn()
        } finally {
            synchronized(active) {
                sync.callers--
                if (sync.callers == 0) active.remove(userId)
            }
        }
    }
}
