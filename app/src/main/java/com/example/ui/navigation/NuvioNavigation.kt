package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ServerSettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSurface
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

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
            onSwitchStream = { newStream -> viewModel.switchPlayerStream(newStream) }
        )
        return
    }

    // Back handling for sub-screens
    BackHandler(enabled = currentSubScreen != SubScreen.NONE) {
        currentSubScreen = SubScreen.NONE
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NuvioBackground,
        bottomBar = {
            if (currentSubScreen == SubScreen.NONE) {
                NavigationBar(
                    containerColor = NuvioSurface.copy(alpha = 0.95f),
                    contentColor = NuvioTextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    NuvioNavTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentTab = tab
                                currentSubScreen = SubScreen.NONE
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NuvioPrimary,
                                indicatorColor = NuvioPrimary,
                                unselectedIconColor = NuvioTextSecondary,
                                unselectedTextColor = NuvioTextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
