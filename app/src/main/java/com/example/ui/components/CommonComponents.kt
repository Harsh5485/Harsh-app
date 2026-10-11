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
    Card(
        modifier = modifier
            .width(130.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, NuvioCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("media_card_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = NuvioCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Smooth Shimmer/Gradient Background Placeholder to prevent black box during poster scroll
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1C2232),
                                Color(0xFF121622),
                                Color(0xFF1C2232)
                            )
                        )
                    )
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

            // Gradient at bottom for title contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                NuvioBackground.copy(alpha = 0.85f),
                                NuvioBackground
                            )
                        )
                    )
            )

            // Rating & Type Badges at top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedRating = formatRating(item.imdbRating)
                if (!formattedRating.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.border(0.5.dp, NuvioGold.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NuvioGold,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formattedRating,
                                color = NuvioGold,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = NuvioPrimary.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = if (item.type.equals("series", true)) "SERIES" else "MOVIE",
                        color = Color.Black,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Title & Year info at bottom - Full Title visible on 2 lines
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(5.dp)
            ) {
                Text(
                    text = item.name,
                    color = NuvioTextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = item.year ?: item.releaseInfo ?: item.genres?.firstOrNull() ?: ""
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = NuvioTextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .testTag("hero_banner")
    ) {
        // High-res backdrop image
        AsyncImage(
            model = item.background ?: item.poster,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Vignette gradients (top, sides, bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NuvioBackground.copy(alpha = 0.5f),
                            Color.Transparent,
                            NuvioBackground.copy(alpha = 0.8f),
                            NuvioBackground
                        )
                    )
                )
        )

        // Hero Info Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Genre and Type tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NuvioPrimary,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = if (item.type.equals("series", true)) "SERIES" else "FEATURE FILM",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                val formattedHeroRating = formatRating(item.imdbRating)
                if (!formattedHeroRating.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = NuvioGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedHeroRating,
                            color = NuvioTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!item.year.isNullOrBlank()) {
                    Text(
                        text = item.year,
                        color = NuvioTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Title
            Text(
                text = item.name,
                color = NuvioTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 32.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Synopsis preview
            if (!item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    color = NuvioTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NuvioPrimary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Play",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDetailClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NuvioTextPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = Brush.horizontalGradient(listOf(NuvioCardBorder, NuvioTextSecondary))
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("hero_detail_button")
                ) {
                    Text(
                        text = "Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
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
    Card(
        modifier = modifier
            .width(220.dp)
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
                    .height(115.dp)
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = NuvioTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${items.size} titles",
                color = NuvioTextMuted,
                fontSize = 12.sp
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
