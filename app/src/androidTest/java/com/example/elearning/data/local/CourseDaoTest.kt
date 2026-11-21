package com.example.elearning.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseDaoTest : InMemoryDatabaseTest() {
    @Test
    fun insertSearchCountAndReplace() = runTest {
        val dao = db.courseDao()
        val courses = SeedData.courses
        dao.insertAll(courses)
        assertEquals(courses.toSet(), dao.observeAll().first().toSet())
        assertEquals(2, dao.count().first())
        assertEquals(listOf(courses[0]), dao.search("Kotl").first())
        assertEquals(listOf(courses[1]), dao.search("mobil").first())

        val updated = courses[0].copy(title = "Nowy tytuł")
        dao.insertAll(listOf(updated))
        assertEquals(2, dao.count().first())
        assertEquals(updated, dao.observeById(updated.id).first())
    }
}
