package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import android.view.KeyEvent
import com.example.ui.theme.NuvioPrimaryBright
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Actions: Server Badge, Search, Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServerStatusBadge(
                            isConnected = serverState.isConnected,
                            tokenName = serverState.activeTokenName,
                            onClick = onSettingsClick
                        )

                        var isSearchFocused by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier
                                .size(36.dp)
                                .onFocusChanged { isSearchFocused = it.isFocused }
                                .background(
                                    if (isSearchFocused) NuvioPrimaryBright else NuvioSurfaceVariant,
                                    CircleShape
                                )
                                .border(
                                    width = if (isSearchFocused) 2.5.dp else 1.dp,
                                    color = if (isSearchFocused) Color.White else NuvioCardBorder,
                                    shape = CircleShape
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                            KeyEvent.KEYCODE_ENTER,
                                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                onSearchClick()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                                .testTag("home_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isSearchFocused) Color.Black else NuvioTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        var isRefreshFocused by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { viewModel.loadHomeData() },
                            modifier = Modifier
                                .size(36.dp)
                                .onFocusChanged { isRefreshFocused = it.isFocused }
                                .background(
                                    if (isRefreshFocused) NuvioPrimaryBright else NuvioSurfaceVariant,
                                    CircleShape
                                )
                                .border(
                                    width = if (isRefreshFocused) 2.5.dp else 1.dp,
                                    color = if (isRefreshFocused) Color.White else NuvioCardBorder,
                                    shape = CircleShape
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                            KeyEvent.KEYCODE_ENTER,
                                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                viewModel.loadHomeData()
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                                .testTag("home_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (isRefreshFocused) Color.Black else NuvioTextSecondary,
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

            // Genres
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { 20 }
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Explore Genres",
                            color = NuvioTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                GenreChip(
                                    label = "All",
                                    isSelected = homeState.selectedGenre == null,
                                    onClick = { viewModel.selectGenre(null) }
                                )
                            }
                            items(GENRES) { genre ->
                                GenreChip(
                                    label = genre,
                                    isSelected = homeState.selectedGenre.equals(genre, ignoreCase = true),
                                    onClick = { viewModel.selectGenre(genre) }
                                )
                            }
                        }
                    }
                }
            }

            // Staggered sections
            val sections = listOf(
                "Latest Releases" to homeState.latestMovies,
                "Trending Movies" to homeState.popularMovies,
                "Binge-Worthy Series" to homeState.latestSeries,
                "All-Time Popular" to homeState.popularSeries
            )

            sections.forEachIndexed { index, (title, items) ->
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(600, delayMillis = 200 * index)) + 
                                slideInVertically(tween(600, delayMillis = 200 * index)) { 50 }
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        MediaSectionRow(
                            title = title,
                            items = items,
                            onItemClick = onMediaClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else if (!isSelected) 1.dp else 0.dp,
                color = if (isFocused) Color.White else NuvioCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onClick()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable(onClick = onClick),
        color = when {
            isFocused -> NuvioPrimaryBright
            isSelected -> NuvioPrimary
            else -> NuvioSurfaceVariant
        },
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            color = if (isFocused || isSelected) Color.Black else NuvioTextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
