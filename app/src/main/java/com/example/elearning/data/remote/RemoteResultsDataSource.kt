package com.example.elearning.data.remote

import com.example.elearning.domain.model.QuizResult

interface RemoteResultsDataSource {
    suspend fun upload(userId: String, results: List<QuizResult>)
}
