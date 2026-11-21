package com.example.elearning.domain.quiz

data class ReviewItem(
    val questionText: String,
    val selectedAnswerText: String?,
    val correctAnswerText: String?,
    val isCorrect: Boolean
)

fun buildReview(questions: List<QuizQuestion>, outcome: QuizOutcome): List<ReviewItem> {
    val results = outcome.perQuestion.associateBy { it.questionId }
    return questions.map { question ->
        val result = results[question.id]
        val selected = result?.selectedIndex?.let(question.options::getOrNull)
        val correct = question.options.getOrNull(result?.correctIndex ?: question.correctIndex)
        ReviewItem(question.text, selected, correct, result?.isCorrect == true && selected != null && correct != null)
    }
}
