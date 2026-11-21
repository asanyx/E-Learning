package com.example.elearning.data.local

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizDaoTest : InMemoryDatabaseTest() {
    @Test
    fun storesQuizAndPreservesOptionOrder() = runTest {
        val dao = db.quizDao()
        db.courseDao().insertAll(SeedData.courses)
        dao.insertQuizzes(SeedData.quizzes)
        dao.insertQuestions(SeedData.questions)
        val quiz = SeedData.quizzes.single()
        assertEquals(quiz, dao.getQuiz(quiz.id).first())
        assertEquals(SeedData.questions.toSet(), dao.getQuestions(quiz.id).toSet())
    }
}
