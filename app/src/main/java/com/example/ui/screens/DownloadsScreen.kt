package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.theme.NuvioPrimaryBright
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DownloadStatus
import com.example.data.model.DownloadTask
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCard
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSecondary
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadsScreen(
    viewModel: NuvioViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()
    val downloadPath by viewModel.downloadPath.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    var isEditingPath by remember { mutableStateOf(false) }
    var customPathInput by remember { mutableStateOf(downloadPath) }

    var isAddingDirectLink by remember { mutableStateOf(false) }
    var directUrlInput by remember { mutableStateOf("") }
    var directTitleInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Downloads & Offline Storage",
                        color = NuvioTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Harsh Download Engine • Multi-threaded high-speed stream engine",
                        color = NuvioTextMuted,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Storage Folder Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NuvioCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(NuvioPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = NuvioPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Storage Folder",
                                            color = NuvioTextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = downloadPath,
                                            color = NuvioTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(onClick = {
                                    customPathInput = downloadPath
                                    isEditingPath = !isEditingPath
                                }) {
                                    Icon(
                                        imageVector = if (isEditingPath) Icons.Default.Check else Icons.Default.Edit,
                                        contentDescription = "Edit Path",
                                        tint = NuvioPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = isEditingPath) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    OutlinedTextField(
                                        value = customPathInput,
                                        onValueChange = { customPathInput = it },
                                        label = { Text("Custom Path", fontSize = 11.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NuvioPrimary,
                                            unfocusedBorderColor = NuvioCardBorder,
                                            focusedTextColor = NuvioTextPrimary,
                                            unfocusedTextColor = NuvioTextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Quick Presets:",
                                        color = NuvioTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "/storage/emulated/0/Download/Harsh"
                                        ).forEach { pathPreset ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = NuvioSurfaceVariant,
                                                modifier = Modifier.clickable {
                                                    customPathInput = pathPreset
                                                    viewModel.updateDownloadPath(pathPreset)
                                                    isEditingPath = false
                                                }
                                            ) {
                                                Text(
                                                    text = pathPreset.substringAfterLast('/'),
                                                    color = NuvioSecondary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            viewModel.updateDownloadPath(customPathInput)
                                            isEditingPath = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NuvioPrimary, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Folder Path", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Downloads List
            if (downloads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, start = 30.dp, end = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = NuvioTextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Active Downloads",
                                color = NuvioTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Click the Download button on any episode or stream to download movies & series directly to your phone storage.",
                                color = NuvioTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(downloads, key = { it.downloadId }) { task ->
                    DownloadCardItem(
                        task = task,
                        onPlay = { viewModel.playDownloadedTask(task) },
                        onPauseResume = { viewModel.pauseOrResumeDownloadTask(task) },
                        onDelete = { viewModel.deleteDownloadTask(task.downloadId) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadCardItem(
    task: DownloadTask,
    onPlay: () -> Unit,
    onPauseResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val thumbWidth = (screenWidth * 0.18f).coerceIn(60.dp, 80.dp)
    val thumbHeight = thumbWidth * 1.3f

    var isCardFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isCardFocused) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "dlScale"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCardFocused) NuvioPrimaryBright else NuvioCard),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onFocusChanged { isCardFocused = it.isFocused }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = if (isCardFocused) 3.5.dp else 1.dp,
                color = if (isCardFocused) Color.White else NuvioCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onPlay()
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
            .clickable(onClick = onPlay)
            .testTag("download_card_${task.downloadId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Poster / Quality Badge
                    Box(
                        modifier = Modifier
                            .width(thumbWidth)
                            .height(thumbHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NuvioSurfaceVariant)
                    ) {
                        if (!task.poster.isNullOrBlank()) {
                            AsyncImage(
                                model = task.poster,
                                contentDescription = task.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = NuvioPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        if (task.quality.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(2.dp),
                                color = NuvioPrimary,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = task.quality,
                                    color = Color.Black,
                                    fontSize = 4.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 1.5.dp, vertical = 0.2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = NuvioTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!task.subtitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.subtitle,
                                color = NuvioTextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = task.fileName,
                            color = NuvioTextMuted,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = task.downloadPath,
                            color = NuvioSecondary.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PENDING || task.status == DownloadStatus.PAUSED) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${task.progressPercent}%",
                        color = NuvioPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Speed in the middle
                    if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PAUSED) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (task.status == DownloadStatus.PAUSED) NuvioSecondary else NuvioPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.formattedSpeed,
                                color = if (task.status == DownloadStatus.PAUSED) NuvioSecondary else NuvioPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (task.formattedEta.isNotBlank()) {
                        Text(
                            text = task.formattedEta,
                            color = NuvioTextMuted,
                            fontSize = 10.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { task.progressPercent.toFloat() / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (task.status == DownloadStatus.PAUSED) NuvioSecondary else NuvioPrimary,
                    trackColor = NuvioSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Stats row (Size, Action Buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${task.formattedDownloadedSize} / ${task.formattedTotalSize}",
                        color = NuvioTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (task.status == DownloadStatus.COMPLETED) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF1E3A1E)
                        ) {
                            Text(
                                text = "DONE",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PAUSED || task.status == DownloadStatus.FAILED) {
                        IconButton(
                            onClick = onPauseResume,
                            modifier = Modifier
                                .size(32.dp)
                                .background(NuvioSurfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, NuvioCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = when (task.status) {
                                    DownloadStatus.DOWNLOADING -> Icons.Default.Pause
                                    DownloadStatus.PAUSED -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Refresh
                                },
                                contentDescription = if (task.status == DownloadStatus.FAILED) "Retry Download" else "Pause / Resume",
                                tint = if (task.status == DownloadStatus.FAILED) Color(0xFFFF9800) else NuvioPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                    }

                    Button(
                        onClick = onPlay,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NuvioPrimary,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Play",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
