package com.example.elearning.domain.quiz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScorerTest {
    private val questions = (1..5).map {
        QuizQuestion("q$it", "Pytanie $it", listOf("A", "B"), correctIndex = it % 2)
    }
    private val correctAnswers = questions.associate { it.id to it.correctIndex }
    private val wrongAnswers = questions.associate { it.id to 1 - it.correctIndex }

    @Test
    fun allCorrectPassEvenAtFullThreshold() {
        val result = QuizScorer.score(questions, correctAnswers, 100)
        assertEquals(5, result.correctCount)
        assertEquals(5, result.total)
        assertEquals(100, result.percent)
        assertTrue(result.passed)
        assertFalse(QuizScorer.score(questions, correctAnswers - "q1", 100).passed)
    }

    @Test
    fun allWrongFailDefaultThresholdButPassZeroThreshold() {
        val result = QuizScorer.score(questions, wrongAnswers)
        assertEquals(0, result.percent)
        assertFalse(result.passed)
        assertTrue(QuizScorer.score(questions, wrongAnswers, 0).passed)
    }

    @Test
    fun missingAndExplicitNullAnswersAreIncorrect() {
        val result = QuizScorer.score(questions.take(2), mapOf("q2" to null))
        assertEquals(listOf(
            QuestionOutcome("q1", null, 1, false),
            QuestionOutcome("q2", null, 0, false)
        ), result.perQuestion)
    }

    @Test
    fun emptyQuizFailsEvenAtZeroThreshold() {
        assertEquals(QuizOutcome(0, 0, 0, false, emptyList()), QuizScorer.score(emptyList(), emptyMap(), 0))
    }

    @Test
    fun defaultThresholdIncludesThreeOfFiveButNotTwoOfFive() {
        val threeCorrect = correctAnswers - "q4" - "q5"
        val atThreshold = QuizScorer.score(questions, threeCorrect)
        assertEquals(60, atThreshold.percent)
        assertTrue(atThreshold.passed)
        val belowThreshold = QuizScorer.score(questions, threeCorrect - "q3")
        assertEquals(40, belowThreshold.percent)
        assertFalse(belowThreshold.passed)
    }

    @Test
    fun percentageRoundsDown() {
        val result = QuizScorer.score(questions.take(3), mapOf("q1" to 1), 34)
        assertEquals(33, result.percent)
        assertFalse(result.passed)
        assertEquals(66, QuizScorer.score(questions.take(3), correctAnswers - "q1").percent)
    }

    @Test
    fun outOfRangeIndicesArePreservedButNeverAwardPoints() {
        for (index in listOf(-1, 2)) {
            val question = questions.first().copy(correctIndex = index)
            val result = QuizScorer.score(listOf(question), mapOf(question.id to index))
            assertEquals(0, result.correctCount)
            assertEquals(QuestionOutcome(question.id, index, index, false), result.perQuestion.single())
        }
    }

    @Test
    fun ignoresUnknownAnswersAndKeepsQuestionOrder() {
        val answers = linkedMapOf("unknown" to 0, "q1" to 1, "q2" to 1)
        val result = QuizScorer.score(questions.take(2).reversed(), answers)
        assertEquals(2, result.total)
        assertEquals(1, result.correctCount)
        assertEquals(listOf(
            QuestionOutcome("q2", 1, 0, false),
            QuestionOutcome("q1", 1, 1, true)
        ), result.perQuestion)
    }

    @Test
    fun invalidThresholdThrowsEvenForEmptyQuiz() {
        for (threshold in listOf(-1, 101)) {
            val error = assertThrows(IllegalArgumentException::class.java) {
                QuizScorer.score(emptyList(), emptyMap(), threshold)
            }
            assertTrue(error.message.orEmpty().contains("0..100"))
        }
    }
}
