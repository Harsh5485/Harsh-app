package com.example.util

import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.os.Build
import android.util.Log
import com.example.data.model.StreamItem

/**
 * Helper to inspect device hardware media codec capabilities (MediaCodecInfo)
 * and automatically select or rank the most compatible streams for smooth playback,
 * preventing 'Codec not supported' crashes on lower-end devices.
 */
object DeviceCodecHelper {

    private const val TAG = "DeviceCodecHelper"

    private var cachedHwHevc: Boolean? = null
    private var cachedHw4K: Boolean? = null
    private var cachedHw4KHevc: Boolean? = null
    private var cachedHwAv1: Boolean? = null
    private var cachedIsLowEnd: Boolean? = null

    /**
     * Checks if the device is considered 'low-end' (e.g. Android Go, low RAM, or very old).
     */
    fun isLowEndDevice(context: android.content.Context): Boolean {
        cachedIsLowEnd?.let { return it }
        val activityManager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
        val isLowRam = activityManager?.isLowRamDevice ?: false
        
        val memInfo = android.app.ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        
        // Devices with <= 4GB RAM are treated as budget/lower-end for 4K/HEVC purposes
        val result = isLowRam || totalRamGb <= 4.1 
        cachedIsLowEnd = result
        return result
    }

    /**
     * Checks if the device has a dedicated hardware decoder for HEVC / H.265.
     */
    fun isHardwareHevcSupported(): Boolean {
        cachedHwHevc?.let { return it }
        val result = checkHardwareCodecSupport("video/hevc")
        cachedHwHevc = result
        return result
    }

    /**
     * Checks if the device hardware can decode 4K (3840x2160) video.
     */
    fun is4KSupported(): Boolean {
        cachedHw4K?.let { return it }
        val result = checkResolutionSupport(3840, 2160)
        cachedHw4K = result
        return result
    }

    /**
     * Checks if the device hardware can decode 4K HEVC specifically.
     */
    fun isHardware4kHevcSupported(): Boolean {
        cachedHw4KHevc?.let { return it }
        val result = checkHardwareCodecResolutionSupport("video/hevc", 3840, 2160)
        cachedHw4KHevc = result
        return result
    }

    /**
     * Checks if the device has a dedicated hardware decoder for AV1.
     */
    fun isHardwareAv1Supported(): Boolean {
        cachedHwAv1?.let { return it }
        val result = checkHardwareCodecSupport("video/av01")
        cachedHwAv1 = result
        return result
    }

    private fun checkHardwareCodecSupport(mimeType: String): Boolean {
        return try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            codecList.codecInfos.any { info ->
                !info.isEncoder && isHardwareDecoder(info) && supportsMimeType(info, mimeType)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking codec support for $mimeType", e)
            false
        }
    }

    private fun checkResolutionSupport(width: Int, height: Int): Boolean {
        return try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            codecList.codecInfos.any { info ->
                if (info.isEncoder || !isHardwareDecoder(info)) return@any false
                info.supportedTypes.any { mime ->
                    if (!mime.startsWith("video/")) return@any false
                    val caps = runCatching { info.getCapabilitiesForType(mime) }.getOrNull() ?: return@any false
                    val videoCaps = caps.videoCapabilities ?: return@any false
                    videoCaps.isSizeSupported(width, height)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking resolution support for ${width}x${height}", e)
            false
        }
    }

    private fun checkHardwareCodecResolutionSupport(mimeType: String, width: Int, height: Int): Boolean {
        return try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            codecList.codecInfos.any { info ->
                if (info.isEncoder || !isHardwareDecoder(info) || !supportsMimeType(info, mimeType)) return@any false
                val caps = runCatching { info.getCapabilitiesForType(mimeType) }.getOrNull() ?: return@any false
                val videoCaps = caps.videoCapabilities ?: return@any false
                videoCaps.isSizeSupported(width, height)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking hardware resolution support for $mimeType ${width}x${height}", e)
            false
        }
    }

    private fun isHardwareDecoder(info: MediaCodecInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            info.isHardwareAccelerated
        } else {
            val name = info.name.lowercase()
            !name.startsWith("omx.google.") &&
            !name.startsWith("c2.android.") &&
            !name.contains(".sw.") &&
            !name.contains("sw_") &&
            !name.contains("software")
        }
    }

    private fun supportsMimeType(info: MediaCodecInfo, mimeType: String): Boolean {
        return info.supportedTypes.any { it.equals(mimeType, ignoreCase = true) }
    }

    fun isHevcStream(stream: StreamItem): Boolean {
        val t = (stream.title ?: "").lowercase()
        val n = (stream.name ?: "").lowercase()
        val combined = "$n $t"
        return stream.displayVideoCodec.contains("HEVC", ignoreCase = true) ||
                combined.contains("x265") ||
                combined.contains("hevc") ||
                combined.contains("h.265") ||
                combined.contains("h265")
    }

    fun is4kStream(stream: StreamItem): Boolean {
        val q = stream.displayQuality
        val t = (stream.title ?: "").lowercase()
        val n = (stream.name ?: "").lowercase()
        val combined = "$n $t"
        return q == "4K UHD" ||
                combined.contains("2160p") ||
                combined.contains("2160i") ||
                (!combined.contains("ds4k") && !combined.contains("downscale") &&
                        (Regex("""\b4k\b""", RegexOption.IGNORE_CASE).containsMatchIn(combined) || combined.contains("uhd")))
    }

    fun isH264Stream(stream: StreamItem): Boolean {
        val t = (stream.title ?: "").lowercase()
        val n = (stream.name ?: "").lowercase()
        val combined = "$n $t"
        return stream.displayVideoCodec.contains("AVC", ignoreCase = true) ||
                stream.displayVideoCodec.contains("H.264", ignoreCase = true) ||
                combined.contains("x264") ||
                combined.contains("avc") ||
                combined.contains("h.264") ||
                combined.contains("h264")
    }

    fun isAv1Stream(stream: StreamItem): Boolean {
        val t = (stream.title ?: "").lowercase()
        val n = (stream.name ?: "").lowercase()
        val combined = "$n $t"
        return stream.displayVideoCodec.contains("AV1", ignoreCase = true) ||
                combined.contains("av1") ||
                combined.contains("av01")
    }

    /**
     * Determines whether this stream can be played comfortably on device hardware.
     */
    fun isStreamHardwareCompatible(stream: StreamItem, context: android.content.Context? = null): Boolean {
        val stream4k = is4kStream(stream)
        val streamHevc = isHevcStream(stream)
        val streamAv1 = isAv1Stream(stream)
        val isLowEnd = context?.let { isLowEndDevice(it) } ?: false

        // Low-end devices often struggle with 4K or HEVC even if hardware claim support
        if (isLowEnd && stream4k) return false
        if (isLowEnd && streamHevc) return false

        if (stream4k && !is4KSupported()) return false
        if (streamHevc && !isHardwareHevcSupported()) return false
        if (stream4k && streamHevc && !isHardware4kHevcSupported()) return false
        if (streamAv1 && !isHardwareAv1Supported()) return false

        return true
    }

    /**
     * Selects the most optimal and hardware-compatible stream.
     * If the device lacks 4K or HEVC hardware decoding, automatically selects
     * an H.264 / 1080p (or 720p) stream by default to prevent crashes and error screens.
     */
    fun selectBestCompatibleStream(streams: List<StreamItem>, context: android.content.Context? = null): StreamItem? {
        val playable = streams.filter { it.isPlayable }
        if (playable.isEmpty()) return streams.firstOrNull()
        if (playable.size == 1) return playable.first()

        val hwHevc = isHardwareHevcSupported()
        val hw4k = is4KSupported()
        val hw4kHevc = isHardware4kHevcSupported()
        val isLowEnd = context?.let { isLowEndDevice(it) } ?: false

        fun scoreStream(stream: StreamItem): Int {
            val stream4k = is4kStream(stream)
            val streamHevc = isHevcStream(stream)
            val streamH264 = isH264Stream(stream)
            val streamAv1 = isAv1Stream(stream)
            val quality = stream.displayQuality

            var score = 0

            val isHardwareCompatible = when {
                stream4k && streamHevc -> hw4kHevc
                stream4k -> hw4k
                streamHevc -> hwHevc
                streamAv1 -> isHardwareAv1Supported()
                else -> true // H.264 is universally hardware-supported
            }

            if (isHardwareCompatible) {
                score += 2000
                // Penalty for HEVC/4K on low-end even if hardware claims to support it
                if (isLowEnd && (streamHevc || stream4k)) {
                    score -= 1000 
                }
            } else {
                // Large penalty to avoid crashing low-end devices
                score -= 4000 
                if (stream4k) score -= 2000 // 4K software decode is catastrophic on budget phones
                if (streamHevc && isLowEnd) score -= 1000 // HEVC software is also heavy
            }

            if (hw4k && (hwHevc || !streamHevc) && !isLowEnd) {
                // High-end device: rank 4K > 1080p > 720p > 480p
                when (quality) {
                    "4K UHD" -> score += 600
                    "1080p" -> score += 400
                    "720p" -> score += 200
                    "480p" -> score += 100
                    else -> score += 50
                }
            } else {
                // Lower-end or budget device without 4K / HEVC hardware:
                // 1080p H.264 (AVC) is the gold standard of smooth zero-lag playback!
                when (quality) {
                    "1080p" -> score += if (streamH264) 1000 else 300
                    "720p" -> score += if (streamH264) 800 else 250
                    "480p" -> score += if (streamH264) 400 else 150
                    "4K UHD" -> score -= 2000 // Incompatible resolution
                    else -> score += 100
                }
            }

            // Strong preference for standard H.264 on low-end
            if (!hwHevc || isLowEnd) {
                if (streamH264) score += 700
                if (streamHevc) score -= 600
            }

            // Reasonable file size preference (under 4GB is safer on mobile memory)
            val bytes = stream.parsedSizeBytes
            if (bytes in 500_000_000L..4_500_000_000L) {
                score += 100
            }

            return score
        }

        return playable.maxByOrNull { scoreStream(it) } ?: playable.first()
    }
}
