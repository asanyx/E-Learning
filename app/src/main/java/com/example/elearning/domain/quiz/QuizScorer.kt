package com.example.elearning.domain.quiz

object QuizScorer {
    fun score(
        questions: List<QuizQuestion>,
        answers: Map<String, Int?>,
        passThresholdPercent: Int = 60
    ): QuizOutcome {
        require(passThresholdPercent in 0..100) {
            "Próg zaliczenia musi mieścić się w zakresie 0..100. Podano: $passThresholdPercent."
        }

        val perQuestion = questions.map { question ->
            val selected = answers[question.id]
            QuestionOutcome(
                questionId = question.id,
                selectedIndex = selected,
                correctIndex = question.correctIndex,
                isCorrect = selected == question.correctIndex && question.correctIndex in question.options.indices
            )
        }
        val total = questions.size
        val correctCount = perQuestion.count { it.isCorrect }
        val percent = if (total == 0) 0 else (correctCount * 100L / total).toInt()
        return QuizOutcome(correctCount, total, percent, total > 0 && percent >= passThresholdPercent, perQuestion)
    }
}
