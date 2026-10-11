package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.material3.IconButton
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import android.view.KeyEvent
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.data.model.MediaItem
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCard
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioGold
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioPrimaryBright
import com.example.ui.theme.NuvioSecondary
import com.example.ui.theme.NuvioSuccess
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary

fun formatRating(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    val clean = raw.trim()
    val num = clean.toDoubleOrNull() ?: return if (clean.length <= 4) clean else null
    return when {
        num > 1000.0 -> String.format(java.util.Locale.US, "%.1f", num / 1000.0)
        num > 100.0 -> String.format(java.util.Locale.US, "%.1f", num / 100.0)
        num > 10.0 -> String.format(java.util.Locale.US, "%.1f", num / 10.0)
        num in 0.0..10.0 -> String.format(java.util.Locale.US, "%.1f", num)
        else -> clean.take(4)
    }
}

@Composable
fun MediaPosterCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 360
    val cardWidth = if (isSmallScreen) 115.dp else 140.dp
    
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Card(
        modifier = modifier
            .width(cardWidth)
            .aspectRatio(2f / 3.1f)
            .zIndex(if (isFocused) 10f else 1f)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isFocused) 3.dp else 0.5.dp,
                color = if (isFocused) NuvioPrimaryBright else NuvioCardBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
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
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .testTag("media_card_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 2.dp)
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            // Placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
            )

            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(item.poster ?: item.background)
                    .size(coil.size.Size(280, 420))
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )

            // Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formattedRating = formatRating(item.imdbRating)
                if (!formattedRating.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier.border(0.4.dp, NuvioGold.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = NuvioGold, modifier = Modifier.size(7.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(formattedRating, color = NuvioGold, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                val yearText = item.year ?: item.releaseInfo
                if (!yearText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = NuvioPrimary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = yearText.trim().take(4),
                            color = Color.Black,
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 2.5.dp, vertical = 0.5.dp)
                        )
                    }
                }
            }

            Text(
                text = item.name,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                lineHeight = 9.5.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
fun HeroBanner(
    item: MediaItem,
    onPlayClick: () -> Unit,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val bannerHeight = (screenHeight * 0.5f).coerceAtMost(420.dp).coerceAtLeast(300.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(bannerHeight)
            .testTag("hero_banner")
    ) {
        AsyncImage(
            model = item.background ?: item.poster,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Multi-layered gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            NuvioBackground.copy(alpha = 0.8f),
                            NuvioBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NuvioPrimary,
                ) {
                    Text(
                        text = if (item.type.equals("series", true)) "SERIES" else "MOVIE",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                val rating = formatRating(item.imdbRating)
                if (rating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = NuvioGold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(rating, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = item.name.uppercase(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 28.sp,
                letterSpacing = (-0.8).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!item.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var isPlayFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlayFocused) NuvioPrimaryBright else Color.White,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .onFocusChanged { isPlayFocused = it.isFocused }
                        .border(
                            width = if (isPlayFocused) 3.dp else 0.dp,
                            color = if (isPlayFocused) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .focusable()
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Now", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }

                var isDetailFocused by remember { mutableStateOf(false) }
                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .size(48.dp)
                        .onFocusChanged { isDetailFocused = it.isFocused }
                        .background(
                            if (isDetailFocused) NuvioPrimary else Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isDetailFocused) 3.dp else 1.dp,
                            color = if (isDetailFocused) Color.White else Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .focusable()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        tint = if (isDetailFocused) Color.Black else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    item: WatchHistoryEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.45f).coerceIn(180.dp, 240.dp)
    val thumbHeight = cardWidth * 0.56f

    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "cwScale"
    )

    Card(
        modifier = modifier
            .width(cardWidth)
            .zIndex(if (isFocused) 10f else 1f)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = if (isFocused) NuvioPrimaryBright else NuvioCardBorder,
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
            .clickable(onClick = onClick)
            .testTag("continue_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbHeight)
            ) {
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(item.backdrop ?: item.poster)
                        .size(coil.size.Size(360, 200))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play icon pill
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(38.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.5.dp, NuvioPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = NuvioPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { item.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = NuvioPrimary,
                    trackColor = Color.Black.copy(alpha = 0.7f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.title,
                    color = NuvioTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
                val epText = if (item.season != null && item.episode != null) {
                    "S${item.season}:E${item.episode} ${item.episodeTitle ?: ""}".trim()
                } else {
                    "Resume playback"
                }
                Text(
                    text = epText,
                    color = NuvioTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MediaSectionRow(
    title: String,
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp
            )
            
            Text(
                text = "See All",
                color = NuvioPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* Future implement */ }.padding(4.dp)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(items, key = { it.id }) { item ->
                MediaPosterCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun ServerStatusBadge(
    isConnected: Boolean,
    tokenName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    var isBadgeFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .onFocusChanged { isBadgeFocused = it.isFocused }
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isBadgeFocused) 3.dp else 1.dp,
                color = if (isBadgeFocused) Color.White else if (isConnected) NuvioSuccess.copy(alpha = 0.5f) else NuvioPrimary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
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
            .clickable(onClick = onClick)
            .testTag("server_status_badge"),
        shape = RoundedCornerShape(20.dp),
        color = if (isBadgeFocused) NuvioPrimaryBright else NuvioSurfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (isConnected) NuvioSuccess.copy(alpha = alphaPulse) else NuvioPrimary,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isConnected) "Server: $tokenName" else "Connecting...",
                color = if (isBadgeFocused) Color.Black else NuvioTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
