package com.example.elearning.data.download

import com.example.elearning.domain.download.DownloadPolicy
import com.example.elearning.domain.download.FetchResult
import com.example.elearning.domain.download.HttpFetcher
import com.example.elearning.domain.model.MaterialType
import com.example.elearning.worker.DownloadDecision
import com.example.elearning.worker.downloadDecision
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MaterialDownloaderTest {
    @get:Rule val folder = TemporaryFolder()
    private val url = "https://example.org/material"
    private val records = mutableMapOf<String, String?>()
    private var markCalls = 0
    private var failMark = false
    private val store = object : DownloadStore {
        override suspend fun downloadedPaths() = records.toMap()

        override suspend fun markDownloaded(id: String, path: String) {
            markCalls++
            assertEquals("pdf", File(path).readText())
            assertFalse(File(folder.root, "$id.part").exists())
            if (failMark) error("Room")
            records[id] = path
        }

        override suspend fun clearDownload(id: String) {
            records.remove(id)
        }
    }
    private val success = HttpFetcher { _, sink, progress ->
        progress(0, 3)
        sink.write("pdf".toByteArray())
        progress(3, 3)
        FetchResult.Success
    }

    private fun downloader(fetcher: HttpFetcher = success, space: Long = Long.MAX_VALUE) =
        MaterialDownloader(fetcher, folder.root, store) { space }

    private suspend fun MaterialDownloader.start() = download("abc", url, MaterialType.PDF)

    private fun assertClean() {
        assertEquals(emptyList<String>(), folder.root.list()!!.toList())
        assertEquals(0, markCalls)
        assertTrue(records.isEmpty())
    }

    @Test fun successPublishesCompleteFileBeforeRoom() = runTest {
        val path = File(folder.root, "abc.pdf").absolutePath
        val progress = mutableListOf<Int?>()
        assertEquals(DownloadResult.Success(path), downloader().download("abc", url, MaterialType.PDF, progress::add))
        assertEquals(listOf(0, 100), progress)
        assertEquals(listOf("abc.pdf"), folder.root.list()!!.toList())
        assertEquals(mapOf("abc" to path), records)
    }

    @Test fun markFailureRollsBackFile() = runTest {
        failMark = true
        assertTrue(downloader().start() is DownloadResult.Failed)
        assertEquals(1, markCalls)
        assertTrue(folder.root.list()!!.isEmpty())
    }

    @Test fun networkErrorsDiscardPartialFile() = runTest {
        val errors = listOf(
            FetchResult.HttpClientError(404) to DownloadResult.Failed("HTTP 404"),
            FetchResult.HttpServerError(503) to DownloadResult.Retryable,
            FetchResult.IoError(IOException()) to DownloadResult.Retryable
        )
        for ((error, expected) in errors) {
            val fetcher = HttpFetcher { _, sink, _ ->
                sink.write(1)
                error
            }
            assertEquals(expected, downloader(fetcher).start())
            assertClean()
        }
    }

    @Test fun lowSpaceSkipsFetch() = runTest {
        val fetcher = HttpFetcher { _, _, _ -> error("Nie powinien pobierać") }
        assertEquals(DownloadResult.Failed("Brak miejsca"), downloader(fetcher, 1024).start())
        assertClean()
    }

    @Test fun knownLengthChecksSpaceBeforeReadingBody() = runTest {
        val connection = StubConnection(200, length = 3)
        val fetcher = HttpUrlConnectionFetcher { connection }
        assertEquals(DownloadResult.Failed("Brak miejsca"), downloader(fetcher, DownloadPolicy.reserveBytes + 2).start())
        assertFalse(connection.bodyOpened)
        assertClean()
    }

    @Test fun exactSpaceBudgetIsEnough() = runTest {
        val path = File(folder.root, "abc.pdf").absolutePath
        assertEquals(DownloadResult.Success(path), downloader(space = DownloadPolicy.reserveBytes + 3).start())
    }

    @Test fun understatedLengthCannotUseReservedSpace() = runTest {
        val connection = StubConnection(200, length = 1)
        val result = downloader(HttpUrlConnectionFetcher { connection }, DownloadPolicy.reserveBytes + 2).start()
        assertEquals(DownloadResult.Failed("Brak miejsca"), result)
        assertClean()
    }

    @Test fun oversizedHeaderSkipsBody() = runTest {
        val connection = StubConnection(200, length = DownloadPolicy.maxBytes + 1)
        assertEquals(DownloadResult.Failed("Plik przekracza limit 100 MB"),
            downloader(HttpUrlConnectionFetcher { connection }).start())
        assertFalse(connection.bodyOpened)
        assertClean()
    }

    @Test fun oversizedStreamWithMissingOrFalseLengthIsStopped() = runTest {
        for (length in listOf(-1L, 1L)) {
            val connection = StubConnection(200, length)
            val result = downloader(HttpUrlConnectionFetcher { connection }).start()
            assertEquals(DownloadResult.Failed("Plik przekracza limit 100 MB"), result)
            assertTrue(connection.bodyOpened)
            assertClean()
        }
    }

    @Test fun httpAndDowngradeRedirectAreRejectedBeforeConnectingToHttp() = runTest {
        var connections = 0
        val downloader = downloader(HttpUrlConnectionFetcher {
            assertEquals("https", it.protocol)
            connections++
            StubConnection(302, location = "http://example.org/file")
        })
        assertTrue(downloader.download("abc", "http://example.org/file", MaterialType.PDF) is DownloadResult.Failed)
        assertEquals(0, connections)
        assertTrue(downloader.start() is DownloadResult.Failed)
        assertEquals(1, connections)
        assertClean()
    }

    @Test fun cancellationRemovesPartialFile() = runTest {
        val writing = CompletableDeferred<Unit>()
        val downloader = downloader(HttpFetcher { _, sink, _ ->
            sink.write(1)
            writing.complete(Unit)
            awaitCancellation()
        })
        val job = launch {
            downloader.start()
            fail("Anulowanie ma przerwać korutynę")
        }
        writing.await()
        assertTrue(File(folder.root, "abc.part").isFile)
        job.cancelAndJoin()
        assertClean()
    }

    @Test fun policyAndNames() {
        assertFalse(DownloadPolicy.isDownloadable(MaterialType.TEXT, url))
        assertTrue(DownloadPolicy.isDownloadable(MaterialType.PDF, url))
        assertFalse(DownloadPolicy.isDownloadable(MaterialType.PDF, "http://example.org/pdf"))
        assertFalse(DownloadPolicy.isDownloadable(MaterialType.VIDEO, "https:///file"))
        assertEquals("A-b_1.mp4", DownloadPolicy.fileName("A-b_1", MaterialType.VIDEO))
        for (id in listOf("../x", "a/b", "")) {
            assertThrows(IllegalArgumentException::class.java) { DownloadPolicy.fileName(id, MaterialType.PDF) }
            assertThrows(IllegalArgumentException::class.java) { DownloadPolicy.partName(id) }
        }
    }

    @Test fun cleanupPreservesKnownFiles() = runTest {
        val keep = folder.newFile("abc.pdf")
        folder.newFile("obsolete.pdf")
        folder.newFile("unused.part")
        downloader().cleanupOrphans(setOf(keep.absolutePath))
        assertEquals(listOf(keep.name), folder.root.list()!!.toList())
    }

    @Test fun verifyClearsMissingFilesAndNullPaths() = runTest {
        val keep = folder.newFile("abc.pdf")
        records.putAll(mapOf("abc" to keep.absolutePath, "missing" to File(folder.root, "missing.pdf").path, "empty" to null))
        downloader().verifyDownloads()
        assertEquals(mapOf("abc" to keep.absolutePath), records)
    }

    @Test fun deleteRemovesFileAndClearsRoom() = runTest {
        val downloader = downloader()
        downloader.start()
        downloader.delete("abc")
        assertTrue(records.isEmpty())
        assertTrue(folder.root.list()!!.isEmpty())
    }

    @Test fun workerDecisionRespectsRetryLimit() {
        for (attempt in 0..3) {
            assertEquals(DownloadDecision.Success, downloadDecision(DownloadResult.Success("x"), attempt))
            assertEquals(DownloadDecision.Failure("HTTP 404"), downloadDecision(DownloadResult.Failed("HTTP 404"), attempt))
            val decision = downloadDecision(DownloadResult.Retryable, attempt)
            if (attempt < 3) assertEquals(DownloadDecision.Retry, decision)
            else assertTrue(decision is DownloadDecision.Failure)
        }
    }

    private class StubConnection(
        private val status: Int, private val length: Long = -1, private val location: String? = null
    ) : HttpURLConnection(URL("https://example.org/material")) {
        var bodyOpened = false
        override fun connect() {}
        override fun disconnect() {}
        override fun usingProxy() = false
        override fun getResponseCode() = status
        override fun getContentLengthLong() = length
        override fun getHeaderField(name: String) = if (name == "Location") location else null
        override fun getInputStream(): InputStream {
            bodyOpened = true
            return object : InputStream() {
                var remaining = DownloadPolicy.maxBytes + 1
                override fun read(): Int = error("Oczekiwano odczytu blokowego")
                override fun read(buffer: ByteArray, off: Int, len: Int): Int {
                    if (remaining == 0L) return -1
                    val count = minOf(remaining, len.toLong()).toInt()
                    remaining -= count
                    return count
                }
            }
        }
    }
}
