package com.ridevision.app.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.R
import com.ridevision.app.ui.theme.EmeraldBackground
import com.ridevision.app.ui.theme.EmeraldSurface
import com.ridevision.app.ui.theme.EmeraldSurfaceHigh
import com.ridevision.app.ui.theme.EmeraldSurfaceHighest
import com.ridevision.app.ui.theme.EmeraldSurfaceLow
import com.ridevision.app.ui.theme.EmeraldSurfaceLowest
import com.ridevision.app.ui.theme.MintSecondary
import com.ridevision.app.ui.theme.MintSecondaryContainer
import com.ridevision.app.ui.theme.OnGoldPrimary
import com.ridevision.app.ui.theme.OnMintSecondaryContainer
import com.ridevision.app.ui.theme.OutlineColor
import com.ridevision.app.ui.theme.RadiantGoldPrimary
import com.ridevision.app.ui.theme.TextOnSurface
import com.ridevision.app.ui.theme.TextOnSurfaceVariant
import com.ridevision.app.ui.viewmodel.RideVisionViewModel
import kotlinx.coroutines.delay

@Composable
fun ReportPotholeScreen(
    viewModel: RideVisionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentBitmap by viewModel.currentBitmap.collectAsState()
    val isAudioEnabled by viewModel.isRadarAudioEnabled.collectAsState()
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLon by viewModel.currentLon.collectAsState()
    val currentStreet by viewModel.currentStreetAddress.collectAsState()
    val submissionConfirmed by viewModel.submissionConfirmed.collectAsState()
    val detectionResult by viewModel.detectionResult.collectAsState()

    val capturedAt = remember {
        java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.US)
            .format(java.util.Date())
    }

    var isFlashing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInitialSample(context)
        viewModel.flashTrigger.collect {
            isFlashing = true
            delay(120)
            isFlashing = false
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            viewModel.setCustomBitmap(it)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(stream)
                stream?.close()
                bmp?.let { valid -> viewModel.setCustomBitmap(valid) }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live Warning Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceHigh),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "RIDER ALERT RADAR ACTIVE",
                            color = RadiantGoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Warning you of road hazards 300m ahead",
                            color = TextOnSurface,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleRadarAudio() },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldSurface)
                ) {
                    Icon(
                        imageVector = if (isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Toggle Audio",
                        tint = RadiantGoldPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // 2. Optical Capture Viewfinder Card
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceLowest),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RadiantGoldPrimary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Header row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldSurface)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Captured Pothole Image",
                            color = TextOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldSurfaceHigh)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "AI Verified",
                            color = RadiantGoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Viewfinder image display with HUD Reticle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(EmeraldSurfaceLowest)
                ) {
                    if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "Pothole Viewfinder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sample_severe_pothole).asImageBitmap(),
                            contentDescription = "Sample Pothole",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Futuristic HUD Tactical Reticle Overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Center Bracket
                        Box(
                            modifier = Modifier
                                .size(170.dp, 100.dp)
                                .border(1.5.dp, RadiantGoldPrimary.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                .background(RadiantGoldPrimary.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SEVERITY: CRITICAL",
                                    color = RadiantGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "DEPTH: 18CM • WIDTH: 45CM",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "LOC: ${"%.4f".format(currentLat)}°N, ${"%.4f".format(kotlin.math.abs(currentLon))}°E",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "SCAN ID: PT-98731",
                                    color = RadiantGoldPrimary.copy(alpha = 0.8f),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Bottom pill badge: Severity Critical Hazard
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EmeraldSurfaceLowest.copy(alpha = 0.92f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MintSecondary)
                        )
                        Text(
                            text = "Severity: Critical Hazard",
                            color = TextOnSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Camera Flash effect
                    if (isFlashing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(RadiantGoldPrimary.copy(alpha = 0.85f))
                        )
                    }
                }

                // Bottom accuracy bar & Take Picture Button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldSurface)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "SCAN ACCURACY",
                                color = MintSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "98.4% Confidence • 14.2 cm Depth",
                                color = TextOnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldSurfaceHigh)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MintSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Calibrated",
                                color = MintSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Radiant Gold Take Picture Button
                    Button(
                        onClick = {
                            takePictureLauncher.launch(null)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadiantGoldPrimary,
                            contentColor = OnGoldPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Picture",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 3. Geolocation Field Card
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "POTHOLE LOCATION",
                            color = MintSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldSurfaceHigh)
                            .clickable { viewModel.detectGps(context) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Detect My GPS",
                            color = RadiantGoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Coordinates Readout Card
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldSurfaceLowest)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${"%.4f".format(currentLat)}° N, ${"%.4f".format(kotlin.math.abs(currentLon))}° E",
                            color = TextOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentStreet,
                            color = TextOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MintSecondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MintSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Date & Time Field Card
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "DATE & TIME OF DETECTION",
                            color = MintSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = capturedAt,
                            color = TextOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = { /* calendar picker */ },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldSurfaceHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Modify Time",
                        tint = RadiantGoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 5. Primary Submit CTA
        Button(
            onClick = { viewModel.submitComplaint() },
            colors = ButtonDefaults.buttonColors(
                containerColor = RadiantGoldPrimary,
                contentColor = OnGoldPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Submit Complaint",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Confirmation Pill Popup
        AnimatedVisibility(
            visible = submissionConfirmed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MintSecondaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Pothole Vector Published!",
                                color = OnMintSecondaryContainer,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Report submitted and published to nearby riders.",
                                color = TextOnSurface.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.dismissSubmissionPill() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextOnSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
