package com.example.util

/**
 * Helper to ensure all video streams and downloads are routed through Harsh's high-speed
 * Cloudflare Edge Worker proxy using Harsh's master token.
 * This guarantees ultra-fast range requests, zero-buffering seeking, and shields the origin server from load.
 */
object StreamUrlHelper {
    const val STREAMING_WORKER_BASE = "https://telegram-stremio-streaming-worker.harshkumar36335.workers.dev"
    const val HARSH_TOKEN = "O6zE4PZv1RHKMxWc0ZU5SkLgdObmoJgh"
    const val HARSH_TOKEN_NAME = "Harsh🎯"

    /**
     * Converts any stream or download URL to route through Harsh's high-speed
     * Cloudflare edge worker proxy with Harsh's master token.
     */
    fun toWorkerProxyUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url
        val trimmed = url.trim()

        if (trimmed.contains("/dl/")) {
            val afterDl = trimmed.substringAfter("/dl/")
            val parts = afterDl.split("/", limit = 2)
            val rest = if (parts.size > 1) {
                val maybeToken = parts[0]
                if (maybeToken.length in 25..45) {
                    parts[1]
                } else {
                    afterDl
                }
            } else {
                afterDl
            }
            return "$STREAMING_WORKER_BASE/dl/$HARSH_TOKEN/$rest"
        }

        return trimmed
    }
}
