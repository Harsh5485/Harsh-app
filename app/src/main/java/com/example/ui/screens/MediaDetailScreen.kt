package com.example.ui.screens

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
import com.example.ui.theme.NuvioSecondary
import com.example.ui.theme.NuvioSuccess
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaDetailScreen(
    viewModel: NuvioViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detailState by viewModel.detailState.collectAsState()
    val mediaItem = detailState.mediaItem ?: return
    val meta = detailState.metaDetail

    val isSeries = mediaItem.type.equals("series", true)

    var selectedEpisode by remember { mutableStateOf<VideoEpisode?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Backdrop Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    AsyncImage(
                        model = meta?.background ?: mediaItem.background ?: mediaItem.poster,
                        contentDescription = mediaItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlays
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        NuvioBackground.copy(alpha = 0.6f),
                                        Color.Transparent,
                                        NuvioBackground.copy(alpha = 0.85f),
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
                        .padding(horizontal = 16.dp)
                ) {
                    // Title
                    Text(
                        text = mediaItem.name,
                        color = NuvioTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 30.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badges row: Rating, Year, Runtime, Type
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val rating = formatRating(meta?.imdbRating ?: mediaItem.imdbRating)
                        if (!rating.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.border(0.5.dp, NuvioGold, RoundedCornerShape(6.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = NuvioGold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = rating,
                                        color = NuvioGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        val year = meta?.year ?: meta?.releaseInfo ?: mediaItem.year
                        if (!year.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NuvioSurfaceVariant
                            ) {
                                Text(
                                    text = year,
                                    color = NuvioTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        val runtime = meta?.runtime ?: mediaItem.runtime
                        if (!runtime.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NuvioSurfaceVariant
                            ) {
                                Text(
                                    text = runtime,
                                    color = NuvioTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NuvioPrimary.copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = if (isSeries) "SERIES" else "MOVIE",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
                        val firstStream = detailState.streams.firstOrNull()
                        Button(
                            onClick = {
                                if (firstStream != null) {
                                    viewModel.playStream(
                                        item = mediaItem,
                                        streamItem = firstStream,
                                        availableStreams = detailState.streams,
                                        season = selectedEpisode?.season,
                                        episode = selectedEpisode?.episode,
                                        episodeTitle = selectedEpisode?.title
                                    )
                                }
                            },
                            enabled = detailState.streams.isNotEmpty(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NuvioPrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("detail_play_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (detailState.streams.isNotEmpty()) "Play (${firstStream?.displayQuality ?: "HD"})" else "Finding Streams...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleWatchlist() },
                            modifier = Modifier
                                .size(50.dp)
                                .background(NuvioSurfaceVariant, RoundedCornerShape(12.dp))
                                .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
                                .testTag("detail_watchlist_toggle")
                        ) {
                            Icon(
                                imageVector = if (detailState.isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Watchlist",
                                tint = if (detailState.isInWatchlist) NuvioPrimary else NuvioTextPrimary
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
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            color = NuvioTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }

                    // Cast members
                    val castList = meta?.cast ?: mediaItem.cast
                    if (!castList.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Cast & Crew",
                            color = NuvioTextPrimary,
                            fontSize = 17.sp,
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

                        // Seasons tabs (Season 1, Season 2, ...)
                        val totalTabs = detailState.seasons.size
                        val selectedTabIdx = detailState.seasons.indexOf(detailState.selectedSeason).coerceAtLeast(0)

                        if (totalTabs > 0) {
                            TabRow(
                                selectedTabIndex = selectedTabIdx.coerceIn(0, (totalTabs - 1).coerceAtLeast(0)),
                                containerColor = NuvioBackground,
                                contentColor = NuvioPrimary,
                                indicator = { tabPositions ->
                                    if (selectedTabIdx < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIdx]),
                                            color = NuvioPrimary
                                        )
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                detailState.seasons.forEach { s ->
                                    val isSelected = detailState.selectedSeason == s
                                    Tab(
                                        selected = isSelected,
                                        onClick = { viewModel.selectSeason(s) },
                                        text = {
                                            Text(
                                                text = if (s == 0) "Specials" else "Season $s",
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp,
                                                color = if (isSelected) NuvioPrimary else NuvioTextSecondary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
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
                            if (!detailState.isSeasonCombinedOnly && detailState.seasonEpisodes.isNotEmpty()) {
                                // Single episodes view ONLY (clean Netflix style)
                                detailState.seasonEpisodes.forEach { ep ->
                                    val isChosen = detailState.selectedEpisode?.id == ep.id
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isChosen) NuvioSurfaceVariant else NuvioCard
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                1.dp,
                                                if (isChosen) NuvioPrimary else NuvioCardBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { viewModel.playEpisodeDirectly(mediaItem, ep) }
                                            .testTag("episode_card_${ep.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Netflix style compact thumbnail
                                                Box(
                                                    modifier = Modifier
                                                        .width(84.dp)
                                                        .height(52.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(NuvioSurfaceVariant)
                                                ) {
                                                    AsyncImage(
                                                        model = ep.thumbnail ?: meta?.background ?: mediaItem.poster,
                                                        contentDescription = ep.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .size(22.dp)
                                                            .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = null,
                                                            tint = NuvioPrimary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "E${ep.episode}. ${ep.title ?: "Episode ${ep.episode}"}",
                                                        color = NuvioTextPrimary,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (!ep.overview.isNullOrBlank()) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = ep.overview,
                                                            color = NuvioTextSecondary,
                                                            fontSize = 11.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { viewModel.downloadEpisodeDirectly(mediaItem, ep) },
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
                                                    onClick = { viewModel.playEpisodeDirectly(mediaItem, ep) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Server Streams Section
            item {
                Spacer(modifier = Modifier.height(26.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Streams & Sources",
                                color = NuvioTextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Powered by Server Addon",
                                color = NuvioTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        if (detailState.isLoadingStreams) {
                            CircularProgressIndicator(
                                color = NuvioPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    val targetId = if (isSeries && selectedEpisode != null) selectedEpisode!!.id else mediaItem.id
                                    viewModel.loadStreams(mediaItem.type, targetId)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Streams",
                                    tint = NuvioTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (detailState.streamErrorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NuvioSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = detailState.streamErrorMessage ?: "No streams available",
                                color = NuvioTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else if (detailState.streams.isEmpty() && !detailState.isLoadingStreams) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NuvioSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = "No streams found on Server channels for this title.",
                                color = NuvioTextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            detailState.streams.forEachIndexed { index, stream ->
                                StreamOptionCard(
                                    stream = stream,
                                    onPlay = {
                                        viewModel.playStream(
                                            item = mediaItem,
                                            streamItem = stream,
                                            availableStreams = detailState.streams,
                                            season = selectedEpisode?.season,
                                            episode = selectedEpisode?.episode,
                                            episodeTitle = selectedEpisode?.title
                                        )
                                    },
                                    onDownload = {
                                        viewModel.downloadStream(
                                            item = mediaItem,
                                            stream = stream,
                                            season = selectedEpisode?.season,
                                            episode = selectedEpisode?.episode,
                                            episodeTitle = selectedEpisode?.title
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Navigation Bar (floating with back button, protected by gradient so it never overlaps text)
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
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                    .border(1.dp, NuvioCardBorder, CircleShape)
                    .size(42.dp)
                    .testTag("detail_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NuvioTextPrimary
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
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isSelected) "Playing" else "Play",
                color = if (isSelected) Color.Black else NuvioTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
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
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = NuvioPrimary
                    ) {
                        Text(
                            text = stream.displayQuality,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Size badge
                    if (stream.formattedSize.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = NuvioSurfaceVariant
                        ) {
                            Text(
                                text = stream.formattedSize,
                                color = NuvioTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Audio
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = NuvioSurfaceVariant
                    ) {
                        Text(
                            text = stream.displayAudio,
                            color = NuvioSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Play",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

