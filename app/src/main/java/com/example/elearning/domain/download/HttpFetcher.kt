package com.example.elearning.domain.download

import java.io.IOException
import java.io.OutputStream

sealed interface FetchResult {
    data object Success : FetchResult
    data class HttpClientError(val code: Int) : FetchResult
    data class HttpServerError(val code: Int) : FetchResult
    data object UnsafeUrl : FetchResult
    data object TooLarge : FetchResult
    data class IoError(val cause: IOException) : FetchResult
}

fun interface HttpFetcher {
    // Pierwszy raport (0, Content-Length lub null) poprzedza odczyt treści.
    suspend fun fetch(url: String, sink: OutputStream, onProgress: (Long, Long?) -> Unit): FetchResult
}
