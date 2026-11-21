package com.example.elearning.domain.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {
    @Test
    fun emptyCourseHasZeroProgressAndIsNotCompleted() {
        val result = ProgressCalculator.calculate(emptyList(), emptySet(), emptyList(), emptyList())
        assertEquals(CourseProgress(0, 0, 0, 0, 0, false, emptyMap()), result)
    }

    @Test
    fun unstartedCourseHasNoBestScores() {
        val result = ProgressCalculator.calculate(listOf("m1"), emptySet(), listOf("q1"), emptyList())
        assertEquals(CourseProgress(0, 1, 0, 1, 0, false, emptyMap()), result)
    }

    @Test
    fun materialsOnlyCountUniqueCourseMaterialsAndRoundDown() {
        val result = ProgressCalculator.calculate(
            listOf("m1", "m1", "m2", "m3"), setOf("m1", "outside"), emptyList(), emptyList()
        )
        assertEquals(CourseProgress(1, 3, 0, 0, 33, false, emptyMap()), result)
    }

    @Test
    fun passingOnRetryCompletesQuizAndLaterLowerScoreDoesNotUndoIt() {
        val attempts = listOf(QuizAttempt("q1", 40, false), QuizAttempt("q1", 80, true))
        val expected = CourseProgress(0, 0, 1, 1, 100, true, mapOf("q1" to 80))
        assertEquals(expected, ProgressCalculator.calculate(emptyList(), emptySet(), listOf("q1"), attempts))
        val laterAttempts = attempts + QuizAttempt("q1", 20, false)
        assertEquals(expected, ProgressCalculator.calculate(emptyList(), emptySet(), listOf("q1"), laterAttempts))
    }

    @Test
    fun failedAttemptsHaveBestScoreButDoNotContributeToCompletion() {
        val result = ProgressCalculator.calculate(
            materialIds = listOf("m1", "m2"),
            completedMaterialIds = setOf("m2"),
            quizIds = listOf("q1", "q2"),
            attempts = listOf(
                QuizAttempt("q1", 70, true), QuizAttempt("q2", 80, false),
                QuizAttempt("q2", 50, false), QuizAttempt("outside", 100, true)
            )
        )
        assertEquals(CourseProgress(1, 2, 1, 2, 50, false, mapOf("q1" to 70, "q2" to 80)), result)
    }

    @Test
    fun completingAllUniqueItemsCompletesMixedCourse() {
        val result = ProgressCalculator.calculate(
            listOf("m1"), setOf("m1"), listOf("q1", "q1"), listOf(QuizAttempt("q1", 60, true))
        )
        assertEquals(CourseProgress(1, 1, 1, 1, 100, true, mapOf("q1" to 60)), result)
    }
}
