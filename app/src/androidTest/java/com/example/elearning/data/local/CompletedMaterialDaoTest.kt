package com.example.elearning.data.local

import com.example.elearning.data.local.entity.CompletedMaterialEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CompletedMaterialDaoTest : InMemoryDatabaseTest() {
    @Test
    fun completionUsesCompositeKey() = runTest {
        val dao = db.completedMaterialDao()
        val entry = CompletedMaterialEntity("anna", "tekst", 1L)
        dao.markCompleted(entry)
        dao.markCompleted(entry.copy(completedAt = 2L))
        dao.markCompleted(entry.copy(userId = "jan"))
        assertEquals(listOf("tekst"), dao.observeCompletedIds("anna").first())
        dao.unmark("anna", "tekst")
        assertEquals(emptyList<String>(), dao.observeCompletedIds("anna").first())
        assertEquals(listOf("tekst"), dao.observeCompletedIds("jan").first())
    }
}
