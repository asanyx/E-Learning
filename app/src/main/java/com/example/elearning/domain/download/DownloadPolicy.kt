package com.example.elearning.domain.download

import com.example.elearning.domain.model.MaterialType
import java.net.URI
import java.net.URISyntaxException

fun isSafeExternalUrl(url: String): Boolean = try {
    val uri = URI(url)
    uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
} catch (_: URISyntaxException) {
    false
}

object DownloadPolicy {
    const val maxBytes = 100L * 1024 * 1024
    const val reserveBytes = 20L * 1024 * 1024
    private val validId = Regex("[A-Za-z0-9_-]+")

    fun isDownloadable(type: MaterialType, url: String) =
        type in setOf(MaterialType.PDF, MaterialType.VIDEO) && isSafeExternalUrl(url)

    fun checkedId(id: String): String {
        require(validId.matches(id)) { "Nieprawidłowy identyfikator materiału" }
        return id
    }

    fun partName(id: String) = "${checkedId(id)}.part"

    fun fileName(id: String, type: MaterialType): String {
        val extension = when (type) {
            MaterialType.PDF -> "pdf"
            MaterialType.VIDEO -> "mp4"
            MaterialType.TEXT -> error("Tekst jest przechowywany w bazie")
        }
        return "${checkedId(id)}.$extension"
    }
}
