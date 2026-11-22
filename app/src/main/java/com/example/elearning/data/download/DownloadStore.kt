package com.example.elearning.data.download

import com.example.elearning.data.local.dao.MaterialDao
import kotlinx.coroutines.flow.first

interface DownloadStore {
    suspend fun downloadedPaths(): Map<String, String?>
    suspend fun markDownloaded(id: String, path: String)
    suspend fun clearDownload(id: String)
}

class RoomDownloadStore(private val dao: MaterialDao) : DownloadStore {
    override suspend fun downloadedPaths() = dao.observeAll().first()
        .filter { it.isDownloaded }.associate { it.id to it.localPath }

    override suspend fun markDownloaded(id: String, path: String) {
        check(dao.markDownloaded(id, path) == 1) { "Materiał nie istnieje" }
    }

    override suspend fun clearDownload(id: String) {
        dao.clearDownload(id)
    }
}
