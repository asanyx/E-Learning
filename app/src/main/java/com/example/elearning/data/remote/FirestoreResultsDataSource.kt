package com.example.elearning.data.remote

import com.example.elearning.domain.model.QuizResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal fun resultDocumentId(quizId: String, completedAt: Long) = "${quizId}_${completedAt}"

class FirestoreResultsDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RemoteResultsDataSource {
    override suspend fun upload(userId: String, results: List<QuizResult>) {
        require(results.all { it.userId == userId })
        val collection = firestore.collection("users").document(userId).collection("quizResults")
        for (chunk in results.chunked(400)) {
            currentCoroutineContext().ensureActive()
            val batch = firestore.batch()
            for (result in chunk) {
                val doc = collection.document(resultDocumentId(result.quizId, result.completedAt))
                batch.set(doc, mapOf(
                    "quizId" to result.quizId,
                    "correctCount" to result.correctCount,
                    "total" to result.total,
                    "percent" to result.percent,
                    "passed" to result.passed,
                    "completedAt" to result.completedAt
                ))
            }
            batch.commit().awaitValue()
        }
    }
}
