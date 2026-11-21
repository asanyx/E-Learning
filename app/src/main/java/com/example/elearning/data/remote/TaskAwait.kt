package com.example.elearning.data.remote

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <T> Task<T>.awaitValue(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        val error = task.exception
        when {
            task.isCanceled -> continuation.cancel()
            error != null -> continuation.resumeWithException(error)
            else -> continuation.resume(task.result)
        }
    }
}
