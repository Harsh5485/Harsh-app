package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import com.example.util.DeviceCodecHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.StreamItem
import com.example.data.model.VideoEpisode
import com.example.ui.components.formatRating
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCard
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioGold
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioPrimaryBright
import com.example.ui.theme.NuvioSecondary
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import android.view.KeyEvent
import com.example.ui.theme.NuvioSuccess
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    viewModel: NuvioViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsState()
    val mediaItem = detailState.mediaItem

    if (mediaItem == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(NuvioBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = NuvioPrimary,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading Details...",
                    color = NuvioTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Back Button
            var isBackFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(44.dp)
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .background(
                        if (isBackFocused) NuvioPrimary else Color.Black.copy(alpha = 0.65f),
                        CircleShape
                    )
                    .border(
                        width = if (isBackFocused) 3.dp else 1.dp,
                        color = if (isBackFocused) Color.White else NuvioCardBorder,
                        shape = CircleShape
                    )
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_CENTER,
                                KeyEvent.KEYCODE_ENTER,
                                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                    onBackClick()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
                    .focusable()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isBackFocused) Color.Black else NuvioTextPrimary
                )
            }
        }
        return
    }

    val meta = detailState.metaDetail
    val isSeries = mediaItem.type.equals("series", true)

    var selectedEpisode by remember { mutableStateOf<VideoEpisode?>(null) }
    var showSeasonPicker by remember { mutableStateOf(false) }
    var showEpisodeStreams by remember { mutableStateOf(false) }
    val seasonSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val episodeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val playButtonFocusRequester = remember { FocusRequester() }

    BackHandler(enabled = showSeasonPicker || showEpisodeStreams) {
        showSeasonPicker = false
        showEpisodeStreams = false
    }

    // Auto-request focus on Play button when Details screen opens
    androidx.compose.runtime.LaunchedEffect(mediaItem.id) {
        kotlinx.coroutines.delay(200)
        runCatching {
            playButtonFocusRequester.requestFocus()
        }
    }

    // Auto-refresh logic for streams
    androidx.compose.runtime.LaunchedEffect(mediaItem.id, detailState.selectedEpisode?.id) {
        while (true) {
            kotlinx.coroutines.delay(25000) // Poll every 25 seconds
            val targetId = if (isSeries && detailState.selectedEpisode != null) 
                detailState.selectedEpisode!!.id 
            else 
                mediaItem.id
            viewModel.loadStreams(mediaItem.type, targetId, silent = true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
    ) {
        // Main Content with Blur when loading
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (detailState.isLoadingStreams && detailState.streams.isEmpty()) 12.dp else 0.dp)
                .graphicsLayer {
                    alpha = if (detailState.isLoadingStreams && detailState.streams.isEmpty()) 0.6f else 1f
                }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Backdrop Header
                item {
                    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                    val screenHeight = configuration.screenHeightDp.dp
                    val headerHeight = (screenHeight * 0.38f).coerceIn(260.dp, 380.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(headerHeight)
                    ) {
                        AsyncImage(
                            model = meta?.background ?: mediaItem.background ?: mediaItem.poster,
                            contentDescription = mediaItem.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                    // Multi-layer immersive gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        NuvioBackground.copy(alpha = 0.9f),
                                        NuvioBackground
                                    )
                                )
                            )
                    )
                }
            }

            // Info Content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    // Premium Display Title
                    Text(
                        text = mediaItem.name.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp,
                        letterSpacing = (-0.4).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Polished Badges row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val rating = formatRating(meta?.imdbRating ?: mediaItem.imdbRating)
                        if (!rating.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = NuvioGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$rating IMDb",
                                    color = NuvioGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val year = meta?.year ?: meta?.releaseInfo ?: mediaItem.year
                        if (!year.isNullOrBlank()) {
                            Text(
                                text = year,
                                color = NuvioTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val runtime = meta?.runtime ?: mediaItem.runtime
                        if (!runtime.isNullOrBlank()) {
                            Text(
                                text = runtime,
                                color = NuvioTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NuvioPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NuvioPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isSeries) "SERIES" else "MOVIE",
                                color = NuvioPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Genres
                    val genresList = meta?.genres ?: mediaItem.genres
                    if (!genresList.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(genresList) { genre ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NuvioSurfaceVariant,
                                    modifier = Modifier.border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = genre,
                                        color = NuvioTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Play & Watchlist Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val context = LocalContext.current
                        val defaultStream = DeviceCodecHelper.selectBestCompatibleStream(detailState.streams, context) ?: detailState.streams.firstOrNull()
                        val qBadge = defaultStream?.displayQuality?.takeIf { it.isNotBlank() }?.let { "($it)" } ?: ""
                        var isPlayFocused by remember { mutableStateOf(false) }

                        val playScale by animateFloatAsState(
                            targetValue = if (isPlayFocused) 1.08f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                            label = "playScale"
                        )

                        Button(
                            onClick = {
                                if (defaultStream != null) {
                                    viewModel.playStream(
                                        item = mediaItem,
                                        streamItem = defaultStream,
                                        availableStreams = detailState.streams,
                                        season = selectedEpisode?.season,
                                        episode = selectedEpisode?.episode,
                                        episodeTitle = selectedEpisode?.title
                                    )
                                } else {
                                    viewModel.loadStreams(mediaItem.type, mediaItem.id)
                                }
                            },
                            enabled = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayFocused) NuvioPrimaryBright else NuvioPrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .focusRequester(playButtonFocusRequester)
                                .onFocusChanged { isPlayFocused = it.isFocused }
                                .graphicsLayer {
                                    scaleX = playScale
                                    scaleY = playScale
                                }
                                .border(
                                    width = if (isPlayFocused) 3.5.dp else 0.dp,
                                    color = if (isPlayFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                            KeyEvent.KEYCODE_ENTER,
                                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                if (defaultStream != null) {
                                                    viewModel.playStream(
                                                        item = mediaItem,
                                                        streamItem = defaultStream,
                                                        availableStreams = detailState.streams,
                                                        season = selectedEpisode?.season,
                                                        episode = selectedEpisode?.episode,
                                                        episodeTitle = selectedEpisode?.title
                                                    )
                                                } else {
                                                    viewModel.loadStreams(mediaItem.type, mediaItem.id)
                                                }
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                                .testTag("detail_play_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (detailState.streams.isNotEmpty()) "Play $qBadge".trim() else "Finding Streams...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        var isBookmarkFocused by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { viewModel.toggleWatchlist() },
                            modifier = Modifier
                                .size(50.dp)
                                .onFocusChanged { isBookmarkFocused = it.isFocused }
                                .background(
                                    if (isBookmarkFocused) NuvioPrimary else NuvioSurfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = if (isBookmarkFocused) 3.dp else 1.dp,
                                    color = if (isBookmarkFocused) Color.White else NuvioCardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                            KeyEvent.KEYCODE_ENTER,
                                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                viewModel.toggleWatchlist()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                                .testTag("detail_watchlist_toggle")
                        ) {
                            Icon(
                                imageVector = if (detailState.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (isBookmarkFocused) Color.Black else if (detailState.isInWatchlist) NuvioPrimary else NuvioTextPrimary
                            )
                        }
                    }

                    // Synopsis
                    val description = meta?.description ?: mediaItem.description
                    if (!description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Storyline",
                            color = NuvioTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            color = NuvioTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }

                    // Cast members
                    val castList = meta?.cast ?: mediaItem.cast
                    if (!castList.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Cast & Crew",
                            color = NuvioTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(castList) { entry ->
                                val parts = entry.split("|")
                                val name = parts.getOrNull(0) ?: "Unknown"
                                val character = parts.getOrNull(1)
                                val photoUrl = parts.getOrNull(2)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(80.dp)
                                ) {
                                    if (!photoUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = photoUrl,
                                            contentDescription = name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, NuvioCardBorder, CircleShape)
                                                .background(NuvioSurfaceVariant)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(CircleShape)
                                                .background(NuvioSurfaceVariant)
                                                .border(1.dp, NuvioCardBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = NuvioTextMuted,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = name,
                                        color = NuvioTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    if (!character.isNullOrBlank()) {
                                        Text(
                                            text = character,
                                            color = NuvioTextSecondary,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Series Season and Episodes section
            if (isSeries && (detailState.seasons.isNotEmpty() || detailState.combinedFiles.isNotEmpty())) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Episodes & Seasons",
                                color = NuvioTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Season Selector Dropdown Button
                        if (detailState.seasons.isNotEmpty()) {
                            var isSeasonFocused by remember { mutableStateOf(false) }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSeasonFocused) NuvioPrimaryBright else NuvioSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .onFocusChanged { isSeasonFocused = it.isFocused }
                                    .border(
                                        width = if (isSeasonFocused) 3.5.dp else 1.dp,
                                        color = if (isSeasonFocused) Color.White else NuvioCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.type == KeyEventType.KeyDown) {
                                            when (keyEvent.nativeKeyEvent.keyCode) {
                                                KeyEvent.KEYCODE_DPAD_CENTER,
                                                KeyEvent.KEYCODE_ENTER,
                                                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                    showSeasonPicker = true
                                                    true
                                                }
                                                else -> false
                                            }
                                        } else false
                                    }
                                    .focusable()
                                    .clickable { showSeasonPicker = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "SEASON",
                                            color = if (isSeasonFocused) Color.Black.copy(alpha = 0.7f) else NuvioTextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = if (detailState.selectedSeason == 0) "Specials" else "Season ${detailState.selectedSeason}",
                                            color = if (isSeasonFocused) Color.Black else NuvioPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Season",
                                        tint = if (isSeasonFocused) Color.Black else NuvioPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (detailState.seasonEpisodes.isNotEmpty()) {
                                // Single episodes list
                                detailState.seasonEpisodes.forEach { ep ->
                                    val isChosen = detailState.selectedEpisode?.id == ep.id
                                    var isEpFocused by remember { mutableStateOf(false) }
                                    val epScale by animateFloatAsState(
                                        targetValue = if (isEpFocused) 1.03f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                                        label = "epScale"
                                    )

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isEpFocused -> NuvioPrimaryBright
                                                isChosen -> NuvioSurfaceVariant
                                                else -> Color.Transparent
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { isEpFocused = it.isFocused }
                                            .graphicsLayer {
                                                scaleX = epScale
                                                scaleY = epScale
                                            }
                                            .border(
                                                width = if (isEpFocused) 3.5.dp else if (isChosen) 1.5.dp else 0.5.dp,
                                                color = if (isEpFocused) Color.White else if (isChosen) NuvioPrimary else NuvioCardBorder.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .onKeyEvent { keyEvent ->
                                                if (keyEvent.type == KeyEventType.KeyDown) {
                                                    when (keyEvent.nativeKeyEvent.keyCode) {
                                                        KeyEvent.KEYCODE_DPAD_CENTER,
                                                        KeyEvent.KEYCODE_ENTER,
                                                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                            viewModel.loadEpisodeStreams(ep)
                                                            showEpisodeStreams = true
                                                            true
                                                        }
                                                        else -> false
                                                    }
                                                } else false
                                            }
                                            .focusable()
                                            .clickable { 
                                                viewModel.loadEpisodeStreams(ep)
                                                showEpisodeStreams = true
                                            }
                                            .testTag("episode_card_${ep.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Netflix style compact thumbnail
                                                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                                                val screenWidth = configuration.screenWidthDp.dp
                                                val epThumbWidth = (screenWidth * 0.28f).coerceIn(90.dp, 120.dp)
                                                val epThumbHeight = epThumbWidth * 0.58f

                                                Box(
                                                    modifier = Modifier
                                                        .width(epThumbWidth)
                                                        .height(epThumbHeight)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(NuvioSurfaceVariant)
                                                ) {
                                                    AsyncImage(
                                                        model = ep.thumbnail ?: meta?.background ?: mediaItem.poster,
                                                        contentDescription = ep.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(14.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "E${ep.episode} • ${ep.title ?: "Episode ${ep.episode}"}",
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (!ep.overview.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = ep.overview,
                                                            color = NuvioTextSecondary,
                                                            fontSize = 11.sp,
                                                            maxLines = 2,
                                                            lineHeight = 14.sp,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { 
                                                        viewModel.loadEpisodeStreams(ep)
                                                        showEpisodeStreams = true
                                                    },
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(NuvioSurfaceVariant, RoundedCornerShape(10.dp))
                                                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = "Download Episode",
                                                        tint = NuvioPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                EpisodeActionButton(
                                                    isSelected = isChosen,
                                                    onClick = { 
                                                        viewModel.loadEpisodeStreams(ep)
                                                        showEpisodeStreams = true
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Full Season Packs Section (Segregated)
                            if (detailState.combinedFiles.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Full Season Packs",
                                    color = NuvioTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                detailState.combinedFiles.forEach { pack ->
                                    val isChosen = detailState.selectedEpisode?.id == pack.id
                                    var isPackFocused by remember { mutableStateOf(false) }
                                    val packScale by animateFloatAsState(
                                        targetValue = if (isPackFocused) 1.03f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                                        label = "packScale"
                                    )

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isPackFocused -> NuvioPrimaryBright
                                                isChosen -> NuvioSurfaceVariant
                                                else -> NuvioSurfaceVariant.copy(alpha = 0.4f)
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onFocusChanged { isPackFocused = it.isFocused }
                                            .graphicsLayer {
                                                scaleX = packScale
                                                scaleY = packScale
                                            }
                                            .border(
                                                width = if (isPackFocused) 3.5.dp else if (isChosen) 1.5.dp else 1.dp,
                                                color = if (isPackFocused) Color.White else if (isChosen) NuvioPrimary else NuvioCardBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .onKeyEvent { keyEvent ->
                                                if (keyEvent.type == KeyEventType.KeyDown) {
                                                    when (keyEvent.nativeKeyEvent.keyCode) {
                                                        KeyEvent.KEYCODE_DPAD_CENTER,
                                                        KeyEvent.KEYCODE_ENTER,
                                                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                            viewModel.loadEpisodeStreams(pack)
                                                            showEpisodeStreams = true
                                                            true
                                                        }
                                                        else -> false
                                                    }
                                                } else false
                                            }
                                            .focusable()
                                            .clickable { 
                                                viewModel.loadEpisodeStreams(pack)
                                                showEpisodeStreams = true
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Default.HighQuality, null, tint = NuvioPrimary, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(12.dp))
                                                Text(
                                                    text = pack.displayTitle,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(Icons.Default.PlayArrow, null, tint = NuvioPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Server Streams Section (For Movies Only)
            if (!isSeries) {
                item {
                    Spacer(modifier = Modifier.height(26.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Streams & Sources",
                            color = NuvioTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (detailState.streams.isEmpty() && !detailState.isLoadingStreams) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = NuvioSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = "No streams found for this title.",
                                    color = NuvioTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                detailState.streams.forEach { stream ->
                                    StreamOptionCard(
                                        stream = stream,
                                        onPlay = {
                                            viewModel.playStream(
                                                item = mediaItem,
                                                streamItem = stream,
                                                availableStreams = detailState.streams
                                            )
                                        },
                                        onDownload = {
                                            viewModel.downloadStream(item = mediaItem, stream = stream)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Episode Streams Bottom Sheet
        if (showEpisodeStreams) {
            ModalBottomSheet(
                onDismissRequest = { showEpisodeStreams = false },
                sheetState = episodeSheetState,
                containerColor = NuvioBackground,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    val ep = detailState.selectedEpisode
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (ep?.isCombined == true) ep.displayTitle else "Episode ${ep?.episode}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (ep?.isCombined == false) {
                                Text(
                                    text = ep.title ?: "",
                                    color = NuvioTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        if (detailState.isLoadingEpisodeStreams) {
                            CircularProgressIndicator(color = NuvioPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }

                    if (detailState.episodeStreams.isEmpty() && !detailState.isLoadingEpisodeStreams) {
                        Text(
                            text = "No streams available for this episode.",
                            color = NuvioTextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(detailState.episodeStreams) { stream ->
                                StreamOptionCard(
                                    stream = stream,
                                    onPlay = {
                                        viewModel.playStream(
                                            item = mediaItem,
                                            streamItem = stream,
                                            availableStreams = detailState.episodeStreams,
                                            season = ep?.season,
                                            episode = ep?.episode,
                                            episodeTitle = ep?.title,
                                            videoEpisode = ep
                                        )
                                        showEpisodeStreams = false
                                    },
                                    onDownload = {
                                        viewModel.downloadStream(
                                            item = mediaItem,
                                            stream = stream,
                                            season = ep?.season,
                                            episode = ep?.episode,
                                            episodeTitle = ep?.title
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Season Picker Bottom Sheet
        if (showSeasonPicker) {
            ModalBottomSheet(
                onDismissRequest = { showSeasonPicker = false },
                sheetState = seasonSheetState,
                containerColor = NuvioBackground,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Select Season",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        items(detailState.seasons) { season ->
                            val isSelected = detailState.selectedSeason == season
                            var isSeasonItemFocused by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .onFocusChanged { isSeasonItemFocused = it.isFocused }
                                    .border(
                                        width = if (isSeasonItemFocused) 3.dp else 0.dp,
                                        color = if (isSeasonItemFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        when {
                                            isSeasonItemFocused -> NuvioPrimaryBright
                                            isSelected -> NuvioPrimary.copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        },
                                        RoundedCornerShape(10.dp)
                                    )
                                    .onKeyEvent { keyEvent ->
                                        if (keyEvent.type == KeyEventType.KeyDown) {
                                            when (keyEvent.nativeKeyEvent.keyCode) {
                                                KeyEvent.KEYCODE_DPAD_CENTER,
                                                KeyEvent.KEYCODE_ENTER,
                                                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                    viewModel.selectSeason(season)
                                                    scope.launch {
                                                        seasonSheetState.hide()
                                                        showSeasonPicker = false
                                                    }
                                                    true
                                                }
                                                else -> false
                                            }
                                        } else false
                                    }
                                    .focusable()
                                    .clickable {
                                        viewModel.selectSeason(season)
                                        scope.launch {
                                            seasonSheetState.hide()
                                            showSeasonPicker = false
                                        }
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (season == 0) "Specials" else "Season $season",
                                    color = if (isSeasonItemFocused) Color.Black else if (isSelected) NuvioPrimary else NuvioTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSeasonItemFocused || isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isSeasonItemFocused) Color.Black else NuvioPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // High-Quality Full Screen Loading Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = detailState.isLoadingStreams && detailState.streams.isEmpty(),
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(40.dp)
                ) {
                    CircularProgressIndicator(
                        color = NuvioPrimary,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Fetching Streams",
                        color = NuvioTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Searching high quality sources...",
                        color = NuvioTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isBackFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .background(
                        if (isBackFocused) NuvioPrimary else Color.Black.copy(alpha = 0.65f),
                        CircleShape
                    )
                    .border(
                        width = if (isBackFocused) 3.dp else 1.dp,
                        color = if (isBackFocused) Color.White else NuvioCardBorder,
                        shape = CircleShape
                    )
                    .size(42.dp)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_CENTER,
                                KeyEvent.KEYCODE_ENTER,
                                KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                    onBackClick()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
                    .focusable()
                    .testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isBackFocused) Color.Black else NuvioTextPrimary
                )
            }
        }
    }
}

@Composable
private fun EpisodeActionButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) NuvioPrimary else NuvioSurfaceVariant,
        border = if (!isSelected) BorderStroke(1.dp, NuvioCardBorder) else null,
        modifier = modifier
            .widthIn(min = 92.dp)
            .height(34.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isSelected) Color.Black else NuvioPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isSelected) "Playing" else "Play",
                color = if (isSelected) Color.Black else NuvioTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreamOptionCard(
    stream: StreamItem,
    onPlay: () -> Unit,
    onDownload: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "streamCardScale"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) NuvioPrimaryBright else NuvioCard
        ),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isFocused) 3.5.dp else 1.dp,
                color = if (isFocused) Color.White else NuvioCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onPlay()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable(onClick = onPlay)
            .testTag("stream_option_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Quality badge
                    if (stream.displayQuality.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NuvioPrimary
                        ) {
                            Text(
                                text = stream.displayQuality,
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Size badge
                    if (stream.formattedSize.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NuvioSurfaceVariant
                        ) {
                            Text(
                                text = stream.formattedSize,
                                color = NuvioTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Audio
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NuvioSurfaceVariant
                    ) {
                        Text(
                            text = stream.displayAudio,
                            color = NuvioSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stream.displayFileName,
                    color = NuvioTextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 5
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDownload != null) {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier
                            .size(36.dp)
                            .background(NuvioSurfaceVariant, RoundedCornerShape(10.dp))
                            .border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
                            .testTag("stream_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Stream",
                            tint = NuvioPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NuvioPrimary,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp).widthIn(min = 80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )
                }
            }
        }
    }
}

