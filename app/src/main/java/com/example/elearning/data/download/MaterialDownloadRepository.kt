package com.example.elearning.data.download

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.example.elearning.data.local.dao.MaterialDao
import com.example.elearning.domain.download.DownloadPolicy
import com.example.elearning.worker.DownloadWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface DownloadState {
    data object NotDownloaded : DownloadState
    data object Queued : DownloadState
    data class Downloading(val percent: Int?) : DownloadState
    data class Downloaded(val path: String) : DownloadState
    data class Failed(val reason: String) : DownloadState
}

interface MaterialDownloadRepository {
    suspend fun enqueue(materialId: String)
    suspend fun cancel(materialId: String)
    suspend fun delete(materialId: String)
    fun observeState(materialId: String): Flow<DownloadState>
}

class WorkManagerMaterialDownloadRepository(
    private val workManager: WorkManager,
    private val dao: MaterialDao,
    private val downloader: MaterialDownloader
) : MaterialDownloadRepository {
    private val commands = Mutex()
    private fun name(id: String) = "download-${DownloadPolicy.checkedId(id)}"

    override suspend fun enqueue(materialId: String) {
        commands.withLock {
            val request = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInputData(workDataOf(DownloadWorker.ID to materialId))
                .build()
            workManager.enqueueUniqueWork(name(materialId), ExistingWorkPolicy.KEEP, request).await()
        }
    }

    override suspend fun cancel(materialId: String) {
        commands.withLock {
            workManager.cancelUniqueWork(name(materialId)).await()
            withContext(Dispatchers.IO) { downloader.removePart(materialId) }
        }
    }

    override suspend fun delete(materialId: String) {
        commands.withLock {
            workManager.cancelUniqueWork(name(materialId)).await()
            withContext(Dispatchers.IO) { downloader.delete(materialId) }
        }
    }

    override fun observeState(materialId: String): Flow<DownloadState> = combine(
        dao.observeById(materialId), workManager.getWorkInfosForUniqueWorkFlow(name(materialId))
    ) { row, infos ->
        val work = infos.firstOrNull { !it.state.isFinished } ?: infos.firstOrNull()
        when {
            row?.isDownloaded == true && row.localPath != null -> DownloadState.Downloaded(row.localPath)
            work?.state == WorkInfo.State.RUNNING ->
                DownloadState.Downloading(work.progress.getInt(DownloadWorker.PERCENT, -1).takeIf { it >= 0 })
            work?.state == WorkInfo.State.ENQUEUED || work?.state == WorkInfo.State.BLOCKED -> DownloadState.Queued
            work?.state == WorkInfo.State.FAILED ->
                DownloadState.Failed(work.outputData.getString(DownloadWorker.REASON) ?: "Pobieranie nie powiodło się")
            else -> DownloadState.NotDownloaded
        }
    }.distinctUntilChanged()
}
