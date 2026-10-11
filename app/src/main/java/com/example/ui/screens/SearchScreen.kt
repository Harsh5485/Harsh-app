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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import android.view.KeyEvent
import com.example.ui.theme.NuvioPrimaryBright
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaItem
import com.example.ui.components.MediaPosterCard
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

private val QUICK_SUGGESTIONS = listOf("Spider-Man", "Hostel Daze", "Raakh", "Main Ladega", "Action", "Comedy", "Thriller")

@Composable
fun SearchScreen(
    viewModel: NuvioViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchState by viewModel.searchState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
            .statusBarsPadding()
    ) {
        // Search Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isBackFocused by remember { mutableStateOf(false) }
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .background(
                        if (isBackFocused) NuvioPrimaryBright else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        width = if (isBackFocused) 3.dp else 0.dp,
                        color = if (isBackFocused) Color.White else Color.Transparent,
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
                    .testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isBackFocused) Color.Black else NuvioTextPrimary
                )
            }

            OutlinedTextField(
                value = searchState.query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Search movies, series, anime...",
                        color = NuvioTextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NuvioPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchState.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = NuvioTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NuvioPrimary,
                    unfocusedBorderColor = NuvioCardBorder,
                    focusedContainerColor = NuvioSurfaceVariant,
                    unfocusedContainerColor = NuvioSurfaceVariant,
                    focusedTextColor = NuvioTextPrimary,
                    unfocusedTextColor = NuvioTextPrimary,
                    cursorColor = NuvioPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("search_input_field")
            )
        }

        // Filter Pills: All, Movies, TV Shows
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("all", "All", null),
                Triple("movie", "Movies", Icons.Default.Movie),
                Triple("series", "TV Series", Icons.Default.Tv)
            ).forEach { (key, label, icon) ->
                val isSelected = searchState.typeFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateSearchType(key) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else NuvioTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
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

        // Quick suggestions when empty
        if (searchState.query.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Trending Searches",
                    color = NuvioTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QUICK_SUGGESTIONS) { term ->
                        var isTermFocused by remember { mutableStateOf(false) }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isTermFocused) NuvioPrimaryBright else NuvioSurfaceVariant,
                            modifier = Modifier
                                .onFocusChanged { isTermFocused = it.isFocused }
                                .border(
                                    width = if (isTermFocused) 3.dp else 1.dp,
                                    color = if (isTermFocused) Color.White else NuvioCardBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .onKeyEvent { keyEvent ->
                                    if (keyEvent.type == KeyEventType.KeyDown) {
                                        when (keyEvent.nativeKeyEvent.keyCode) {
                                            KeyEvent.KEYCODE_DPAD_CENTER,
                                            KeyEvent.KEYCODE_ENTER,
                                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                                viewModel.updateSearchQuery(term)
                                                true
                                            }
                                            else -> false
                                        }
                                    } else false
                                }
                                .focusable()
                                .clickable { viewModel.updateSearchQuery(term) }
                        ) {
                            Text(
                                text = term,
                                color = if (isTermFocused) Color.Black else NuvioTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isTermFocused) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Searching indicator
        if (searchState.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = NuvioPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Search Results Grid or Empty State
        if (searchState.results.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 115.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchState.results, key = { it.id }) { item ->
                    MediaPosterCard(
                        item = item,
                        onClick = { onMediaClick(item) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (searchState.query.isNotBlank() && !searchState.isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NuvioTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No titles found for \"${searchState.query}\"",
                        color = NuvioTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try searching by another title, genre, or keyword",
                        color = NuvioTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
