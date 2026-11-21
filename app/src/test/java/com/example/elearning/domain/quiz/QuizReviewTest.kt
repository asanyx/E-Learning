package com.example.elearning.domain.quiz

import org.junit.Assert.assertEquals
import org.junit.Test

class QuizReviewTest {
    private val first = QuizQuestion("a", "Pierwsze?", listOf("A", "B"), 1)
    private val second = QuizQuestion("b", "Drugie?", listOf("C", "D"), 0)
    private val questions = listOf(first, second)

    @Test
    fun correctAnswersFollowQuestionOrderEvenWhenResultsAreReversed() {
        val outcome = QuizScorer.score(questions.reversed(), mapOf("a" to 1, "b" to 0))
        assertEquals(listOf(
            ReviewItem("Pierwsze?", "B", "B", true),
            ReviewItem("Drugie?", "C", "C", true)
        ), buildReview(questions, outcome))
    }

    @Test
    fun wrongAndMissingAnswersKeepCorrectAnswer() {
        val outcome = QuizScorer.score(questions, mapOf("a" to 0))
        assertEquals(listOf(
            ReviewItem("Pierwsze?", "A", "B", false),
            ReviewItem("Drugie?", null, "C", false)
        ), buildReview(questions, outcome))
    }

    @Test
    fun invalidIndicesAndMissingResultAreSafe() {
        val outcome = QuizOutcome(0, 2, 0, false, listOf(QuestionOutcome("a", -1, 2, false)))
        assertEquals(listOf(
            ReviewItem("Pierwsze?", null, null, false),
            ReviewItem("Drugie?", null, "C", false)
        ), buildReview(questions, outcome))
    }
}
