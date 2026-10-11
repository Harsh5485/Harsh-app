package com.example.data.repository

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.app.NotificationCompat
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.util.StreamUrlHelper
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class DownloadRepository(private val context: Context) {

    private val downloadHttpClient = OkHttpClient.Builder()
        .dispatcher(Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 32
        })
        .connectionPool(ConnectionPool(32, 5, TimeUnit.MINUTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val prefs = context.getSharedPreferences("nuvio_downloads", Context.MODE_PRIVATE)

    private val _customPath = MutableStateFlow(
        prefs.getString("custom_download_path", "/storage/emulated/0/Download/Harsh") ?: "/storage/emulated/0/Download/Harsh"
    )
    val customPath: StateFlow<String> = _customPath.asStateFlow()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()
    private val downloadListAdapter = moshi.adapter<List<DownloadTask>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, DownloadTask::class.java)
    )

    private val _downloads = MutableStateFlow<List<DownloadTask>>(loadSavedDownloads())
    val downloads: StateFlow<List<DownloadTask>> = _downloads.asStateFlow()

    private fun loadSavedDownloads(): List<DownloadTask> {
        val json = prefs.getString("saved_downloads_json", null) ?: return emptyList()
        return try {
            downloadListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDownloads() {
        val json = downloadListAdapter.toJson(_downloads.value)
        prefs.edit { putString("saved_downloads_json", json) }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var progressTrackerJob: Job? = null

    private val lastBytesMap = mutableMapOf<Long, Pair<Long, Long>>() // downloadId -> (bytes, timestamp)
    private val customDownloaderJobs = mutableMapOf<Long, Job>()

    init {
        createNotificationChannel()
        startProgressTracker()
        
        // Auto-resume downloads that were in progress
        scope.launch {
            delay(1000)
            _downloads.value.forEach { task ->
                if (task.status == DownloadStatus.DOWNLOADING) {
                    startTurboParallelHttpDownload(task)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "harsh_downloads_channel",
                "Harsh Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Harsh high-speed background video downloads"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun updateSystemNotification(task: DownloadTask) {
        val builder = NotificationCompat.Builder(context, "harsh_downloads_channel")
            .setContentTitle("Harsh: ${task.title}")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOnlyAlertOnce(true)

        when (task.status) {
            DownloadStatus.DOWNLOADING -> {
                val speedText = task.formattedSpeed
                val sizeText = "${task.formattedDownloadedSize} / ${task.formattedTotalSize}"
                builder.setContentText("$speedText • $sizeText")
                    .setProgress(100, task.progressPercent, task.totalBytes <= 0)
                    .setOngoing(true)
            }
            DownloadStatus.COMPLETED -> {
                builder.setContentText("Download completed: ${task.fileName}")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            }
            DownloadStatus.FAILED -> {
                builder.setContentText("Download failed. Tap to retry in app.")
                    .setProgress(0, 0, false)
                    .setOngoing(false)
            }
            DownloadStatus.PAUSED -> {
                builder.setContentText("Download paused")
                    .setProgress(100, task.progressPercent, false)
                    .setOngoing(false)
            }
            else -> return
        }

        try {
            notificationManager?.notify(task.downloadId.toInt(), builder.build())
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Notification error", e)
        }
    }

    private fun sanitizeFileNameWithDots(name: String): String {
        val extension = when {
            name.endsWith(".mkv", ignoreCase = true) -> ".mkv"
            name.endsWith(".avi", ignoreCase = true) -> ".avi"
            name.endsWith(".mp4", ignoreCase = true) -> ".mp4"
            else -> ".mp4"
        }
        val nameWithoutExt = name.removeSuffix(".mp4").removeSuffix(".mkv").removeSuffix(".avi")
        val dotted = nameWithoutExt.replace(Regex("[^a-zA-Z0-9]+"), ".").trim('.')
        val cleanBase = if (dotted.isBlank()) "Harsh.Video.${System.currentTimeMillis()}" else dotted
        return "$cleanBase$extension"
    }

    fun setCustomPath(newPath: String) {
        val cleaned = newPath.trim()
        if (cleaned.isNotBlank()) {
            _customPath.value = cleaned
            prefs.edit { putString("custom_download_path", cleaned) }
            Toast.makeText(context, "Storage folder updated: $cleaned", Toast.LENGTH_SHORT).show()
        }
    }

    fun startDownload(
        mediaId: String,
        title: String,
        subtitle: String?,
        poster: String?,
        streamUrl: String,
        quality: String,
        rawFileName: String
    ): Long {
        val cleanStreamUrl = StreamUrlHelper.toWorkerProxyUrl(streamUrl.trim()) ?: streamUrl.trim()
        if (cleanStreamUrl.isBlank()) {
            Toast.makeText(context, "Invalid stream URL", Toast.LENGTH_SHORT).show()
            return -1L
        }

        val inputName = if (rawFileName.isNotBlank()) rawFileName else title
        val finalFileName = sanitizeFileNameWithDots(inputName)

        val currentFolderPath = _customPath.value

        // Check if file is already downloaded in folder path
        val targetFile = try {
            getPublicTargetFile(currentFolderPath, finalFileName)
        } catch (e: Exception) {
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: File(context.filesDir, "downloads").apply { mkdirs() }
            File(targetDir, finalFileName)
        }

        if (targetFile.exists() && targetFile.length() > 0L) {
            val sizeMb = targetFile.length() / (1024 * 1024)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(
                    context,
                    "⚠️ Already Downloaded!\n'$finalFileName' ($sizeMb MB) already exists in Harsh folder.",
                    Toast.LENGTH_LONG
                ).show()
            }

            val completedTask = DownloadTask(
                downloadId = System.currentTimeMillis(),
                mediaId = mediaId,
                title = title,
                subtitle = subtitle ?: "Already Downloaded",
                poster = poster,
                streamUrl = cleanStreamUrl,
                quality = quality,
                fileName = finalFileName,
                downloadPath = targetFile.absolutePath,
                bytesDownloaded = targetFile.length(),
                totalBytes = targetFile.length(),
                status = DownloadStatus.COMPLETED,
                localFilePath = targetFile.absolutePath
            )
            val filtered = _downloads.value.filter { it.fileName != finalFileName }
            _downloads.value = listOf(completedTask) + filtered
            saveDownloads()
            return completedTask.downloadId
        }

        // Always use Turbo Multi-Threaded Parallel Engine for maximum speed!
        val generatedId = System.currentTimeMillis()
        val task = DownloadTask(
            downloadId = generatedId,
            mediaId = mediaId,
            title = title,
            subtitle = subtitle,
            poster = poster,
            streamUrl = cleanStreamUrl,
            quality = quality,
            fileName = finalFileName,
            downloadPath = "$currentFolderPath/$finalFileName",
            status = DownloadStatus.DOWNLOADING
        )
        _downloads.value = listOf(task) + _downloads.value.filter { it.downloadId != generatedId }
        saveDownloads()
        Toast.makeText(context, "⚡ Turbo Engine (16x Parallel Threads): $title", Toast.LENGTH_SHORT).show()
        startTurboParallelHttpDownload(task)
        return generatedId
    }

    fun startDirectLinkDownload(url: String, title: String?): Long {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank() || (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://"))) {
            Toast.makeText(context, "Please enter a valid HTTP/HTTPS video URL", Toast.LENGTH_SHORT).show()
            return -1L
        }

        val derivedName = cleanUrl.substringAfterLast('/').substringBefore('?').takeIf { it.contains(".") } ?: "Harsh.Video.${System.currentTimeMillis()}.mp4"
        val displayTitle = if (!title.isNullOrBlank()) title.trim() else derivedName

        return startDownload(
            mediaId = "direct_${System.currentTimeMillis()}",
            title = displayTitle,
            subtitle = "Direct Link Download",
            poster = null,
            streamUrl = cleanUrl,
            quality = "Video",
            rawFileName = derivedName
        )
    }

    fun deleteDownload(downloadId: Long) {
        val taskToDelete = _downloads.value.find { it.downloadId == downloadId }
        customDownloaderJobs[downloadId]?.cancel()
        customDownloaderJobs.remove(downloadId)

        try {
            downloadManager?.remove(downloadId)
        } catch (e: Exception) {
            Log.e("DownloadRepository", "Error removing download from manager", e)
        }

        // Delete main file and any .part chunk files from storage folder path
        if (taskToDelete != null) {
            try {
                val fileToDelete = if (!taskToDelete.localFilePath.isNullOrBlank()) {
                    File(taskToDelete.localFilePath)
                } else {
                    getPublicTargetFile(_customPath.value, taskToDelete.fileName)
                }
                if (fileToDelete.exists()) {
                    val isDeleted = fileToDelete.delete()
                    Log.d("DownloadRepository", "Deleted file on cancel: ${fileToDelete.absolutePath}, result = $isDeleted")
                }
                val parentDir = fileToDelete.parentFile ?: context.filesDir
                for (i in 0 until 16) {
                    val partFile = File(parentDir, "${taskToDelete.fileName}.part$i")
                    if (partFile.exists()) partFile.delete()
                }
            } catch (e: Exception) {
                Log.e("DownloadRepository", "Error deleting file on download cancel", e)
            }

            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(downloadId.toInt())
            } catch (e: Exception) {
                Log.e("DownloadRepository", "Error cancelling notification", e)
            }
        }

        _downloads.value = _downloads.value.filter { it.downloadId != downloadId }
        saveDownloads()
        Toast.makeText(context, "🚫 Download Cancelled & File Deleted", Toast.LENGTH_SHORT).show()
    }

    fun pauseOrResumeDownload(task: DownloadTask) {
        if (task.status == DownloadStatus.DOWNLOADING) {
            customDownloaderJobs[task.downloadId]?.cancel()
            customDownloaderJobs.remove(task.downloadId)
            val updated = task.copy(status = DownloadStatus.PAUSED, downloadSpeedBytesPerSec = 0L)
            _downloads.value = _downloads.value.map { if (it.downloadId == task.downloadId) updated else it }
            saveDownloads()
            Toast.makeText(context, "Download paused", Toast.LENGTH_SHORT).show()
        } else if (task.status == DownloadStatus.PAUSED || task.status == DownloadStatus.FAILED || task.status == DownloadStatus.PENDING) {
            Toast.makeText(context, "⚡ Resuming Turbo Download (8x Parallel Segments)...", Toast.LENGTH_SHORT).show()
            val updated = task.copy(status = DownloadStatus.DOWNLOADING)
            _downloads.value = _downloads.value.map { if (it.downloadId == task.downloadId) updated else it }
            saveDownloads()
            startTurboParallelHttpDownload(updated)
        }
    }

    private fun getPublicTargetFile(subFolderPath: String, fileName: String): File {
        val folderName = "Harsh"
        val publicDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFolder = File(publicDownloadsDir, folderName)
        if (!targetFolder.exists()) {
            try {
                targetFolder.mkdirs()
            } catch (e: Exception) {
                Log.e("DownloadRepository", "Error creating public folder $folderName", e)
            }
        }
        return File(targetFolder, fileName)
    }

    private fun startTurboParallelHttpDownload(task: DownloadTask) {
        customDownloaderJobs[task.downloadId]?.cancel()

        val job = scope.launch(Dispatchers.IO) {
            try {
                val targetFile = try {
                    getPublicTargetFile(_customPath.value, task.fileName)
                } catch (e: Exception) {
                    val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        ?: File(context.filesDir, "downloads").apply { mkdirs() }
                    File(targetDir, task.fileName)
                }

                val parentDir = targetFile.parentFile ?: context.filesDir

                // Step 1: Probe the stream URL to detect exact content length and Range support
                val probeReq = Request.Builder()
                    .url(task.streamUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Range", "bytes=0-0")
                    .get()
                    .build()

                var contentLength = task.totalBytes
                var supportsRange = true

                try {
                    val probeResp = downloadHttpClient.newCall(probeReq).execute()
                    probeResp.use { resp ->
                        if (resp.isSuccessful) {
                            val cr = resp.header("Content-Range")
                            val totalFromCr = cr?.substringAfterLast('/')?.trim()?.toLongOrNull()
                            if (totalFromCr != null && totalFromCr > 0) {
                                contentLength = totalFromCr
                            }
                            supportsRange = resp.code == 206 || resp.header("Accept-Ranges")?.contains("bytes", ignoreCase = true) == true
                        }
                    }
                } catch (e: Exception) {
                    Log.w("DownloadRepository", "Probe request failed, using task metadata", e)
                }

                val existingBytes = if (targetFile.exists()) targetFile.length() else 0L

                // If file is already fully downloaded
                if (contentLength > 0 && existingBytes >= contentLength) {
                    val completedTask = task.copy(
                        bytesDownloaded = contentLength,
                        totalBytes = contentLength,
                        downloadSpeedBytesPerSec = 0L,
                        etaSeconds = 0L,
                        status = DownloadStatus.COMPLETED,
                        localFilePath = targetFile.absolutePath
                    )
                    _downloads.value = _downloads.value.map {
                        if (it.downloadId == task.downloadId) completedTask else it
                    }
                    saveDownloads()
                    updateSystemNotification(completedTask)
                    return@launch
                }

                val remainingBytes = if (contentLength > 0) contentLength - existingBytes else 0L
                val numWorkers = if (supportsRange && remainingBytes > 30 * 1024 * 1024L) 8 else 1

                if (numWorkers > 1) {
                    // TRUE 8-WAY PARALLEL MULTI-THREADED SEGMENTED DOWNLOAD
                    val segmentSize = remainingBytes / numWorkers
                    val partFiles = (0 until numWorkers).map { i ->
                        File(parentDir, "${task.fileName}.part$i")
                    }

                    // Progress monitor job
                    val progressJob = launch {
                        var lastTime = System.currentTimeMillis()
                        var lastBytes = existingBytes + partFiles.sumOf { if (it.exists()) it.length() else 0L }
                        var smoothedSpeed = 0L

                        while (isActive) {
                            delay(400)
                            val now = System.currentTimeMillis()
                            val currentPartBytes = partFiles.sumOf { if (it.exists()) it.length() else 0L }
                            val totalDownloaded = (existingBytes + currentPartBytes).coerceAtMost(contentLength)
                            val timeDiffSec = (now - lastTime) / 1000.0

                            if (timeDiffSec >= 0.35) {
                                val bytesDiff = (totalDownloaded - lastBytes).coerceAtLeast(0L)
                                val instantSpeed = (bytesDiff / timeDiffSec).toLong()
                                smoothedSpeed = if (smoothedSpeed == 0L) instantSpeed else ((smoothedSpeed * 0.75) + (instantSpeed * 0.25)).toLong()
                                lastTime = now
                                lastBytes = totalDownloaded

                                val remBytes = (contentLength - totalDownloaded).coerceAtLeast(0L)
                                val eta = if (smoothedSpeed > 0) remBytes / smoothedSpeed else 0L

                                val updated = task.copy(
                                    bytesDownloaded = totalDownloaded,
                                    totalBytes = if (contentLength > 0) contentLength else totalDownloaded,
                                    downloadSpeedBytesPerSec = smoothedSpeed,
                                    etaSeconds = eta,
                                    status = DownloadStatus.DOWNLOADING,
                                    localFilePath = targetFile.absolutePath
                                )
                                _downloads.value = _downloads.value.map {
                                    if (it.downloadId == task.downloadId) updated else it
                                }
                                updateSystemNotification(updated)
                            }
                        }
                    }

                    // Launch all 8 workers in parallel coroutines
                    val workerJobs = (0 until numWorkers).map { idx ->
                        launch(Dispatchers.IO) {
                            val segStart = existingBytes + (idx * segmentSize)
                            val segEnd = if (idx == numWorkers - 1) contentLength - 1 else existingBytes + ((idx + 1) * segmentSize) - 1
                            val partFile = partFiles[idx]

                            var retryCount = 0
                            var done = false
                            while (!done && retryCount < 5 && isActive) {
                                try {
                                    val downloadedInPart = if (partFile.exists()) partFile.length() else 0L
                                    val reqStart = segStart + downloadedInPart
                                    if (reqStart > segEnd) {
                                        done = true
                                        break
                                    }

                                    val workerReq = Request.Builder()
                                        .url(task.streamUrl)
                                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, Gecko) Chrome/122.0.0.0 Safari/537.36")
                                        .header("Range", "bytes=$reqStart-$segEnd")
                                        .get()
                                        .build()

                                    val response = downloadHttpClient.newCall(workerReq).execute()
                                    if (response.isSuccessful) {
                                        val body = response.body
                                        if (body != null) {
                                            val inputStream = body.byteStream()
                                            val outputStream = FileOutputStream(partFile, true)
                                            val buffer = ByteArray(256 * 1024) // 256KB high-throughput buffer

                                            outputStream.use { out ->
                                                inputStream.use { input ->
                                                    var bytesRead = 0
                                                    while (isActive && input.read(buffer).also { bytesRead = it } != -1) {
                                                        out.write(buffer, 0, bytesRead)
                                                    }
                                                }
                                            }
                                            if (isActive) {
                                                val finalPartLen = if (partFile.exists()) partFile.length() else 0L
                                                if (segStart + finalPartLen >= segEnd) {
                                                    done = true
                                                }
                                            }
                                        }
                                    } else {
                                        retryCount++
                                        delay(1000)
                                    }
                                } catch (e: Exception) {
                                    if (!isActive) break
                                    Log.w("DownloadRepository", "Worker $idx chunk connection interrupted, retrying ($retryCount/5)", e)
                                    retryCount++
                                    delay(1000)
                                }
                            }
                        }
                    }

                    // Wait for all 8 workers to finish
                    workerJobs.joinAll()
                    progressJob.cancel()

                    // If cancelled or paused, do not merge part files yet
                    if (!isActive) return@launch

                    // Merge all 8 parts into final file
                    val outStream = FileOutputStream(targetFile, existingBytes > 0)
                    outStream.use { out ->
                        val mergeBuffer = ByteArray(1024 * 1024) // 1MB buffer for fast local disk copy
                        for (i in 0 until numWorkers) {
                            val partFile = partFiles[i]
                            if (partFile.exists()) {
                                partFile.inputStream().use { input ->
                                    var read = 0
                                    while (input.read(mergeBuffer).also { read = it } != -1) {
                                        out.write(mergeBuffer, 0, read)
                                    }
                                }
                                partFile.delete()
                            }
                        }
                    }

                    val finalSize = if (contentLength > 0) contentLength else targetFile.length()
                    val completedTask = task.copy(
                        bytesDownloaded = finalSize,
                        totalBytes = finalSize,
                        downloadSpeedBytesPerSec = 0L,
                        etaSeconds = 0L,
                        status = DownloadStatus.COMPLETED,
                        localFilePath = targetFile.absolutePath
                    )
                    _downloads.value = _downloads.value.map {
                        if (it.downloadId == task.downloadId) completedTask else it
                    }
                    saveDownloads()
                    updateSystemNotification(completedTask)

                } else {
                    // Single stream fallback using OkHttp HTTP/2 with 256KB buffer
                    val reqBuilder = Request.Builder()
                        .url(task.streamUrl)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, Gecko) Chrome/122.0.0.0 Safari/537.36")
                    if (existingBytes > 0) {
                        reqBuilder.header("Range", "bytes=$existingBytes-")
                    }

                    val response = downloadHttpClient.newCall(reqBuilder.build()).execute()
                    if (response.isSuccessful) {
                        val isAppend = response.code == 206 && existingBytes > 0
                        val startOffset = if (isAppend) existingBytes else 0L
                        val body = response.body
                        if (body != null) {
                            val inStream = body.byteStream()
                            val outStream = FileOutputStream(targetFile, isAppend)
                            val buffer = ByteArray(256 * 1024)
                            var totalDownloaded = startOffset
                            var lastTime = System.currentTimeMillis()
                            var lastBytes = startOffset
                            var smoothedSpeed = 0L

                            outStream.use { out ->
                                inStream.use { input ->
                                    var read = 0
                                    while (isActive && input.read(buffer).also { read = it } != -1) {
                                        out.write(buffer, 0, read)
                                        totalDownloaded += read

                                        val now = System.currentTimeMillis()
                                        val diffSec = (now - lastTime) / 1000.0
                                        if (diffSec >= 0.35) {
                                            val instantSpeed = ((totalDownloaded - lastBytes) / diffSec).toLong().coerceAtLeast(0L)
                                            smoothedSpeed = if (smoothedSpeed == 0L) instantSpeed else ((smoothedSpeed * 0.75) + (instantSpeed * 0.25)).toLong()
                                            lastTime = now
                                            lastBytes = totalDownloaded
                                            val rem = (contentLength - totalDownloaded).coerceAtLeast(0L)
                                            val eta = if (smoothedSpeed > 0) rem / smoothedSpeed else 0L

                                            val updated = task.copy(
                                                bytesDownloaded = totalDownloaded,
                                                totalBytes = if (contentLength > 0) contentLength else totalDownloaded,
                                                downloadSpeedBytesPerSec = smoothedSpeed,
                                                etaSeconds = eta,
                                                status = DownloadStatus.DOWNLOADING,
                                                localFilePath = targetFile.absolutePath
                                            )
                                            _downloads.value = _downloads.value.map {
                                                if (it.downloadId == task.downloadId) updated else it
                                            }
                                            updateSystemNotification(updated)
                                        }
                                    }
                                }
                            }

                            if (isActive) {
                                val finalSize = if (contentLength > 0) contentLength else totalDownloaded
                                val completedTask = task.copy(
                                    bytesDownloaded = finalSize,
                                    totalBytes = finalSize,
                                    downloadSpeedBytesPerSec = 0L,
                                    etaSeconds = 0L,
                                    status = DownloadStatus.COMPLETED,
                                    localFilePath = targetFile.absolutePath
                                )
                                _downloads.value = _downloads.value.map {
                                    if (it.downloadId == task.downloadId) completedTask else it
                                }
                                saveDownloads()
                                updateSystemNotification(completedTask)
                            }
                        }
                    } else {
                        val failedTask = task.copy(status = DownloadStatus.FAILED, downloadSpeedBytesPerSec = 0L)
                        _downloads.value = _downloads.value.map {
                            if (it.downloadId == task.downloadId) failedTask else it
                        }
                        updateSystemNotification(failedTask)
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    return@launch
                }
                Log.e("DownloadRepository", "Turbo downloader exception", e)
                val failedTask = task.copy(status = DownloadStatus.FAILED, downloadSpeedBytesPerSec = 0L)
                _downloads.value = _downloads.value.map {
                    if (it.downloadId == task.downloadId) failedTask else it
                }
            }
        }
        customDownloaderJobs[task.downloadId] = job
    }

    private fun startProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = scope.launch {
            while (isActive) {
                val currentTasks = _downloads.value
                if (currentTasks.isNotEmpty() && downloadManager != null) {
                    val updatedList = currentTasks.map { task ->
                        if (task.status == DownloadStatus.COMPLETED || task.status == DownloadStatus.PAUSED) {
                            task
                        } else if (customDownloaderJobs.containsKey(task.downloadId)) {
                            task
                        } else {
                            queryDownloadStatus(task)
                        }
                    }
                    _downloads.value = updatedList
                    saveDownloads()
                }
                delay(2000)
            }
        }
    }

    private fun queryDownloadStatus(task: DownloadTask): DownloadTask {
        if (downloadManager == null) return task

        val query = DownloadManager.Query().setFilterById(task.downloadId)
        val cursor = try {
            downloadManager.query(query)
        } catch (e: Exception) {
            null
        }

        if (cursor != null && cursor.moveToFirst()) {
            val bytesSoFarIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalBytesIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val localUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

            val bytesSoFar = if (bytesSoFarIdx >= 0) cursor.getLong(bytesSoFarIdx) else task.bytesDownloaded
            val totalBytes = if (totalBytesIdx >= 0) cursor.getLong(totalBytesIdx) else task.totalBytes
            val statusInt = if (statusIdx >= 0) cursor.getInt(statusIdx) else -1
            val localUri = if (localUriIdx >= 0) cursor.getString(localUriIdx) else null

            cursor.close()

            val now = System.currentTimeMillis()
            val lastInfo = lastBytesMap[task.downloadId]
            val speedBytesPerSec = if (lastInfo != null) {
                val timeDiffSec = (now - lastInfo.second) / 1000.0
                if (timeDiffSec > 0) {
                    ((bytesSoFar - lastInfo.first) / timeDiffSec).toLong().coerceAtLeast(0L)
                } else 0L
            } else 0L

            lastBytesMap[task.downloadId] = Pair(bytesSoFar, now)

            val newStatus = when (statusInt) {
                DownloadManager.STATUS_RUNNING -> DownloadStatus.DOWNLOADING
                DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
                DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                else -> DownloadStatus.PENDING
            }

            val remainingBytes = (totalBytes - bytesSoFar).coerceAtLeast(0L)
            val etaSec = if (speedBytesPerSec > 0) remainingBytes / speedBytesPerSec else 0L

            return task.copy(
                bytesDownloaded = bytesSoFar,
                totalBytes = if (totalBytes > 0) totalBytes else task.totalBytes,
                downloadSpeedBytesPerSec = speedBytesPerSec,
                etaSeconds = etaSec,
                status = newStatus,
                localFilePath = localUri ?: task.localFilePath
            )
        }
        cursor?.close()
        return task
    }
}
