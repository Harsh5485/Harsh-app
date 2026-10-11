package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.MediaItem
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCard
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioError
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

@Composable
fun LibraryScreen(
    viewModel: NuvioViewModel,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Continue Watching", "Watchlist", "History")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Library",
                    color = NuvioTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your bookmarks & watch progress",
                    color = NuvioTextSecondary,
                    fontSize = 12.sp
                )
            }

            if (selectedTab == 2 && watchHistory.isNotEmpty()) {
                IconButton(
                    onClick = { showClearHistoryDialog = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = NuvioError
                    )
                }
            }
        }

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NuvioBackground,
            contentColor = NuvioPrimary,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NuvioPrimary
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) NuvioPrimary else NuvioTextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Continue Watching
                if (continueWatching.isEmpty()) {
                    EmptyLibraryState(
                        icon = Icons.Default.PlayArrow,
                        title = "No active progress",
                        subtitle = "Start streaming any movie or show and your progress will appear here automatically."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(continueWatching, key = { it.id }) { item ->
                            HistoryItemCard(
                                item = item,
                                onResume = { viewModel.resumeHistoryItem(item) },
                                onDelete = { viewModel.deleteHistory(item.id) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Watchlist
                if (watchlist.isEmpty()) {
                    EmptyLibraryState(
                        icon = Icons.Default.Bookmark,
                        title = "Your Watchlist is empty",
                        subtitle = "Save movies and series to your watchlist using the bookmark icon to watch them later."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 115.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchlist, key = { it.id }) { item ->
                            val mediaItem = MediaItem(
                                id = item.id,
                                name = item.title,
                                type = item.type,
                                poster = item.poster,
                                background = item.backdrop,
                                year = item.year,
                                imdbRating = item.rating
                            )
                            MediaPosterCard(
                                item = mediaItem,
                                onClick = { onMediaClick(mediaItem) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            2 -> {
                // Full Watch History
                if (watchHistory.isEmpty()) {
                    EmptyLibraryState(
                        icon = Icons.Default.History,
                        title = "No watch history",
                        subtitle = "Movies and episodes you stream will be cataloged here."
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchHistory, key = { it.id }) { item ->
                            HistoryItemCard(
                                item = item,
                                onResume = { viewModel.resumeHistoryItem(item) },
                                onDelete = { viewModel.deleteHistory(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History?", color = NuvioTextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This will delete all saved watch progress and history.", color = NuvioTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear All", color = NuvioError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = NuvioTextSecondary)
                }
            },
            containerColor = NuvioSurfaceVariant
        )
    }
}

@Composable
fun HistoryItemCard(
    item: WatchHistoryEntity,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onResume)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with progress
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NuvioSurfaceVariant)
            ) {
                AsyncImage(
                    model = item.backdrop ?: item.poster,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = NuvioPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = NuvioPrimary,
                    trackColor = Color.Black.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = NuvioTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sub = if (item.season != null && item.episode != null) {
                    "S${item.season}:E${item.episode} • ${item.episodeTitle ?: ""}"
                } else {
                    "${(item.progressFraction * 100).toInt()}% watched"
                }
                Text(
                    text = sub,
                    color = NuvioTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = NuvioTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(NuvioSurfaceVariant, CircleShape)
                    .border(1.dp, NuvioCardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NuvioPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = NuvioTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                color = NuvioTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
