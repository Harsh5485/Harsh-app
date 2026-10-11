package com.example.data.model

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

data class DownloadTask(
    val downloadId: Long,
    val mediaId: String = "",
    val title: String,
    val subtitle: String? = null,
    val poster: String? = null,
    val streamUrl: String,
    val quality: String = "1080p",
    val fileName: String,
    val downloadPath: String = "/storage/emulated/0/Download/HarshStream/Video",
    val localFilePath: String? = null,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val downloadSpeedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val threadsCount: Int = 8,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Int
        get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100) else 0

    val formattedDownloadedSize: String
        get() = formatBytes(bytesDownloaded)

    val formattedTotalSize: String
        get() = if (totalBytes > 0) formatBytes(totalBytes) else "Unknown"

    val formattedSpeed: String
        get() = when (status) {
            DownloadStatus.DOWNLOADING -> {
                if (downloadSpeedBytesPerSec > 0) {
                    val mbPerSec = downloadSpeedBytesPerSec.toDouble() / (1024 * 1024)
                    if (mbPerSec >= 1.0) {
                        String.format(java.util.Locale.US, "⚡ %.1f MB/s", mbPerSec)
                    } else {
                        val kbPerSec = downloadSpeedBytesPerSec.toDouble() / 1024
                        String.format(java.util.Locale.US, "⚡ %.0f KB/s", kbPerSec)
                    }
                } else {
                    "⚡ Connecting..."
                }
            }
            DownloadStatus.PAUSED -> "Paused"
            DownloadStatus.COMPLETED -> "Done"
            DownloadStatus.FAILED -> "Failed"
            DownloadStatus.PENDING -> "Pending"
        }

    val formattedEta: String
        get() = if (status == DownloadStatus.DOWNLOADING && etaSeconds > 0) {
            val hours = etaSeconds / 3600
            val mins = (etaSeconds % 3600) / 60
            val secs = etaSeconds % 60
            when {
                hours > 0 -> "~${hours}h ${mins}m left"
                mins > 0 -> "~${mins}m ${secs}s left"
                else -> "~${secs}s left"
            }
        } else if (status == DownloadStatus.COMPLETED) {
            "Done"
        } else {
            ""
        }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format(java.util.Locale.US, "%.2f GB", bytes.toDouble() / (1024 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }

    private fun formatSpeedBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes.toDouble() / 1024)
            else -> "$bytes B"
        }
    }
}
