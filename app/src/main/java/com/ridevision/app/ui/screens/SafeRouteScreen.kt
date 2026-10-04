package com.ridevision.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricMoped
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.SafeRouteOption
import com.ridevision.app.ui.theme.EmeraldBackground
import com.ridevision.app.ui.theme.EmeraldSurface
import com.ridevision.app.ui.theme.EmeraldSurfaceHigh
import com.ridevision.app.ui.theme.EmeraldSurfaceHighest
import com.ridevision.app.ui.theme.EmeraldSurfaceLow
import com.ridevision.app.ui.theme.EmeraldSurfaceLowest
import com.ridevision.app.ui.theme.HazardError
import com.ridevision.app.ui.theme.HazardErrorContainer
import com.ridevision.app.ui.theme.MintSecondary
import com.ridevision.app.ui.theme.MintSecondaryContainer
import com.ridevision.app.ui.theme.OnGoldPrimary
import com.ridevision.app.ui.theme.OutlineColor
import com.ridevision.app.ui.theme.RadiantGoldContainer
import com.ridevision.app.ui.theme.RadiantGoldPrimary
import com.ridevision.app.ui.theme.TextOnSurface
import com.ridevision.app.ui.theme.TextOnSurfaceVariant
import com.ridevision.app.ui.theme.WarmAmberTertiary
import com.ridevision.app.ui.viewmodel.RideVisionViewModel
import com.ridevision.app.ui.viewmodel.TransportMode

@Composable
fun SafeRouteScreen(
    viewModel: RideVisionViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val origin by viewModel.routeOrigin.collectAsState()
    val destination by viewModel.routeDestination.collectAsState()
    val transportMode by viewModel.transportMode.collectAsState()
    val avoidHoles by viewModel.avoidHoles.collectAsState()
    val selectedRouteId by viewModel.selectedRouteId.collectAsState()
    val isNavigating by viewModel.isNavigating.collectAsState()

    val options = viewModel.safeRouteOptions
    val selectedOption = options.firstOrNull { it.id == selectedRouteId } ?: options[0]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. Interactive Vector Map Canvas with Floating HUD Elements ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .background(EmeraldSurfaceLowest)
        ) {
            // Background Vector Map Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw Grid
                val gridSpacing = 45.dp.toPx()
                var gx = 0f
                while (gx < w) {
                    drawLine(
                        color = Color(0xFF0F3020).copy(alpha = 0.5f),
                        start = Offset(gx, 0f),
                        end = Offset(gx, h),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                    )
                    gx += gridSpacing
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(
                        color = Color(0xFF0F3020).copy(alpha = 0.5f),
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                    )
                    gy += gridSpacing
                }

                // Road Arterials Base
                drawLine(
                    color = Color(0xFF072014),
                    start = Offset(0.15f * w, 0f),
                    end = Offset(0.15f * w, h),
                    strokeWidth = 18.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF072014),
                    start = Offset(0f, 0.45f * h),
                    end = Offset(w, 0.45f * h),
                    strokeWidth = 18.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Dotted Danger Alternate Route (Red)
                val dangerPath = Path().apply {
                    moveTo(0.20f * w, 0.38f * h)
                    cubicTo(
                        0.32f * w, 0.48f * h,
                        0.46f * w, 0.58f * h,
                        0.74f * w, 0.85f * h
                    )
                }
                drawPath(
                    path = dangerPath,
                    color = HazardErrorContainer,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))
                    )
                )

                // Luminous Neon Gold / Emerald Safe Route (Solid Glow)
                val safePath = Path().apply {
                    moveTo(0.20f * w, 0.38f * h)
                    cubicTo(
                        0.25f * w, 0.56f * h,
                        0.38f * w, 0.74f * h,
                        0.74f * w, 0.85f * h
                    )
                }
                // Outer Glow
                drawPath(
                    path = safePath,
                    color = RadiantGoldPrimary.copy(alpha = 0.35f),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
                // Center Trace
                drawPath(
                    path = safePath,
                    color = RadiantGoldPrimary,
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )
                // Inner White Ribbon
                drawPath(
                    path = safePath,
                    color = Color.White.copy(alpha = 0.8f),
                    style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Top Floating Route Planning Capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceLowest.copy(alpha = 0.94f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RadiantGoldPrimary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Waypoint indicator line
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.height(64.dp).padding(vertical = 4.dp)
                            ) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MintSecondary))
                                Box(modifier = Modifier.width(2.dp).height(24.dp).background(MintSecondary.copy(alpha = 0.4f)))
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(RadiantGoldPrimary))
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Start & Destination Inputs
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Start / Origin Field
                                OutlinedTextField(
                                    value = origin,
                                    onValueChange = { viewModel.setRouteOrigin(it) },
                                    label = { Text("START LOCATION", color = MintSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MintSecondaryContainer)
                                                .clickable { viewModel.detectGps(context) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("GPS", color = MintSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MintSecondary,
                                        unfocusedBorderColor = OutlineColor,
                                        focusedTextColor = TextOnSurface,
                                        unfocusedTextColor = TextOnSurface,
                                        focusedContainerColor = EmeraldSurface,
                                        unfocusedContainerColor = EmeraldSurface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Destination Field
                                OutlinedTextField(
                                    value = destination,
                                    onValueChange = { viewModel.setRouteDestination(it) },
                                    label = { Text("DESTINATION", color = RadiantGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PinDrop,
                                            contentDescription = null,
                                            tint = RadiantGoldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = RadiantGoldPrimary,
                                        unfocusedBorderColor = OutlineColor,
                                        focusedTextColor = TextOnSurface,
                                        unfocusedTextColor = TextOnSurface,
                                        focusedContainerColor = EmeraldSurface,
                                        unfocusedContainerColor = EmeraldSurface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Calculate Route Button
                                Button(
                                    onClick = { viewModel.refreshRouteMapsIntel(selectedRouteId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RadiantGoldPrimary,
                                        contentColor = OnGoldPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze & Calculate Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Swap Locations button
                            IconButton(
                                onClick = { viewModel.swapOriginDestination() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldSurfaceHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Locations",
                                    tint = RadiantGoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Mode Chips & Avoid Holes Toggle Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Transport Modes (Ride, Car, Fleet)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TransportMode.values().forEach { mode ->
                                    val isSelected = transportMode == mode
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) RadiantGoldPrimary else EmeraldSurface)
                                            .clickable { viewModel.setTransportMode(mode) }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (mode) {
                                                TransportMode.RIDE -> Icons.Default.TwoWheeler
                                                TransportMode.CAR -> Icons.Default.DirectionsCar
                                                TransportMode.FLEET -> Icons.Default.ElectricMoped
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) OnGoldPrimary else TextOnSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = mode.label,
                                            color = if (isSelected) OnGoldPrimary else TextOnSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            // Avoid Holes Active Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (avoidHoles) MintSecondaryContainer else EmeraldSurface)
                                    .clickable { viewModel.toggleAvoidHoles() }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MintSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text("Avoid Holes", color = MintSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                if (avoidHoles) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MintSecondary))
                                }
                            }
                        }
                    }
                }
            }

            // Map Pins & Badges
            // 1. Origin Pin
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 64.dp, top = 160.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MintSecondaryContainer)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Origin", color = MintSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 2. Hazard Cluster 1 (Red crater on Route B)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 50.dp, bottom = 40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(HazardErrorContainer)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = HazardError, modifier = Modifier.size(16.dp))
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceLowest.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("14CM CRATER", color = HazardError, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Cluster of 4 holes", color = TextOnSurfaceVariant, fontSize = 9.sp)
                    }
                }
            }

            // Route B label
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 70.dp, top = 50.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmeraldSurfaceHigh.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("Route B: +6m rough", color = TextOnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }

            // 3. Hairline Crack chip on Bypass
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 90.dp, top = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarmAmberTertiary))
                Text("3cm hairline crack", color = WarmAmberTertiary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }

            // 4. Destination Pin
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 90.dp, bottom = 40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(RadiantGoldPrimary)
                ) {
                    Icon(Icons.Default.SportsScore, contentDescription = null, tint = OnGoldPrimary, modifier = Modifier.size(20.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RadiantGoldContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Destination", color = OnGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Safe Route Ribbon Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 60.dp, bottom = 90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MintSecondaryContainer.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(14.dp))
                Text("96% Glass Smooth", color = MintSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Right side Cockpit Floating Controls Stack
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 180.dp, end = 12.dp)
            ) {
                // Layers
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                        .border(1.dp, EmeraldSurfaceHighest, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(20.dp))
                }

                // 3D
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                        .border(1.dp, EmeraldSurfaceHighest, RoundedCornerShape(12.dp))
                ) {
                    Text("3D", color = RadiantGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }

                // Recenter
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                        .border(1.dp, EmeraldSurfaceHighest, RoundedCornerShape(12.dp))
                        .clickable { viewModel.detectGps(context) }
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(20.dp))
                }

                // Zoom (+ / -)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                        .border(1.dp, EmeraldSurfaceHighest, RoundedCornerShape(12.dp))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(38.dp, 34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextOnSurface, modifier = Modifier.size(18.dp))
                    }
                    Box(modifier = Modifier.width(38.dp).height(1.dp).background(EmeraldSurfaceHighest))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(38.dp, 34.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = TextOnSurface, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // --- 2. Bottom Sheet / Route Comparison Drawer ---
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceLowest.copy(alpha = 0.98f)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Grab Bar / Quick Status Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RadiantGoldPrimary))
                        Text(
                            text = selectedOption.name,
                            color = TextOnSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MintSecondaryContainer)
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(13.dp))
                        Text("SAFEST ROUTE", color = MintSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Road Surface & Hazard Telemetry Bento Grid (3 columns)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Potholes Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("POTHOLES", color = MintSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Shield, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${selectedOption.potholeCount}",
                                color = MintSecondary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("Low hazard", color = TextOnSurfaceVariant, fontSize = 10.sp)
                        }
                    }

                    // ETA Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ETA", color = RadiantGoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Timer, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${selectedOption.etaMin}",
                                    color = RadiantGoldPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(" min", color = TextOnSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(bottom = 2.dp))
                            }
                            Text("${selectedOption.distanceKm} km total", color = TextOnSurfaceVariant, fontSize = 10.sp)
                        }
                    }

                    // Quality Index Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldSurface)
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("INDEX", color = WarmAmberTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Tune, contentDescription = null, tint = WarmAmberTertiary, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${selectedOption.conditionIndex}",
                                    color = TextOnSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text("/100", color = WarmAmberTertiary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 2.dp))
                            }
                            Text("Silky smooth", color = TextOnSurfaceVariant, fontSize = 10.sp)
                        }
                    }
                }

                // Alternative Safe Corridors List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ALTERNATIVE SAFE CORRIDORS",
                        color = TextOnSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    options.forEach { opt ->
                        val isSelected = selectedRouteId == opt.id
                        val itemBg = if (isSelected) EmeraldSurfaceHigh else EmeraldSurface.copy(alpha = 0.6f)
                        val borderCol = if (isSelected) RadiantGoldPrimary.copy(alpha = 0.4f) else Color.Transparent

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(itemBg)
                                .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectRoute(opt.id) }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                opt.isOptimal -> RadiantGoldPrimary.copy(alpha = 0.2f)
                                                opt.riskLabel == "Severe Risk" -> HazardErrorContainer.copy(alpha = 0.4f)
                                                else -> EmeraldSurfaceHighest
                                            }
                                        )
                                ) {
                                    Icon(
                                        imageVector = when {
                                            opt.isOptimal -> Icons.Default.ElectricBike
                                            opt.riskLabel == "Severe Risk" -> Icons.Default.Report
                                            else -> Icons.Default.AltRoute
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            opt.isOptimal -> RadiantGoldPrimary
                                            opt.riskLabel == "Severe Risk" -> HazardError
                                            else -> TextOnSurfaceVariant
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(opt.name, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        if (opt.isOptimal) {
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MintSecondary))
                                        }
                                    }
                                    Text(
                                        text = opt.description,
                                        color = if (opt.riskLabel == "Severe Risk") HazardError else TextOnSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${opt.etaMin} min",
                                    color = if (opt.isOptimal) RadiantGoldPrimary else TextOnSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = opt.riskLabel,
                                    color = when (opt.riskLabel) {
                                        "Optimal" -> MintSecondary
                                        "Moderate" -> WarmAmberTertiary
                                        else -> HazardError
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Primary Start Navigation Launcher CTA
                Button(
                    onClick = { viewModel.toggleNavigation() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RadiantGoldPrimary,
                        contentColor = OnGoldPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNavigating) "Navigation Active (Pause)" else "Start Safe Navigation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
