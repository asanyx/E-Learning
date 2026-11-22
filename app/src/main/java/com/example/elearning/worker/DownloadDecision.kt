package com.example.elearning.worker

import com.example.elearning.data.download.DownloadResult

internal sealed interface DownloadDecision {
    data object Success : DownloadDecision
    data object Retry : DownloadDecision
    data class Failure(val reason: String) : DownloadDecision
}

internal fun downloadDecision(result: DownloadResult, runAttemptCount: Int): DownloadDecision = when (result) {
    is DownloadResult.Success -> DownloadDecision.Success
    is DownloadResult.Failed -> DownloadDecision.Failure(result.reason)
    DownloadResult.Retryable -> if (runAttemptCount < 3) DownloadDecision.Retry
        else DownloadDecision.Failure("Wyczerpano próby pobierania")
}
