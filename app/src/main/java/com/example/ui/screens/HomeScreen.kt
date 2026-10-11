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
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.HeroBanner
import com.example.ui.components.MediaSectionRow
import com.example.ui.components.ServerStatusBadge
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSurface
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

private val GENRES = listOf(
    "Action",
    "Adventure",
    "Comedy",
    "Crime",
    "Drama",
    "Romance",
    "Sci-Fi",
    "Thriller",
    "Horror"
)

@Composable
fun HomeScreen(
    viewModel: NuvioViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    val serverState by viewModel.serverState.collectAsState()
    val continueWatchingList by viewModel.continueWatching.collectAsState()

    val carouselItems = (homeState.heroItems + homeState.latestMovies).distinctBy { it.id }.take(5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Non-sticky top bar header (scrolls up naturally with page content)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo & Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(NuvioPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HARSH",
                            color = NuvioTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Actions: Server Badge, Search, Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ServerStatusBadge(
                            isConnected = serverState.isConnected,
                            tokenName = serverState.activeTokenName,
                            onClick = onSettingsClick
                        )

                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("home_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = NuvioTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.loadHomeData() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("home_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = NuvioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            // Auto-Sliding Carousel
            if (carouselItems.isNotEmpty()) {
                item {
                    val pagerState = rememberPagerState(pageCount = { carouselItems.size })
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(4000)
                            if (carouselItems.isNotEmpty()) {
                                val nextPage = (pagerState.currentPage + 1) % carouselItems.size
                                pagerState.animateScrollToPage(nextPage)
                            }
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val item = carouselItems[page]
                        HeroBanner(
                            item = item,
                            onPlayClick = {
                                viewModel.streamNow(item)
                            },
                            onDetailClick = {
                                viewModel.openMediaDetail(item)
                                onMediaClick(item)
                            }
                        )
                    }
                }
            }

            // Genre Selector Chips
            item {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Browse by Genre",
                        color = NuvioTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isAllSelected = homeState.selectedGenre == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { viewModel.selectGenre(null) },
                                label = {
                                    Text(
                                        text = "All",
                                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NuvioPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = NuvioSurfaceVariant,
                                    labelColor = NuvioTextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isAllSelected,
                                    borderColor = if (isAllSelected) NuvioPrimary else NuvioCardBorder
                                )
                            )
                        }

                        items(GENRES) { genre ->
                            val isSelected = homeState.selectedGenre.equals(genre, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectGenre(genre) },
                                label = {
                                    Text(
                                        text = genre,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NuvioPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = NuvioSurfaceVariant,
                                    labelColor = NuvioTextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) NuvioPrimary else NuvioCardBorder
                                )
                            )
                        }
                    }
                }
            }

            // Continue Watching Row (if any)
            if (continueWatchingList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Continue Watching",
                            color = NuvioTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(continueWatchingList, key = { it.id }) { item ->
                                ContinueWatchingCard(
                                    item = item,
                                    onClick = { viewModel.resumeHistoryItem(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Loading Indicator
            if (homeState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NuvioPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Latest Movies
            item {
                Spacer(modifier = Modifier.height(12.dp))
                MediaSectionRow(
                    title = "Latest Movies",
                    items = homeState.latestMovies,
                    onItemClick = onMediaClick
                )
            }

            // Popular Movies
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MediaSectionRow(
                    title = "Popular Movies",
                    items = homeState.popularMovies,
                    onItemClick = onMediaClick
                )
            }

            // Latest TV Series
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MediaSectionRow(
                    title = "Latest Series",
                    items = homeState.latestSeries,
                    onItemClick = onMediaClick
                )
            }

            // Popular TV Series
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MediaSectionRow(
                    title = "Popular Series",
                    items = homeState.popularSeries,
                    onItemClick = onMediaClick
                )
            }
        }
    }
}
