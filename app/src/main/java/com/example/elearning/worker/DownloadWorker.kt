package com.example.elearning.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.elearning.ElearningApp
import com.example.elearning.domain.download.DownloadPolicy
import com.example.elearning.domain.model.MaterialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val id = inputData.getString(ID) ?: return@withContext failure("Brak identyfikatora")
        val container = (applicationContext as ElearningApp).container
        val row = container.database.materialDao().observeById(id).first()
            ?: return@withContext failure("Materiał nie istnieje")
        val type = MaterialType.entries.find { it.name == row.type }
        if (type == null || !DownloadPolicy.isDownloadable(type, row.contentUrl)) {
            return@withContext failure("Niedozwolony materiał lub adres")
        }
        val progress = Channel<Int>(Channel.CONFLATED)
        val reporter = launch {
            for (percent in progress) setProgress(workDataOf(PERCENT to percent))
        }
        var lastPercent: Int? = null
        val result = try {
            container.materialDownloader.download(id, row.contentUrl, type) { percent ->
                if (percent != null && percent != lastPercent) {
                    progress.trySend(percent)
                    lastPercent = percent
                }
            }
        } finally {
            progress.close()
        }
        reporter.join()
        when (val decision = downloadDecision(result, runAttemptCount)) {
            DownloadDecision.Success -> Result.success()
            DownloadDecision.Retry -> Result.retry()
            is DownloadDecision.Failure -> failure(decision.reason)
        }
    }

    private fun failure(reason: String) = Result.failure(workDataOf(REASON to reason))

    companion object {
        const val ID = "materialId"
        const val PERCENT = "percent"
        const val REASON = "reason"
    }
}
