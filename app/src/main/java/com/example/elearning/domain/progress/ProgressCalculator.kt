package com.example.elearning.domain.progress

object ProgressCalculator {
    fun calculate(
        materialIds: List<String>,
        completedMaterialIds: Set<String>,
        quizIds: List<String>,
        attempts: List<QuizAttempt>
    ): CourseProgress {
        val materials = materialIds.toSet()
        val quizzes = quizIds.toSet()
        val completedMaterials = materials.count { it in completedMaterialIds }
        val passedQuizIds = mutableSetOf<String>()
        val bestPercentByQuiz = mutableMapOf<String, Int>()

        for (attempt in attempts) {
            if (attempt.quizId !in quizzes) continue
            if (attempt.passed) passedQuizIds.add(attempt.quizId)
            val best = bestPercentByQuiz[attempt.quizId]
            if (best == null || attempt.percent > best) {
                bestPercentByQuiz[attempt.quizId] = attempt.percent
            }
        }

        val total = materials.size.toLong() + quizzes.size
        val completed = completedMaterials.toLong() + passedQuizIds.size
        return CourseProgress(
            completedMaterials = completedMaterials,
            totalMaterials = materials.size,
            passedQuizzes = passedQuizIds.size,
            totalQuizzes = quizzes.size,
            percent = if (total == 0L) 0 else (completed * 100 / total).toInt(),
            isCompleted = total > 0 && completed == total,
            bestPercentByQuiz = bestPercentByQuiz
        )
    }
}
