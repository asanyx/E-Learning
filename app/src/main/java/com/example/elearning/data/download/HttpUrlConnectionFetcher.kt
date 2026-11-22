package com.example.elearning.data.download

import com.example.elearning.domain.download.DownloadPolicy
import com.example.elearning.domain.download.FetchResult
import com.example.elearning.domain.download.HttpFetcher
import com.example.elearning.domain.download.isSafeExternalUrl
import java.io.IOException
import java.io.OutputStream
import java.net.MalformedURLException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class HttpUrlConnectionFetcher(
    private val open: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection }
) : HttpFetcher {
    override suspend fun fetch(url: String, sink: OutputStream, onProgress: (Long, Long?) -> Unit): FetchResult {
        var address = url
        try {
            repeat(6) {
                currentCoroutineContext().ensureActive()
                if (!isSafeExternalUrl(address)) return FetchResult.UnsafeUrl
                val connection = open(URL(address))
                try {
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = 15_000
                    connection.readTimeout = 20_000
                    connection.setRequestProperty("Accept-Encoding", "identity")
                    when (val code = connection.responseCode) {
                        301, 302, 303, 307, 308 -> {
                            val location = connection.getHeaderField("Location") ?: return FetchResult.UnsafeUrl
                            address = try {
                                URL(connection.url, location).toExternalForm()
                            } catch (_: MalformedURLException) {
                                return FetchResult.UnsafeUrl
                            }
                            if (!isSafeExternalUrl(address)) return FetchResult.UnsafeUrl
                        }
                        200 -> return copyBody(connection, sink, onProgress)
                        in 400..499 -> return FetchResult.HttpClientError(code)
                        in 500..599 -> return FetchResult.HttpServerError(code)
                        else -> throw IOException("Nieoczekiwany status HTTP $code")
                    }
                } finally {
                    connection.disconnect()
                }
            }
            throw IOException("Zbyt wiele przekierowań")
        } catch (error: IOException) {
            currentCoroutineContext().ensureActive()
            return FetchResult.IoError(error)
        }
    }

    private suspend fun copyBody(
        connection: HttpURLConnection, sink: OutputStream, onProgress: (Long, Long?) -> Unit
    ): FetchResult {
        val total = connection.contentLengthLong.takeIf { it >= 0 }
        if (total != null && total > DownloadPolicy.maxBytes) return FetchResult.TooLarge
        onProgress(0, total)
        var bytes = 0L
        connection.inputStream.use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count == -1) break
                bytes += count
                if (bytes > DownloadPolicy.maxBytes) return FetchResult.TooLarge
                onProgress(bytes, total)
                sink.write(buffer, 0, count)
            }
        }
        if (total != null && bytes != total) throw IOException("Niezgodna długość odpowiedzi")
        return FetchResult.Success
    }
}
