package com.example.elearning.data.local

import com.example.elearning.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizResultDaoTest : InMemoryDatabaseTest() {
    @Test
    fun resultsBelongToTheirUser() = runTest {
        val dao = db.quizResultDao()
        val result = QuizResultEntity(userId = "anna", quizId = "quiz", correctCount = 3,
            total = 5, percent = 60, passed = true, completedAt = 123L)
        val id = dao.insert(result)
        dao.insert(result.copy(userId = "jan"))
        assertEquals(listOf(result.copy(id = id)), dao.observeByUser("anna").first())
    }
}
