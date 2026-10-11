package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ServerSettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.NuvioViewModel
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioPrimaryBright
import com.example.ui.theme.NuvioSurface
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextSecondary

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import android.view.KeyEvent

enum class NuvioNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    DOWNLOADS("Downloads", Icons.Filled.Download, Icons.Outlined.Download),
    LIBRARY("Watchlist", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    SETTINGS("Server", Icons.Filled.CloudDone, Icons.Outlined.CloudDone)
}

enum class SubScreen {
    NONE,
    DETAIL,
    SETTINGS_STANDALONE,
    SEARCH_STANDALONE
}

@Composable
fun TvSideNavigationRail(
    currentTab: NuvioNavTab,
    onTabSelected: (NuvioNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var isRailFocused by remember { mutableStateOf(false) }
    val railWidth by animateDpAsState(
        targetValue = if (isRailFocused) 180.dp else 68.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "railWidth"
    )

    Surface(
        modifier = modifier
            .width(railWidth)
            .fillMaxHeight()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        if (isRailFocused) NuvioPrimary.copy(alpha = 0.6f) else Color.Transparent,
                        NuvioCardBorder.copy(alpha = 0.3f)
                    )
                ),
                shape = androidx.compose.ui.graphics.RectangleShape
            ),
        color = NuvioSurface.copy(alpha = 0.98f),
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            // App Branding Header for TV
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, bottom = 12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = NuvioPrimary,
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "H",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                if (isRailFocused) {
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "HARSH TV",
                        color = NuvioPrimaryBright,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        maxLines = 1
                    )
                }
            }

            NuvioNavTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                var isItemFocused by remember { mutableStateOf(false) }

                val scale by animateFloatAsState(
                    targetValue = if (isItemFocused) 1.06f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "railScale"
                )

                Surface(
                    onClick = { onTabSelected(tab) },
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isItemFocused -> NuvioPrimaryBright
                        isSelected -> NuvioSurfaceVariant
                        else -> Color.Transparent
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .onFocusChanged { focusState ->
                            isItemFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                isRailFocused = true
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .border(
                            width = if (isItemFocused) 3.dp else if (isSelected) 1.dp else 0.dp,
                            color = if (isItemFocused) Color.White else if (isSelected) NuvioPrimary.copy(alpha = 0.6f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_CENTER,
                                    KeyEvent.KEYCODE_ENTER,
                                    KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                        onTabSelected(tab)
                                        true
                                    }
                                    else -> false
                                }
                            } else false
                        }
                        .focusable()
                        .testTag("tv_nav_tab_${tab.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = if (isSelected || isItemFocused) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.title,
                            tint = when {
                                isItemFocused -> Color.Black
                                isSelected -> NuvioPrimaryBright
                                else -> NuvioTextSecondary
                            },
                            modifier = Modifier.size(22.dp)
                        )
                        if (isRailFocused) {
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = tab.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected || isItemFocused) FontWeight.ExtraBold else FontWeight.Medium,
                                color = when {
                                    isItemFocused -> Color.Black
                                    isSelected -> NuvioPrimaryBright
                                    else -> NuvioTextSecondary
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NuvioMainApp(viewModel: NuvioViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(NuvioNavTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    val playerPlayback by viewModel.playerPlayback.collectAsState()

    if (showSplash) {
        SplashScreen(
            onSplashFinished = { showSplash = false }
        )
        return
    }

    // Fullscreen Player overlay when playback active
    if (playerPlayback != null) {
        BackHandler {
            viewModel.closePlayer()
        }
        PlayerScreen(
            playbackState = playerPlayback!!,
            onBackClick = { viewModel.closePlayer() },
            onSaveProgress = { pos, dur -> viewModel.saveProgress(pos, dur) },
            onNextClick = { viewModel.playNextEpisode() },
            onSwitchStream = { newStream -> viewModel.switchPlayerStream(newStream) }
        )
        return
    }

    // Back handling for sub-screens
    BackHandler(enabled = currentSubScreen != SubScreen.NONE) {
        currentSubScreen = SubScreen.NONE
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(NuvioBackground)
    ) {
        // TV Side Navigation Rail
        if (currentSubScreen == SubScreen.NONE) {
            TvSideNavigationRail(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    currentSubScreen = SubScreen.NONE
                }
            )
        }

        // Main Screen Content Area with TV Safe Margins (overscan padding: 24.dp)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(
                    start = if (currentSubScreen == SubScreen.NONE) 8.dp else 24.dp,
                    end = 24.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
        ) {
            when (currentSubScreen) {
                SubScreen.DETAIL -> {
                    MediaDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.SETTINGS_STANDALONE -> {
                    ServerSettingsScreen(
                        viewModel = viewModel,
                        onBackClick = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.SEARCH_STANDALONE -> {
                    SearchScreen(
                        viewModel = viewModel,
                        onMediaClick = { item ->
                            viewModel.openMediaDetail(item)
                            currentSubScreen = SubScreen.DETAIL
                        },
                        onBackClick = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.NONE -> {
                    when (currentTab) {
                        NuvioNavTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onMediaClick = { item ->
                                    viewModel.openMediaDetail(item)
                                    currentSubScreen = SubScreen.DETAIL
                                },
                                onSearchClick = {
                                    currentSubScreen = SubScreen.SEARCH_STANDALONE
                                },
                                onSettingsClick = {
                                    currentSubScreen = SubScreen.SETTINGS_STANDALONE
                                }
                            )
                        }
                        NuvioNavTab.SEARCH -> {
                            SearchScreen(
                                viewModel = viewModel,
                                onMediaClick = { item ->
                                    viewModel.openMediaDetail(item)
                                    currentSubScreen = SubScreen.DETAIL
                                },
                                onBackClick = {
                                    currentTab = NuvioNavTab.HOME
                                }
                            )
                        }
                        NuvioNavTab.DOWNLOADS -> {
                            DownloadsScreen(
                                viewModel = viewModel
                            )
                        }
                        NuvioNavTab.LIBRARY -> {
                            LibraryScreen(
                                viewModel = viewModel,
                                onMediaClick = { item ->
                                    viewModel.openMediaDetail(item)
                                    currentSubScreen = SubScreen.DETAIL
                                }
                            )
                        }
                        NuvioNavTab.SETTINGS -> {
                            ServerSettingsScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    currentTab = NuvioNavTab.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
