package com.example.elearning.data.repository

import com.example.elearning.data.remote.FakeRemoteResultsDataSource
import com.example.elearning.data.remote.resultDocumentId
import com.example.elearning.domain.quiz.QuizOutcome
import com.example.elearning.ui.viewmodel.FakeQuizResultRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizResultSyncerTest {
    private val local = FakeQuizResultRepository()
    private val remote = FakeRemoteResultsDataSource()
    private var warnings = 0
    private val syncer = QuizResultSyncer(local, remote) { warnings++ }
    private val outcome = QuizOutcome(3, 5, 60, true, emptyList())

    @Test
    fun uploadsAllAndOnlyRequestedUsersResults() = runTest {
        local.saveResult("anna", "q1", outcome, 10)
        local.saveResult("jan", "q2", outcome, 20)
        local.saveResult("anna", "q3", outcome, 30)
        syncer.syncAll("anna")
        assertEquals(listOf("anna" to local.observeResults("anna").first()), remote.uploads)
        syncer.syncAll("empty")
        assertEquals(1, remote.uploads.size)
    }

    @Test
    fun failedUploadIsLoggedAndCanBeRetried() = runTest {
        local.saveResult("anna", "q", outcome)
        remote.onUpload = { error("offline") }
        syncer.syncAll("anna")
        assertEquals(1, warnings)
        remote.onUpload = {}
        syncer.syncAll("anna")
        assertEquals(2, remote.uploads.size)
    }

    @Test
    fun cancellationPropagates() = runTest {
        local.saveResult("anna", "q", outcome)
        remote.onUpload = { throw CancellationException() }
        val job = launch { syncer.syncAll("anna") }
        job.join()
        assertTrue(job.isCancelled)
        assertEquals(0, warnings)
    }

    @Test
    fun overlappingRequestsShareUploadForIdenticalData() = runTest {
        local.saveResult("anna", "q", outcome)
        val release = CompletableDeferred<Unit>()
        remote.onUpload = { release.await() }
        repeat(2) { launch { syncer.syncAll("anna") } }
        runCurrent()
        release.complete(Unit)
        runCurrent()
        assertEquals(1, remote.uploads.size)
    }

    @Test
    fun queuedRequestIncludesResultSavedDuringUpload() = runTest {
        local.saveResult("anna", "q", outcome, 10)
        val release = CompletableDeferred<Unit>()
        remote.onUpload = { release.await() }
        launch { syncer.syncAll("anna") }
        runCurrent()
        local.saveResult("anna", "q", outcome, 20)
        launch { syncer.syncAll("anna") }
        runCurrent()
        release.complete(Unit)
        runCurrent()
        assertEquals(listOf(1, 2), remote.uploads.map { it.second.size })
    }

    @Test
    fun documentIdUsesQuizAndCompletionTime() {
        assertEquals("quiz_1_12345", resultDocumentId("quiz_1", 12345))
    }
}
