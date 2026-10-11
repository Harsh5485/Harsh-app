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
import androidx.compose.runtime.remember
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
    val cardWidth = if (isSmallScreen) 105.dp else 125.dp
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Card(
        modifier = modifier
            .width(cardWidth)
            .aspectRatio(2f / 3.1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(NuvioCardBorder.copy(alpha = 0.4f), Color.Transparent, NuvioCardBorder.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
            .testTag("media_card_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Watch Now", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }

                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
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
    val cardWidth = (screenWidth * 0.5f).coerceIn(160.dp, 220.dp)
    val thumbHeight = cardWidth * 0.56f

    Card(
        modifier = modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("continue_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thumbHeight)
            ) {
                AsyncImage(
                    model = item.backdrop ?: item.poster,
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

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                if (isConnected) NuvioSuccess.copy(alpha = 0.5f) else NuvioPrimary.copy(alpha = 0.5f),
                RoundedCornerShape(20.dp)
            )
            .testTag("server_status_badge"),
        shape = RoundedCornerShape(20.dp),
        color = NuvioSurfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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
                color = NuvioTextPrimary,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
