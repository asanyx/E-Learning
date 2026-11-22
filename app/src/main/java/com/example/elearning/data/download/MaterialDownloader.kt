package com.example.elearning.data.download

import com.example.elearning.domain.download.DownloadPolicy
import com.example.elearning.domain.download.FetchResult
import com.example.elearning.domain.download.HttpFetcher
import com.example.elearning.domain.model.MaterialType
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed interface DownloadResult {
    data class Success(val path: String) : DownloadResult
    data object Retryable : DownloadResult
    data class Failed(val reason: String) : DownloadResult
}

class MaterialDownloader(
    private val fetcher: HttpFetcher,
    private val directory: File,
    private val store: DownloadStore,
    private val usableSpace: (File) -> Long = { it.usableSpace }
) {
    // Sprzątanie, usuwanie i pobieranie współdzielą katalog oraz budżet wolnego miejsca.
    private val lock = Mutex()
    private class NoSpace : Exception()

    suspend fun download(
        materialId: String, url: String, type: MaterialType, onProgress: (Int?) -> Unit = {}
    ): DownloadResult = lock.withLock {
        try {
            val part = File(directory, DownloadPolicy.partName(materialId))
            try {
                if (!DownloadPolicy.isDownloadable(type, url)) return@withLock DownloadResult.Failed("Niedozwolony materiał lub adres")
                Files.createDirectories(directory.toPath())
                val target = File(directory, DownloadPolicy.fileName(materialId, type))
                if (target.isFile && store.downloadedPaths()[materialId] == target.absolutePath) {
                    return@withLock DownloadResult.Success(target.absolutePath)
                }
                val budget = usableSpace(directory) - DownloadPolicy.reserveBytes
                if (budget < 0) throw NoSpace()
                val result = part.outputStream().use { sink ->
                    fetcher.fetch(url, sink) { bytes, total ->
                        if ((total ?: DownloadPolicy.maxBytes) > budget || bytes > budget) throw NoSpace()
                        onProgress(total?.takeIf { it > 0 }?.let { (bytes * 100 / it).coerceAtMost(100).toInt() })
                    }
                }
                currentCoroutineContext().ensureActive()
                when (result) {
                    FetchResult.Success -> withContext(NonCancellable) {
                        Files.move(part.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
                        try {
                            store.markDownloaded(materialId, target.absolutePath)
                        } catch (error: Exception) {
                            Files.delete(target.toPath())
                            throw error
                        }
                        DownloadResult.Success(target.absolutePath)
                    }
                    is FetchResult.HttpServerError, is FetchResult.IoError -> DownloadResult.Retryable
                    is FetchResult.HttpClientError -> DownloadResult.Failed("HTTP ${result.code}")
                    FetchResult.UnsafeUrl -> DownloadResult.Failed("Niebezpieczny adres")
                    FetchResult.TooLarge -> DownloadResult.Failed("Plik przekracza limit 100 MB")
                }
            } finally {
                Files.deleteIfExists(part.toPath())
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: IOException) {
            DownloadResult.Retryable
        } catch (_: NoSpace) {
            DownloadResult.Failed("Brak miejsca")
        } catch (error: Exception) {
            DownloadResult.Failed(error.message ?: "Błąd zapisu materiału")
        }
    }

    suspend fun removePart(id: String) = lock.withLock {
        Files.deleteIfExists(File(directory, DownloadPolicy.partName(id)).toPath())
    }

    suspend fun delete(id: String) = lock.withLock {
        withContext(NonCancellable) {
            for (type in listOf(MaterialType.PDF, MaterialType.VIDEO)) {
                Files.deleteIfExists(File(directory, DownloadPolicy.fileName(id, type)).toPath())
            }
            Files.deleteIfExists(File(directory, DownloadPolicy.partName(id)).toPath())
            store.clearDownload(id)
        }
    }

    suspend fun verifyDownloads() = lock.withLock {
        for ((id, path) in store.downloadedPaths()) {
            if (path == null || !File(path).isFile) store.clearDownload(id)
        }
    }

    suspend fun cleanupOrphans(knownPaths: Set<String>? = null) = lock.withLock {
        val paths = knownPaths ?: store.downloadedPaths().values.filterNotNull().toSet()
        if (!directory.exists()) return@withLock
        val files = directory.listFiles() ?: throw IOException("Nie można odczytać katalogu materiałów")
        for (file in files) {
            if (file.isFile && file.absolutePath !in paths) Files.delete(file.toPath())
        }
    }
}
