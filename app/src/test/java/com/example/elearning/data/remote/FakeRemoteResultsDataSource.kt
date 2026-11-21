package com.example.elearning.data.remote

import com.example.elearning.domain.model.QuizResult

internal class FakeRemoteResultsDataSource : RemoteResultsDataSource {
    val uploads = mutableListOf<Pair<String, List<QuizResult>>>()
    var onUpload: suspend () -> Unit = {}

    override suspend fun upload(userId: String, results: List<QuizResult>) {
        uploads.add(userId to results)
        onUpload()
    }
}
