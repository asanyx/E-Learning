package com.example.elearning.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseSeederTest : InMemoryDatabaseTest() {
    @Test
    fun secondSeedPreservesExistingData() = runTest {
        seedIfEmpty(db)
        val material = SeedData.materials.first()
        db.materialDao().markDownloaded(material.id, "/offline/tekst")
        seedIfEmpty(db)

        assertEquals(2, db.courseDao().count().first())
        for (course in SeedData.courses) {
            assertEquals(SeedData.materials.count { it.courseId == course.id },
                db.materialDao().observeByCourse(course.id).first().size)
            assertEquals(SeedData.quizzes.filter { it.courseId == course.id },
                db.quizDao().observeByCourse(course.id).first())
        }
        assertEquals(SeedData.questions.toSet(), db.quizDao().getQuestions(SeedData.quizzes.single().id).toSet())
        assertEquals("/offline/tekst", db.materialDao().observeById(material.id).first()?.localPath)
    }
}
