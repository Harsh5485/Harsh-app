@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.AudioManager
import androidx.core.content.edit
import androidx.core.net.toUri
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.ffmpeg.FfmpegAudioRenderer
import androidx.media3.decoder.ffmpeg.FfmpegLibrary
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mkv.MatroskaExtractor
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.audio.AudioCapabilities
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.video.VideoRendererEventListener
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.StreamItem
import com.example.util.StreamUrlHelper
import com.example.util.DeviceCodecHelper
import com.example.ui.player.AppAudioTrack
import com.example.ui.player.AppSubtitleTrack
import com.example.ui.player.AppVideoTrack
import com.example.ui.player.PlayerTrackHelper
import com.example.ui.player.MediaCodecInfo
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCard
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioPlayerOverlay
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSecondary
import com.example.ui.theme.NuvioSuccess
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.PlayerPlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class PlayerSheetType {
    NONE,
    AUDIO,
    SUBTITLES,
    STREAMS,
    CODECS_INFO,
    SPEED
}

private enum class ScreenOrientationMode(val label: String, val orientation: Int) {
    LANDSCAPE("Landscape Locked", ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
    PORTRAIT("Portrait Mode", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    AUTO("Auto-Rotate (Sensor)", ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)
}

@Composable
fun PlayerScreen(
    playbackState: PlayerPlaybackState,
    onBackClick: () -> Unit,
    onSaveProgress: (positionMs: Long, durationMs: Long) -> Unit,
    onNextClick: (() -> Unit)? = null,
    onSwitchStream: ((StreamItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val currentPlaybackState by rememberUpdatedState(playbackState)
    val currentOnNextClick by rememberUpdatedState(onNextClick)
    val currentOnSaveProgress by rememberUpdatedState(onSaveProgress)
    val currentOnBackClick by rememberUpdatedState(onBackClick)

    // Device Capability Check: If device does not support HEVC or 4K hardware decoding,
    // automatically select the most compatible stream (like H.264 / 1080p) by default to prevent crashes
    var activeStreamItem by remember(playbackState.mediaId, playbackState.season, playbackState.episode, playbackState.streamItem?.url) {
        val initial = playbackState.streamItem
        val streams = playbackState.availableStreams
        val selected = if (streams.isNotEmpty() && (initial == null || !DeviceCodecHelper.isStreamHardwareCompatible(initial, context))) {
            DeviceCodecHelper.selectBestCompatibleStream(streams, context) ?: initial
        } else {
            initial
        }
        mutableStateOf(selected)
    }
    val rawUrl = activeStreamItem?.url ?: playbackState.streamItem?.url ?: return
    val streamUrl = StreamUrlHelper.toWorkerProxyUrl(rawUrl) ?: rawUrl

    // Session id counter: used to guarantee complete player & codec recreation from scratch on source switch
    var playerSessionId by remember { mutableIntStateOf(0) }

    var isPlaying by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var isSeeking by remember { mutableStateOf(false) }
    var currentPosMs by remember { mutableLongStateOf(playbackState.initialPositionMs) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var showUpNext by remember { mutableStateOf(false) }
    var upNextTimer by remember { mutableIntStateOf(10) }
    var playerError by remember { mutableStateOf<String?>(null) }
    val failedStreamUrls = remember { mutableStateListOf<String>() }

    // Screen rotation state (Auto rotate to Landscape when entering playback)
    var orientationMode by remember { mutableStateOf(ScreenOrientationMode.LANDSCAPE) }
    var orientationFeedback by remember { mutableStateOf<String?>(null) }

    // Aspect ratio: FIT, ZOOM, FILL
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Audio and Subtitle tracks
    val audioTracks = remember { mutableStateListOf<AppAudioTrack>() }
    val subtitleTracks = remember { mutableStateListOf<AppSubtitleTrack>() }

    // Bottom sheets
    var activeSheet by remember { mutableStateOf(PlayerSheetType.NONE) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Double tap feedback
    var doubleTapFeedback by remember { mutableStateOf<String?>(null) }

    // Gesture feedback (Volume / Brightness / Seek)
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    var gestureFeedbackText by remember { mutableStateOf<String?>(null) }
    var gestureFeedbackIcon by remember { mutableStateOf<ImageVector?>(null) }
    var gestureProgress by remember { mutableFloatStateOf(0f) }
    var isGestureLeftSide by remember { mutableStateOf(true) }

    // Seek gesture state
    var isSeekingByGesture by remember { mutableStateOf(false) }
    var seekGestureTargetMs by remember { mutableLongStateOf(0L) }
    var seekDeltaText by remember { mutableStateOf("") }

    // Gesture feedback auto-hide timer
    LaunchedEffect(gestureFeedbackText, isSeekingByGesture) {
        if (gestureFeedbackText != null && !isSeekingByGesture) {
            delay(1200)
            gestureFeedbackText = null
        }
    }

    // Auto rotate to landscape on launch and enforce TRUE immersive fullscreen (hiding timer, wifi, status bar icons)
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        val window = activity?.window
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val window = activity?.window
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Keep status bar (wifi, clock/timer, battery) permanently hidden so they never disturb the user
    LaunchedEffect(controlsVisible) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Configure Software Decoder Fallback: ensures software decoders (like c2.android.hevc.decoder, OMX.google.hevc.decoder)
    // are included and prioritized on hardware failure, preventing crashes on low-end devices.
    val fallbackMediaCodecSelector = remember {
        MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
            val decoders = ArrayList<androidx.media3.exoplayer.mediacodec.MediaCodecInfo>()

            val primary = runCatching {
                MediaCodecUtil.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingDecoder)
            }.getOrDefault(emptyList())
            decoders.addAll(primary)

            if (requiresSecureDecoder || requiresTunnelingDecoder) {
                val standard = runCatching {
                    MediaCodecUtil.getDecoderInfos(mimeType, false, false)
                }.getOrDefault(emptyList())
                for (info in standard) {
                    if (!decoders.any { it.name == info.name }) {
                        decoders.add(info)
                    }
                }
            }

            // Always ensure software decoders are available as backup if hardware fails
            val allDecoders = runCatching {
                MediaCodecUtil.getDecoderInfos(mimeType, false, false)
            }.getOrDefault(emptyList())

            val softwareDecoders = allDecoders.filter { info ->
                !info.hardwareAccelerated ||
                info.name.startsWith("c2.android.", ignoreCase = true) ||
                info.name.startsWith("OMX.google.", ignoreCase = true) ||
                info.name.contains(".sw.", ignoreCase = true) ||
                info.name.contains("software", ignoreCase = true)
            }

            for (sw in softwareDecoders) {
                if (!decoders.any { it.name == sw.name }) {
                    decoders.add(sw)
                }
            }

            decoders
        }
    }

    // ExoPlayer instance: re-created cleanly when playerSessionId increments on source switch
    val exoPlayer: androidx.media3.exoplayer.ExoPlayer = remember(playerSessionId) {
        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildVideoRenderers(
                context: Context,
                extensionRendererMode: Int,
                mediaCodecSelector: MediaCodecSelector,
                enableDecoderFallback: Boolean,
                eventHandler: Handler,
                eventListener: VideoRendererEventListener,
                allowedVideoJoiningTimeMs: Long,
                out: ArrayList<Renderer>
            ) {
                super.buildVideoRenderers(
                    context,
                    extensionRendererMode,
                    fallbackMediaCodecSelector,
                    /* enableDecoderFallback = */ true,
                    eventHandler,
                    eventListener,
                    allowedVideoJoiningTimeMs,
                    out
                )
            }

            override fun buildAudioRenderers(
                context: Context,
                extensionRendererMode: Int,
                mediaCodecSelector: MediaCodecSelector,
                enableDecoderFallback: Boolean,
                audioSink: AudioSink,
                eventHandler: Handler,
                eventListener: AudioRendererEventListener,
                out: ArrayList<Renderer>
            ) {
                // Add native FFmpeg software decoder FIRST so that Dolby Digital Plus (DDP 5.1 / E-AC-3)
                // and AC-3/DTS/TrueHD audio streams decode directly to Stereo PCM without phone AudioTrack failures
                runCatching {
                    if (FfmpegLibrary.isAvailable()) {
                        val ffmpegRenderer = FfmpegAudioRenderer(eventHandler, eventListener, audioSink).apply {
                            setAudioOutputChannels("stereo", 2)
                            setDownmixNormalizationEnabled(true)
                        }
                        out.add(ffmpegRenderer)
                    }
                }.onFailure { e ->
                    android.util.Log.w("PlayerScreen", "FFmpeg renderer fallback: ${e.message}")
                }

                // Also add standard platform MediaCodec renderers (AAC, MP3, Opus, etc.)
                super.buildAudioRenderers(
                    context,
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF,
                    fallbackMediaCodecSelector,
                    /* enableDecoderFallback = */ true,
                    audioSink,
                    eventHandler,
                    eventListener,
                    out
                )
            }
        }.apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            setEnableDecoderFallback(true)
            setMediaCodecSelector(fallbackMediaCodecSelector)
        }

        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setForceHighestSupportedBitrate(false)
            )
        }

        // Fast startup and smooth buffering: prioritizes time over size thresholds so playback starts immediately
        // without waiting to buffer 50MB+ over mobile data
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 2_500,
                /* maxBufferMs = */ 30_000,
                /* bufferForPlaybackMs = */ 500,
                /* bufferForPlaybackAfterRebufferMs = */ 1_000
            )
            .setBackBuffer(
                /* backBufferDurationMs = */ 5_000,
                /* retainBackBufferFromKeyframe = */ true
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
            .setConnectTimeoutMs(25_000)
            .setReadTimeoutMs(40_000)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
            .setDefaultRequestProperties(
                mapOf(
                    "Accept" to "*/*",
                    "Connection" to "keep-alive"
                )
            )

        val defaultDataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val extractorsFactory = DefaultExtractorsFactory().apply {
            setMatroskaExtractorFlags(0)
            setMp4ExtractorFlags(Mp4Extractor.FLAG_WORKAROUND_IGNORE_EDIT_LISTS)
            setConstantBitrateSeekingEnabled(true)
        }

        val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory, extractorsFactory)

        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(androidx.media3.common.AudioAttributes.DEFAULT, true)
            .setSeekParameters(androidx.media3.exoplayer.SeekParameters.CLOSEST_SYNC)
            .build()
    }

    // Completely releases the previous media item, clears surface/codec state, and re-initializes from scratch
    val switchStreamWithFullReset: (StreamItem) -> Unit = { newStream: StreamItem ->
        val currentPos = if (exoPlayer.currentPosition > 0L) exoPlayer.currentPosition else currentPosMs
        currentPosMs = currentPos

        // Completely release previous media item, surface and codec state
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.clearVideoSurface()

        activeStreamItem = newStream
        playerError = null
        isLoading = true
        playerSessionId++ // triggers fresh re-initialization of ExoPlayer from scratch!
        onSwitchStream?.invoke(newStream)
    }

    // Track the current episode/item to distinguish between quality switch and next episode
    val currentVideoKey = remember(playbackState.mediaId, playbackState.season, playbackState.episode) {
        "${playbackState.mediaId}_S${playbackState.season}_E${playbackState.episode}"
    }
    var lastVideoKey by remember { mutableStateOf<String?>(null) }

    // Handle stream changes (initial load, quality switch, or next episode)
    LaunchedEffect(playbackState.mediaId, playbackState.season, playbackState.episode, streamUrl, playerSessionId) {
        val isNewEpisode = lastVideoKey == null || currentVideoKey != lastVideoKey
        lastVideoKey = currentVideoKey
        if (isNewEpisode) {
            failedStreamUrls.clear()
        }
        
        isLoading = true
        playerError = null
        showUpNext = false
        upNextTimer = 15
        
        // Stop previous playback and clear items to guarantee a fresh state without inheriting previous episode position
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        
        // Differentiate: Episode switch (reset to initial) vs Quality switch (maintain current)
        val targetPos = if (isNewEpisode) {
            playbackState.initialPositionMs.coerceAtLeast(0L)
        } else {
            currentPosMs.coerceAtLeast(0L)
        }
        
        // Reset UI progress states immediately for clean visual transition
        currentPosMs = targetPos
        totalDurationMs = 0L // Reset duration; will be updated once new metadata is ready
        isSeeking = false // Reset seeking state for new load
        
        val parsedUri = try {
            if (streamUrl.startsWith("/") || streamUrl.startsWith("file://") || streamUrl.startsWith("content://")) {
                if (streamUrl.startsWith("/")) java.io.File(streamUrl).toUri() else streamUrl.toUri()
            } else {
                streamUrl.toUri()
            }
        } catch (e: Exception) {
            streamUrl.toUri()
        }
        
        val cleanUrl = streamUrl.substringBefore('?').lowercase()
        val mediaItemBuilder = androidx.media3.common.MediaItem.Builder().setUri(parsedUri)
        if (cleanUrl.endsWith(".m3u8") || streamUrl.contains(".m3u8", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
        } else if (cleanUrl.endsWith(".mpd") || streamUrl.contains(".mpd", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_MPD)
        } else if (cleanUrl.endsWith(".mkv") || streamUrl.contains(".mkv", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.VIDEO_MATROSKA)
        } else if (cleanUrl.endsWith(".mp4") || streamUrl.contains(".mp4", ignoreCase = true)) {
            mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.VIDEO_MP4)
        }
        val mediaItem = mediaItemBuilder.build()

        exoPlayer.setMediaItem(mediaItem, targetPos)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    // Track listeners & updates
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val d = player.duration
                if (d > 0 && d != androidx.media3.common.C.TIME_UNSET) {
                    totalDurationMs = d
                }
                val pos = player.currentPosition
                if (pos >= 0 && !isSeeking) {
                    currentPosMs = pos
                }
            }

            override fun onRenderedFirstFrame() {
                isLoading = false
                isSeeking = false
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    val p = newPosition.positionMs
                    if (p >= 0L) {
                        currentPosMs = p
                    }
                    isSeeking = false
                    isLoading = false
                }
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                val d = exoPlayer.duration
                if (d > 0 && d != androidx.media3.common.C.TIME_UNSET) {
                    totalDurationMs = d
                }
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                val d = exoPlayer.duration
                if (d > 0 && d != androidx.media3.common.C.TIME_UNSET) {
                    totalDurationMs = d
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                isLoading = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    isLoading = false
                    isSeeking = false
                    val d = exoPlayer.duration
                    if (d > 0 && d != androidx.media3.common.C.TIME_UNSET) {
                        totalDurationMs = d
                    }
                    playerError = null
                    if (exoPlayer.playWhenReady && !exoPlayer.isPlaying) {
                        exoPlayer.play()
                    }
                }
                if (state == Player.STATE_ENDED) {
                    if (currentPlaybackState.nextEpisode != null) {
                        showUpNext = false
                        currentOnSaveProgress(exoPlayer.duration, exoPlayer.duration)
                        currentOnNextClick?.invoke()
                    } else {
                        currentOnBackClick()
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                isLoading = false
                val failedUrl = activeStreamItem?.url
                if (failedUrl != null && !failedStreamUrls.contains(failedUrl)) {
                    failedStreamUrls.add(failedUrl)
                }
                android.util.Log.e("PlayerScreen", "ExoPlayer Error: ${error.errorCodeName} (cause: ${error.cause?.message})", error)
                
                // If a hardware codec failure occurred or stream failed, switch to most compatible stream (e.g. 1080p H.264)
                val currentStreams = playbackState.availableStreams
                val nextStream = DeviceCodecHelper.selectBestCompatibleStream(
                    currentStreams.filter { it.url != null && !failedStreamUrls.contains(it.url) },
                    context
                )
                if (nextStream != null && nextStream.url != null) {
                    val isCodecFailure = error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
                            error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED
                    val toastMsg = if (isCodecFailure) {
                        "Codec not supported on hardware. Switching to compatible 1080p stream..."
                    } else {
                        "Stream failed, trying alternate source..."
                    }
                    android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_SHORT).show()
                    switchStreamWithFullReset(nextStream)
                    return
                }

                val msg = when (error.errorCode) {
                    androidx.media3.common.PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
                    androidx.media3.common.PlaybackException.ERROR_CODE_DECODING_FAILED -> "Video/audio codec not supported by this phone's hardware"
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Network connection timeout: server is slow to respond"
                    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
                    androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED -> "Unsupported media stream format"
                    else -> error.localizedMessage ?: "Playback failed. Please try another source."
                }
                playerError = msg
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    isLoading = false
                    val d = exoPlayer.duration
                    if (d > 0 && d != androidx.media3.common.C.TIME_UNSET) {
                        totalDurationMs = d
                    }
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                audioTracks.clear()
                audioTracks.addAll(PlayerTrackHelper.extractAudioTracks(exoPlayer))
                subtitleTracks.clear()
                subtitleTracks.addAll(PlayerTrackHelper.extractSubtitleTracks(exoPlayer))
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            val p = exoPlayer.currentPosition
            val d = exoPlayer.duration.coerceAtLeast(0L)
            if (p > 0L) {
                currentOnSaveProgress(p, d)
            }
            exoPlayer.removeListener(listener)
            // Completely release player, clear surface and native codec allocations
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.clearVideoSurface()
            exoPlayer.release()
        }
    }

    // Periodic time tick & progress save every 5 seconds + Up Next check
    LaunchedEffect(playbackState.mediaId, playbackState.season, playbackState.episode) {
        var tick = 0
        while (true) {
            delay(800)
            val pos = exoPlayer.currentPosition
            val dur = exoPlayer.duration
            if (pos >= 0 && !isSeeking) {
                currentPosMs = pos
            }
            if (dur > 0 && dur != androidx.media3.common.C.TIME_UNSET) {
                totalDurationMs = dur
            }
            
            if (exoPlayer.isPlaying) {
                isLoading = false
                // Up Next logic: last 25 seconds of video
                val remaining = totalDurationMs - currentPosMs
                if (currentPlaybackState.nextEpisode != null && totalDurationMs > 30000L && remaining in 1..25000L) {
                    if (!showUpNext) {
                        showUpNext = true
                        upNextTimer = 15
                    }
                } else if (remaining > 25000L || remaining <= 0L) {
                    showUpNext = false
                }
                
                tick++
                if (tick % 5 == 0 && totalDurationMs > 0) {
                    currentOnSaveProgress(currentPosMs, totalDurationMs)
                }
            }
        }
    }
    
    // Up Next countdown
    LaunchedEffect(showUpNext) {
        if (showUpNext) {
            while (upNextTimer > 0) {
                delay(1000)
                upNextTimer--
            }
            // Auto play when timer hits 0 if still showing
            if (showUpNext) {
                showUpNext = false
                currentOnNextClick?.invoke()
            }
        }
    }

    // Auto-hide controls after 4.5 seconds
    LaunchedEffect(controlsVisible, isPlaying, isScreenLocked) {
        if (controlsVisible && isPlaying && !isScreenLocked) {
            delay(4500)
            controlsVisible = false
        }
    }

    // Double tap feedback timer
    LaunchedEffect(doubleTapFeedback) {
        if (doubleTapFeedback != null) {
            delay(750)
            doubleTapFeedback = null
        }
    }

    // Orientation change feedback timer
    LaunchedEffect(orientationFeedback) {
        if (orientationFeedback != null) {
            delay(1200)
            orientationFeedback = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isScreenLocked) {
                detectTapGestures(
                    onTap = {
                        gestureFeedbackText = null
                        controlsVisible = !controlsVisible
                    },
                    onDoubleTap = { offset ->
                        gestureFeedbackText = null
                        if (!isScreenLocked) {
                            val screenWidth = size.width
                            if (offset.x < screenWidth * 0.4f) {
                                // Rewind 10s
                                isSeeking = true
                                isLoading = true
                                val basePos = if (currentPosMs > 0L) currentPosMs else exoPlayer.currentPosition.coerceAtLeast(0L)
                                val newPos = (basePos - 10000L).coerceAtLeast(0L)
                                currentPosMs = newPos
                                exoPlayer.seekTo(newPos)
                                doubleTapFeedback = "-10s"
                            } else if (offset.x > screenWidth * 0.6f) {
                                // Forward 10s
                                isSeeking = true
                                isLoading = true
                                val basePos = if (currentPosMs > 0L) currentPosMs else exoPlayer.currentPosition.coerceAtLeast(0L)
                                val maxDur = if (totalDurationMs > 0L) totalDurationMs else if (exoPlayer.duration > 0L) exoPlayer.duration else Long.MAX_VALUE
                                val newPos = (basePos + 10000L).coerceIn(0L, maxDur)
                                currentPosMs = newPos
                                exoPlayer.seekTo(newPos)
                                doubleTapFeedback = "+10s"
                            } else {
                                // Center double tap: play/pause
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                        }
                    }
                )
            }
            .pointerInput(isScreenLocked) {
                if (isScreenLocked) return@pointerInput
                
                var startVal = 0f
                var isVerticalDrag = false
                var isHorizontalDrag = false
                var hasDecidedDirection = false
                var dragStartPosMs = 0L

                detectDragGestures(
                    onDragStart = { offset ->
                        hasDecidedDirection = false
                        isVerticalDrag = false
                        isHorizontalDrag = false
                        isGestureLeftSide = offset.x < (size.width.toFloat() / 2f)
                        dragStartPosMs = exoPlayer.currentPosition
                        
                        if (isGestureLeftSide) {
                            val window = activity?.window
                            val lp = window?.attributes
                            startVal = if (lp != null && lp.screenBrightness >= 0f) lp.screenBrightness else 0.5f
                        } else {
                            val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                            val currVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 8
                            startVal = if (maxVol > 0) currVol.toFloat() / maxVol.toFloat() else 0.5f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (change.isConsumed) return@detectDragGestures
                        change.consume()
                        
                        if (!hasDecidedDirection) {
                            if (kotlin.math.abs(dragAmount.y) > kotlin.math.abs(dragAmount.x)) {
                                isVerticalDrag = true
                            } else {
                                isHorizontalDrag = true
                                isSeekingByGesture = true
                            }
                            hasDecidedDirection = true
                        }

                        if (isVerticalDrag) {
                            val height = size.height.toFloat().coerceAtLeast(1f)
                            val delta = -dragAmount.y / (height * 0.5f)
                            startVal = (startVal + delta).coerceIn(0f, 1f)

                            if (isGestureLeftSide) {
                                val window = activity?.window
                                val lp = window?.attributes
                                if (window != null && lp != null) {
                                    lp.screenBrightness = startVal.coerceIn(0.01f, 1.0f)
                                    window.attributes = lp
                                }
                                gestureProgress = startVal
                                gestureFeedbackText = "${(startVal * 100).toInt()}%"
                                gestureFeedbackIcon = Icons.Default.BrightnessHigh
                            } else {
                                val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                                val newVolIndex = (startVal * maxVol).toInt().coerceIn(0, maxVol)
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVolIndex, 0)
                                gestureProgress = startVal
                                gestureFeedbackText = "${(startVal * 100).toInt()}%"
                                gestureFeedbackIcon = if (newVolIndex == 0) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp
                            }
                        } else if (isHorizontalDrag) {
                            val width = size.width.toFloat().coerceAtLeast(1f)
                            // Seek sensitivity: 1/2 screen width = 60 seconds
                            val seekRangeMs = 120000L 
                            val deltaMs = ((dragAmount.x / width) * seekRangeMs).toLong()
                            
                            val maxDur = if (totalDurationMs > 0L) totalDurationMs else if (exoPlayer.duration > 0L) exoPlayer.duration else Long.MAX_VALUE
                            val target = if (seekGestureTargetMs == 0L) (dragStartPosMs + deltaMs) else (seekGestureTargetMs + deltaMs)
                            seekGestureTargetMs = target.coerceIn(0L, maxDur)
                            
                            val diff = seekGestureTargetMs - dragStartPosMs
                            val sign = if (diff >= 0) "+" else "-"
                            seekDeltaText = "$sign${formatTimestamp(kotlin.math.abs(diff))}"
                        }
                    },
                    onDragEnd = {
                        if (isHorizontalDrag && seekGestureTargetMs >= 0L) {
                            isSeeking = true
                            isLoading = true
                            currentPosMs = seekGestureTargetMs
                            exoPlayer.seekTo(seekGestureTargetMs)
                        }
                        isSeekingByGesture = false
                        seekGestureTargetMs = 0L
                        seekDeltaText = ""
                        hasDecidedDirection = false
                    },
                    onDragCancel = {
                        isSeekingByGesture = false
                        seekGestureTargetMs = 0L
                        seekDeltaText = ""
                        hasDecidedDirection = false
                    }
                )
            }
            .testTag("player_screen_container")
    ) {
        // Video View with SubtitleView embedded
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                if (playerView.player !== exoPlayer) {
                    playerView.player = null
                    playerView.player = exoPlayer
                }
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading / Buffering spinner (shown when buffering or seeking)
        if (isLoading || isSeeking) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = NuvioPrimary,
                            modifier = Modifier.size(46.dp),
                            strokeWidth = 3.5.dp
                        )
                    }
                }
            }
        }

        // Horizontal Seek Preview Overlay (The "choti si screen" feel)
        AnimatedVisibility(
            visible = isSeekingByGesture,
            enter = fadeIn() + androidx.compose.animation.scaleIn(),
            exit = fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.85f),
                    border = BorderStroke(2.dp, NuvioPrimary),
                    modifier = Modifier
                        .width(240.dp)
                        .height(135.dp) // 16:9 aspect ratio box
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Blurred placeholder image to simulate "video slip" animation
                        AsyncImage(
                            model = playbackState.poster ?: "",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alpha = 0.3f,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (seekDeltaText.startsWith("+")) Icons.Default.FastForward else Icons.Default.FastRewind,
                                contentDescription = null,
                                tint = NuvioPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = seekDeltaText,
                                color = NuvioPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTimestamp(seekGestureTargetMs),
                            color = NuvioTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (totalDurationMs > 0) " / ${formatTimestamp(totalDurationMs)}" else " / --:--",
                            color = NuvioTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Double-tap visual feedback badge
        AnimatedVisibility(
            visible = doubleTapFeedback != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NuvioPrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if (doubleTapFeedback == "-10s") Icons.Default.FastRewind else Icons.Default.FastForward,
                        contentDescription = null,
                        tint = NuvioPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = doubleTapFeedback ?: "",
                        color = NuvioTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Screen rotation visual feedback badge
        AnimatedVisibility(
            visible = orientationFeedback != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NuvioPrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = NuvioPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = orientationFeedback ?: "",
                        color = NuvioTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Gesture feedback overlay badge (Volume on Right & Brightness on Left)
        AnimatedVisibility(
            visible = gestureFeedbackText != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier
                .align(if (isGestureLeftSide) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 48.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.15f), // Ultra transparent glass
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    gestureFeedbackIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = NuvioPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        text = gestureFeedbackText ?: "",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { gestureProgress },
                        modifier = Modifier
                            .width(4.dp)
                            .height(80.dp)
                            .clip(CircleShape),
                        color = NuvioPrimary,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )
                }
            }
        }

        // Up Next Overlay (Bottom Right)
        AnimatedVisibility(
            visible = showUpNext && playbackState.nextEpisode != null,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = if (controlsVisible) 100.dp else 0.dp) // Lift above controls
        ) {
            val next = playbackState.nextEpisode!!
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                border = BorderStroke(1.5.dp, NuvioPrimary.copy(alpha = 0.6f)),
                modifier = Modifier.width(260.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(NuvioPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = upNextTimer.toString(),
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Next Episode",
                            color = NuvioPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "E${next.episode}: ${next.title ?: "Episode ${next.episode}"}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { 
                            showUpNext = false
                            currentOnNextClick?.invoke() 
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Play Now", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Player Error Overlay
        AnimatedVisibility(
            visible = playerError != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Playback Failed",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = playerError ?: "Unknown Error",
                        color = NuvioTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                playerError = null
                                isLoading = true
                                // Cleanly reset player state and re-initialize from scratch
                                exoPlayer.stop()
                                exoPlayer.clearMediaItems()
                                exoPlayer.clearVideoSurface()
                                playerSessionId++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NuvioPrimary, contentColor = Color.Black)
                        ) {
                            Text("Retry")
                        }
                        if (playbackState.availableStreams.size > 1) {
                            Button(
                                onClick = {
                                    val other = DeviceCodecHelper.selectBestCompatibleStream(
                                        playbackState.availableStreams.filter { it.url != activeStreamItem?.url },
                                        context
                                    ) ?: playbackState.availableStreams.firstOrNull { it.url != activeStreamItem?.url }
                                    if (other != null) {
                                        switchStreamWithFullReset(other)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NuvioSecondary, contentColor = Color.Black)
                            ) {
                                Text("Switch Source")
                            }
                        }
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }

        // Screen Lock Indicator & Unlock Button
        if (isScreenLocked) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(24.dp)
            ) {
                IconButton(
                    onClick = { isScreenLocked = false },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        .border(1.5.dp, NuvioPrimary, CircleShape)
                        .testTag("player_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlock screen",
                        tint = NuvioPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            return@Box
        }

        // Main Player Controls Overlay
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NuvioPlayerOverlay)
            ) {
                // Top Header Row (True Immersive layout without status bar padding disturbance)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                onSaveProgress(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0L))
                                onBackClick()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .testTag("player_back_action")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NuvioTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = playbackState.title,
                                color = NuvioTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val subInfo = if (playbackState.season != null && playbackState.episode != null) {
                                "S${playbackState.season}:E${playbackState.episode} • ${playbackState.episodeTitle ?: ""}"
                            } else {
                                activeStreamItem?.displaySource ?: "Server Stream"
                            }
                            Text(
                                text = subInfo,
                                color = NuvioTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top Quick Action Icons: Codecs Tool, Audio, Subtitle, Rotate, Lock (VLC/External removed)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Nuvio Codecs Tool & Media Stats Button
                        IconButton(
                            onClick = { activeSheet = PlayerSheetType.CODECS_INFO },
                            modifier = Modifier.testTag("player_codecs_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Harsh Codecs & Stats Tool",
                                tint = NuvioPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Audio Track Selector Button
                        IconButton(
                            onClick = { activeSheet = PlayerSheetType.AUDIO },
                            modifier = Modifier.testTag("player_audio_tracks_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = "Audio Tracks",
                                tint = if (audioTracks.isNotEmpty()) NuvioPrimary else NuvioTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Subtitles Selector Button
                        IconButton(
                            onClick = { activeSheet = PlayerSheetType.SUBTITLES },
                            modifier = Modifier.testTag("player_subtitle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ClosedCaption,
                                contentDescription = "Subtitles",
                                tint = if (subtitleTracks.any { it.isSelected }) NuvioPrimary else NuvioTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Screen Rotation Toggle (Landscape, Portrait, Auto-Rotate Sensor)
                        IconButton(
                            onClick = {
                                orientationMode = when (orientationMode) {
                                    ScreenOrientationMode.LANDSCAPE -> ScreenOrientationMode.PORTRAIT
                                    ScreenOrientationMode.PORTRAIT -> ScreenOrientationMode.AUTO
                                    ScreenOrientationMode.AUTO -> ScreenOrientationMode.LANDSCAPE
                                }
                                activity?.requestedOrientation = orientationMode.orientation
                                orientationFeedback = orientationMode.label
                            },
                            modifier = Modifier.testTag("player_rotate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = "Rotate Screen",
                                tint = when (orientationMode) {
                                    ScreenOrientationMode.LANDSCAPE -> NuvioPrimary
                                    ScreenOrientationMode.PORTRAIT -> NuvioSecondary
                                    ScreenOrientationMode.AUTO -> NuvioSuccess
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Screen Lock Button
                        IconButton(
                            onClick = { isScreenLocked = true },
                            modifier = Modifier.testTag("player_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Lock Screen",
                                tint = NuvioTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Center Playback Controls: Rewind 10, Play/Pause/Buffering, Forward 10
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10
                    IconButton(
                        onClick = {
                            isSeeking = true
                            isLoading = true
                            val basePos = if (currentPosMs > 0L) currentPosMs else exoPlayer.currentPosition.coerceAtLeast(0L)
                            val newPos = (basePos - 10000L).coerceAtLeast(0L)
                            currentPosMs = newPos
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .testTag("player_seek_back_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = NuvioTextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Play / Pause / Buffering Spinner (shows buffering icon when forwarding/seeking instead of pause icon)
                    IconButton(
                        onClick = {
                            if (!isLoading && !isSeeking) {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(NuvioPrimary, CircleShape)
                            .testTag("player_toggle_playback")
                    ) {
                        if (isLoading || isSeeking) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    // Forward 10
                    IconButton(
                        onClick = {
                            isSeeking = true
                            isLoading = true
                            val basePos = if (currentPosMs > 0L) currentPosMs else exoPlayer.currentPosition.coerceAtLeast(0L)
                            val maxDur = if (totalDurationMs > 0L) totalDurationMs else if (exoPlayer.duration > 0L) exoPlayer.duration else Long.MAX_VALUE
                            val newPos = (basePos + 10000L).coerceIn(0L, maxDur)
                            currentPosMs = newPos
                            exoPlayer.seekTo(newPos)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .testTag("player_seek_forward_10")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = NuvioTextPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Bottom Controls Bar (Scrubber Slider, Timestamps, Aspect, Speed, Stream Sources)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    // Time Scrubber
                    Slider(
                        value = if (totalDurationMs > 0L) currentPosMs.toFloat().coerceIn(0f, totalDurationMs.toFloat()) else 0f,
                        onValueChange = {
                            isSeeking = true
                            currentPosMs = it.toLong().coerceIn(0L, if (totalDurationMs > 0L) totalDurationMs else Long.MAX_VALUE)
                        },
                        onValueChangeFinished = {
                            isSeeking = true
                            isLoading = true
                            exoPlayer.seekTo(currentPosMs)
                        },
                        valueRange = 0f..(totalDurationMs.toFloat().coerceAtLeast(1f)),
                        colors = SliderDefaults.colors(
                            thumbColor = NuvioPrimary,
                            activeTrackColor = NuvioPrimary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bottom Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val durationText = if (totalDurationMs > 0) formatTimestamp(totalDurationMs) else "--:--"
                        Text(
                            text = "${formatTimestamp(currentPosMs)} / $durationText",
                            color = NuvioTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Multiple Stream Sources (if available)
                            if (playbackState.availableStreams.size > 1) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NuvioSurfaceVariant,
                                    modifier = Modifier.clickable { activeSheet = PlayerSheetType.STREAMS }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HighQuality,
                                            contentDescription = null,
                                            tint = NuvioPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = activeStreamItem?.displayQuality ?: "Streams",
                                            color = NuvioTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Aspect Ratio Toggle (Fit / Zoom / Fill)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NuvioSurfaceVariant,
                                modifier = Modifier.clickable {
                                    resizeMode = when (resizeMode) {
                                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = null,
                                        tint = NuvioPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (resizeMode) {
                                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Zoom"
                                            AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Fill"
                                            else -> "Fit"
                                        },
                                        color = NuvioTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Playback Speed
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NuvioSurfaceVariant,
                                modifier = Modifier.clickable {
                                    playbackSpeed = when (playbackSpeed) {
                                        1.0f -> 1.25f
                                        1.25f -> 1.5f
                                        1.5f -> 2.0f
                                        2.0f -> 0.75f
                                        else -> 1.0f
                                    }
                                    exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = NuvioPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${playbackSpeed}x",
                                        color = NuvioTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheets for Audio / Subtitles / Streams
        if (activeSheet != PlayerSheetType.NONE) {
            ModalBottomSheet(
                onDismissRequest = { activeSheet = PlayerSheetType.NONE },
                sheetState = sheetState,
                containerColor = NuvioCard,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(NuvioTextSecondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                }
            ) {
                when (activeSheet) {
                    PlayerSheetType.AUDIO -> {
                        AudioTracksSheet(
                            tracks = audioTracks,
                            onSelect = { selected ->
                                PlayerTrackHelper.selectAudioTrack(exoPlayer, selected)
                                audioTracks.clear()
                                audioTracks.addAll(PlayerTrackHelper.extractAudioTracks(exoPlayer))
                                activeSheet = PlayerSheetType.NONE
                            }
                        )
                    }
                    PlayerSheetType.SUBTITLES -> {
                        SubtitleTracksSheet(
                            tracks = subtitleTracks,
                            onSelect = { selected ->
                                PlayerTrackHelper.selectSubtitleTrack(exoPlayer, selected)
                                subtitleTracks.clear()
                                subtitleTracks.addAll(PlayerTrackHelper.extractSubtitleTracks(exoPlayer))
                                activeSheet = PlayerSheetType.NONE
                            }
                        )
                    }
                    PlayerSheetType.STREAMS -> {
                        StreamsSheet(
                            streams = playbackState.availableStreams,
                            currentStream = activeStreamItem,
                            onSelect = { newStream ->
                                switchStreamWithFullReset(newStream)
                                activeSheet = PlayerSheetType.NONE
                            }
                        )
                    }
                    PlayerSheetType.CODECS_INFO -> {
                        val codecInfo = PlayerTrackHelper.extractCurrentMediaCodecInfo(exoPlayer, activeStreamItem)
                        CodecsInfoSheet(
                            info = codecInfo,
                            streamUrl = streamUrl,
                            onDismiss = { activeSheet = PlayerSheetType.NONE }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun AudioTracksSheet(
    tracks: List<AppAudioTrack>,
    onSelect: (AppAudioTrack) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 28.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Audiotrack,
                contentDescription = null,
                tint = NuvioPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Select Audio Language (${tracks.size})",
                color = NuvioTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (tracks.isEmpty()) {
            Text(
                text = "Single standard audio track active",
                color = NuvioTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tracks, key = { it.id }) { track ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (track.isSelected) NuvioSurfaceVariant else NuvioBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (track.isSelected) NuvioPrimary else NuvioCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(track) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = track.language,
                                    color = NuvioTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NuvioSurfaceVariant
                                    ) {
                                        Text(
                                            text = track.channels,
                                            color = NuvioSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NuvioSurfaceVariant
                                    ) {
                                        Text(
                                            text = track.codec,
                                            color = NuvioTextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (track.isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = NuvioPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubtitleTracksSheet(
    tracks: List<AppSubtitleTrack>,
    onSelect: (AppSubtitleTrack?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ClosedCaption,
                contentDescription = null,
                tint = NuvioPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Subtitles & Closed Captions",
                color = NuvioTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "Off" option
        val isNoneSelected = tracks.none { it.isSelected }
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNoneSelected) NuvioSurfaceVariant else NuvioBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isNoneSelected) NuvioPrimary else NuvioCardBorder,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onSelect(null) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subtitles Off",
                    color = NuvioTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isNoneSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = NuvioPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tracks, key = { it.id }) { track ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (track.isSelected) NuvioSurfaceVariant else NuvioBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (track.isSelected) NuvioPrimary else NuvioCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(track) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = track.language,
                                color = NuvioTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (track.label != track.language) {
                                Text(
                                    text = track.label,
                                    color = NuvioTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (track.isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = NuvioPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamsSheet(
    streams: List<StreamItem>,
    currentStream: StreamItem?,
    onSelect: (StreamItem) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HighQuality,
                contentDescription = null,
                tint = NuvioPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Switch Stream Quality / Source",
                color = NuvioTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(streams) { stream ->
                val isSelected = stream.url == currentStream?.url
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NuvioSurfaceVariant else NuvioBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isSelected) NuvioPrimary else NuvioCardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(stream) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NuvioPrimary
                                ) {
                                    Text(
                                        text = stream.displayQuality,
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NuvioSurfaceVariant
                                ) {
                                    Text(
                                        text = stream.displayVideoCodec,
                                        color = NuvioTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                val isHwSupported = DeviceCodecHelper.isStreamHardwareCompatible(stream, context)
                                if (!isHwSupported) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFFD32F2F).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "⚠️ No HW Dec",
                                            color = Color(0xFFFF8A80),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NuvioSuccess.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "✓ Compatible",
                                            color = NuvioSuccess,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (stream.formattedSize.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NuvioSurfaceVariant
                                    ) {
                                        Text(
                                            text = stream.formattedSize,
                                            color = NuvioTextSecondary,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stream.displayFileName,
                                color = NuvioTextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = NuvioPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    if (millis <= 0) return "00:00"
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun CodecsInfoSheet(
    info: MediaCodecInfo,
    streamUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = NuvioPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Harsh Codecs & Media Stats",
                color = NuvioTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Video Pipeline Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NuvioBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "VIDEO STREAM PIPELINE",
                    color = NuvioPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodecStatRow(label = "Video Codec", value = info.videoCodec)
                CodecStatRow(label = "Resolution", value = info.resolution)
                CodecStatRow(label = "Frame Rate", value = info.frameRate)
                CodecStatRow(label = "Color & Dynamic Range", value = info.colorDepth)
                CodecStatRow(label = "Bitrate", value = info.videoBitrate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Audio Pipeline Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NuvioBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AUDIO STREAM & DECODER",
                    color = NuvioSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodecStatRow(label = "Audio Codec", value = info.audioCodec)
                CodecStatRow(label = "Active Audio Decoder", value = info.activeDecoder)
                CodecStatRow(label = "Audio Channels", value = info.audioChannels)
                CodecStatRow(label = "Sample Rate", value = info.audioSampleRate)
                CodecStatRow(label = "Audio Bitrate", value = info.audioBitrate)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Container & Source Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NuvioBackground),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CONTAINER & NETWORK",
                    color = NuvioTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodecStatRow(label = "Container Format", value = info.containerFormat)
                CodecStatRow(label = "Stream Source", value = info.streamSource)
                CodecStatRow(label = "Buffer Health", value = info.bufferStatus)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Copy Stream URL Action Button
        androidx.compose.material3.Button(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Stream URL", streamUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Stream URL copied to clipboard", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(10.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = NuvioSurfaceVariant,
                contentColor = NuvioTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = NuvioPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Direct Stream URL", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CodecStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = NuvioTextSecondary, fontSize = 12.sp)
        Text(
            text = value,
            color = NuvioTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
