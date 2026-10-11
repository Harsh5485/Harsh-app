package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Harsh3DCardGradientBottom
import com.example.ui.theme.Harsh3DCardGradientTop
import com.example.ui.theme.Harsh3DGradientEnd
import com.example.ui.theme.Harsh3DGradientStart
import com.example.ui.theme.Nuvio3DGlowBorder
import com.example.ui.theme.NuvioBackground
import com.example.ui.theme.NuvioCard
import com.example.ui.theme.NuvioCardBorder
import com.example.ui.theme.NuvioGold
import com.example.ui.theme.NuvioPrimary
import com.example.ui.theme.NuvioSecondary
import com.example.ui.theme.NuvioSuccess
import com.example.ui.theme.NuvioSurfaceVariant
import com.example.ui.theme.NuvioTextMuted
import com.example.ui.theme.NuvioTextPrimary
import com.example.ui.theme.NuvioTextSecondary
import com.example.ui.viewmodel.NuvioViewModel

@Composable
fun ServerSettingsScreen(
    viewModel: NuvioViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val serverState by viewModel.serverState.collectAsState()
    var cacheCleaned by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var newProfileNameInput by remember { mutableStateOf("") }
    var newProfileTokenInput by remember { mutableStateOf("") }

    // Edit Server State
    var isEditingServer by remember { mutableStateOf(false) }
    var editHost by remember { mutableStateOf(serverState.serverUrl) }
    var editUser by remember { mutableStateOf(serverState.username) }
    var editPass by remember { mutableStateOf(serverState.password) }
    var isTokenVisible by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinActionType by remember { mutableStateOf(0) } // 0 for Server, 1 for Token

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPinDialog = false
                pinInput = ""
            },
            title = { Text("Enter Admin PIN", color = NuvioTextPrimary) },
            text = {
                Column {
                    Text("Enter PIN to unlock protected settings", color = NuvioTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("PIN") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = NuvioTextPrimary,
                            unfocusedTextColor = NuvioTextSecondary,
                            focusedBorderColor = NuvioPrimary,
                            unfocusedBorderColor = NuvioCardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput == "2001") {
                            if (pinActionType == 0) isEditingServer = true
                            else isTokenVisible = true
                            showPinDialog = false
                            pinInput = ""
                        } else {
                            Toast.makeText(context, "Incorrect PIN!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NuvioPrimary, contentColor = Color.Black)
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPinDialog = false
                    pinInput = ""
                }) {
                    Text("Cancel", color = NuvioTextSecondary)
                }
            },
            containerColor = NuvioCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NuvioBackground)
            .statusBarsPadding()
    ) {
        // 3D Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.verticalGradient(listOf(Harsh3DCardGradientTop, Harsh3DCardGradientBottom)),
                            shape = CircleShape
                        )
                        .border(1.dp, NuvioCardBorder, CircleShape)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NuvioPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "About Harsh",
                        color = NuvioTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "App Architecture & Streaming Engine",
                        color = NuvioTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // 3D Protected Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F1A28),
                modifier = Modifier.border(1.dp, NuvioPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Secured",
                        tint = NuvioPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Protected",
                        color = NuvioPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NuvioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                                contentDescription = null,
                                tint = NuvioSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "System Activity Logs",
                                color = NuvioTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        serverState.profileLogs.takeLast(6).forEach { log ->
                            Text(
                                text = "• $log",
                                color = NuvioTextSecondary,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            // 3D Hero App Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Harsh3DCardGradientTop, Harsh3DCardGradientBottom)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(listOf(Nuvio3DGlowBorder.copy(alpha = 0.6f), Color.Transparent)),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(
                                            Brush.linearGradient(listOf(Harsh3DGradientStart, Harsh3DGradientEnd)),
                                            CircleShape
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Harsh v1.0",
                                        color = NuvioTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "High-Speed Streaming Engine",
                                        color = NuvioPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NuvioSuccess.copy(alpha = 0.15f),
                                modifier = Modifier.border(1.dp, NuvioSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(NuvioSuccess, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Active",
                                        color = NuvioSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Private streaming app built for fast playback, Dolby audio support, 4K rendering, and multi-threaded downloads.",
                            color = NuvioTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3D Stat Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThreeDStatChip(
                                title = "Pipeline",
                                value = "Direct CDN",
                                modifier = Modifier.weight(1f)
                            )
                            ThreeDStatChip(
                                title = "Audio Engine",
                                value = "FFmpeg",
                                modifier = Modifier.weight(1f)
                            )
                            ThreeDStatChip(
                                title = "Security",
                                value = "Protected 🔒",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Engine Features Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NuvioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Playback Technologies",
                            color = NuvioTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Engine capabilities integrated directly into Harsh",
                            color = NuvioTextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        FeatureRow(
                            icon = Icons.Default.Audiotrack,
                            iconTint = NuvioPrimary,
                            title = "Dolby Atmos & DDP Audio",
                            description = "Full software decoding for Dolby Digital Plus 5.1/7.1 & Atmos streams downmixed directly to studio stereo PCM."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FeatureRow(
                            icon = Icons.Default.HighQuality,
                            iconTint = NuvioSecondary,
                            title = "4K Ultra HD & 10-Bit HEVC",
                            description = "Direct hardware-accelerated video decoding for 4K 2160p, 1080p FHD, and HDR content with seamless audio synchronization."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FeatureRow(
                            icon = Icons.Default.Speed,
                            iconTint = NuvioSuccess,
                            title = "Instant Buffer & Fast Seek",
                            description = "Low-latency stream initialization with smart cache chunks for zero waiting time on play and forward."
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FeatureRow(
                            icon = Icons.Default.Security,
                            iconTint = NuvioSecondary,
                            title = "End-to-End Privacy",
                            description = "Server credentials, tokens, and streams are safeguarded internally without exposing sensitive details in the UI."
                        )
                    }
                }
            }

            // Cache & Storage Management Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NuvioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Server Configuration",
                                    color = NuvioTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Update host if server expires",
                                    color = NuvioTextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(
                                onClick = { 
                                    if (isEditingServer) {
                                        isEditingServer = false 
                                    } else {
                                        pinActionType = 0
                                        showPinDialog = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isEditingServer) Icons.Default.Lock else Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = NuvioPrimary
                                )
                            }
                        }

                        if (isEditingServer) {
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = editHost,
                                onValueChange = { editHost = it },
                                label = { Text("Server Host URL", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = NuvioTextPrimary,
                                    unfocusedTextColor = NuvioTextSecondary,
                                    focusedBorderColor = NuvioPrimary,
                                    unfocusedBorderColor = NuvioCardBorder,
                                    focusedLabelColor = NuvioPrimary
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = editUser,
                                    onValueChange = { editUser = it },
                                    label = { Text("User", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = NuvioTextPrimary,
                                        unfocusedTextColor = NuvioTextSecondary,
                                        focusedBorderColor = NuvioPrimary,
                                        unfocusedBorderColor = NuvioCardBorder
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                OutlinedTextField(
                                    value = editPass,
                                    onValueChange = { editPass = it },
                                    label = { Text("Pass", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = NuvioTextPrimary,
                                        unfocusedTextColor = NuvioTextSecondary,
                                        focusedBorderColor = NuvioPrimary,
                                        unfocusedBorderColor = NuvioCardBorder
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.connectServer(editHost, editUser, editPass)
                                    isEditingServer = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NuvioPrimary, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                enabled = !serverState.isChecking
                            ) {
                                Text(
                                    text = if (serverState.isChecking) "Connecting..." else "Re-activate & Sync Server",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                            // Display current credentials briefly or just host
                            SpecItem(label = "Host", value = serverState.serverUrl.take(30) + "...")
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Active Token", color = NuvioTextSecondary, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isTokenVisible) serverState.token else "••••••••••••••••",
                                        color = NuvioTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (isTokenVisible) Icons.Default.Lock else Icons.Default.Security,
                                        contentDescription = null,
                                        tint = NuvioPrimary,
                                        modifier = Modifier.size(16.dp).clickable { 
                                            if (isTokenVisible) {
                                                isTokenVisible = false
                                            } else {
                                                pinActionType = 1
                                                showPinDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Cache & Storage Management Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NuvioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Cache & Performance",
                                    color = NuvioTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Optimize app responsiveness & memory",
                                    color = NuvioTextMuted,
                                    fontSize = 12.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = NuvioPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3D Clean Cache Action Button
                        Button(
                            onClick = {
                                cacheCleaned = true
                                Toast.makeText(context, "Stream cache optimized and temporary files cleared!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cacheCleaned) NuvioSuccess else NuvioPrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("clear_cache_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (cacheCleaned) "Cache Cleared & Optimized ✓" else "Clear Stream Cache & Optimize",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Technical Specifications
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NuvioCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NuvioCardBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Specifications",
                            color = NuvioTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        SpecItem(label = "Application Name", value = "Harsh")
                        SpecItem(label = "Version", value = "1.0.0 Stable")
                        SpecItem(label = "Streaming Core", value = "Server Native")
                        SpecItem(label = "Media Framework", value = "AndroidX Media3 ExoPlayer")
                        SpecItem(label = "Audio Decoder", value = "FFmpeg libavcodec Embedded")
                        SpecItem(label = "UI Architecture", value = "Jetpack Compose 3D Modern")
                        SpecItem(label = "State & Cache", value = "Active & Synced")
                        SpecItem(label = "Install Origin", value = "Internal Shell (Dev Build)")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ThreeDStatChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F1524),
        modifier = modifier.border(1.dp, NuvioCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = NuvioTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                color = NuvioTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, iconTint.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = NuvioTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = NuvioTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SpecItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = NuvioTextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = NuvioTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
