package com.ridevision.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.R
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
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
import com.ridevision.app.ui.theme.OnMintSecondaryContainer
import com.ridevision.app.ui.theme.OutlineColor
import com.ridevision.app.ui.theme.RadiantGoldPrimary
import com.ridevision.app.ui.theme.TextOnSurface
import com.ridevision.app.ui.theme.TextOnSurfaceVariant
import com.ridevision.app.ui.viewmodel.AppTab
import com.ridevision.app.ui.viewmodel.RideVisionViewModel

@Composable
fun ComplaintHistoryScreen(
    viewModel: RideVisionViewModel,
    modifier: Modifier = Modifier
) {
    val potholes by viewModel.myReportedPotholes.collectAsState()
    val activeTab by viewModel.historyFilterTab.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()

    val totalCount = potholes.size
    val resolvedCount = potholes.count { it.status == PotholeStatus.VERIFIED_FIXED }
    val progressCount = potholes.count { it.status == PotholeStatus.IN_PROGRESS }
    val criticalCount = potholes.count { it.status == PotholeStatus.ACTIVE }

    val filterTabs = listOf(
        Pair("all", "All Reports ($totalCount)"),
        Pair("pending", "Pending ($criticalCount)"),
        Pair("progress", "In Progress ($progressCount)"),
        Pair("repaired", "Repaired ($resolvedCount)")
    )

    val filteredList = potholes.filter { p ->
        val matchesTab = when (activeTab) {
            "pending" -> p.status == PotholeStatus.ACTIVE
            "progress" -> p.status == PotholeStatus.IN_PROGRESS
            "repaired" -> p.status == PotholeStatus.VERIFIED_FIXED
            else -> true
        }
        val matchesQuery = searchQuery.isBlank() ||
                p.address.contains(searchQuery, ignoreCase = true) ||
                p.ticketNumber.contains(searchQuery, ignoreCase = true) ||
                p.notes.contains(searchQuery, ignoreCase = true)

        matchesTab && matchesQuery
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hazard Registry Analytics Bento Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Analytics, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                            Text("Hazard Registry", color = TextOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MintSecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("SYNCED", color = MintSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }

                    // 4-grid bento
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Total Reported
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSurface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("TOTAL REPORTED", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("$totalCount", color = TextOnSurface, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" cases", color = MintSecondary, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                                }
                            }
                        }

                        // Resolved
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSurface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("RESOLVED", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("$resolvedCount", color = MintSecondary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" 67%", color = MintSecondary.copy(alpha = 0.7f), fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        // Municipal Review
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSurface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("MUNICIPAL REVIEW", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("$progressCount", color = RadiantGoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" active", color = TextOnSurfaceVariant, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                                }
                            }
                        }

                        // Critical Risk
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSurface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("CRITICAL RISK", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("$criticalCount", color = HazardError, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    Text(" urgent", color = HazardError.copy(alpha = 0.8f), fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Search & Filter Bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setHistorySearchQuery(it) },
                    placeholder = { Text("Search avenue, ticket ID, or landmark...", color = TextOnSurfaceVariant, fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextOnSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = EmeraldSurface,
                        unfocusedContainerColor = EmeraldSurface,
                        focusedBorderColor = RadiantGoldPrimary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextOnSurface,
                        unfocusedTextColor = TextOnSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                )

                // Date filter button
                IconButton(
                    onClick = { /* filter date */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldSurfaceHighest)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Date", tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                }

                // Sort filter button
                IconButton(
                    onClick = { /* sort */ },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldSurfaceHighest)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Sort", tint = TextOnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }

        // 3. Filter Tabs Row (All Reports, Pending, In Progress, Repaired)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(filterTabs) { (key, label) ->
                    val isSelected = activeTab == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) RadiantGoldPrimary else EmeraldSurface)
                            .clickable { viewModel.setHistoryFilter(key) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) OnGoldPrimary else TextOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Empty state when user has no reports
        if (filteredList.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(EmeraldSurfaceHighest)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = RadiantGoldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "No Reported Hazards Found",
                            color = TextOnSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Only pothole complaints reported under your authenticated account appear here. Use the Camera / Report tab to scan and publish road hazards.",
                            color = TextOnSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.setTab(AppTab.REPORT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RadiantGoldPrimary,
                                contentColor = OnGoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Report First Hazard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Hazard Cards List
        items(filteredList, key = { it.id }) { pothole ->
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceHigh),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Photo header with badges
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(EmeraldSurfaceLowest)
                    ) {
                        Image(
                            painter = painterResource(
                                id = pothole.drawableResId ?: when (pothole.severity) {
                                    Severity.SEVERE -> R.drawable.sample_severe_pothole
                                    Severity.MODERATE -> R.drawable.sample_moderate_pothole
                                    Severity.MINOR -> R.drawable.sample_clean_road
                                }
                            ),
                            contentDescription = pothole.address,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Transparent,
                                            EmeraldSurfaceHigh
                                        )
                                    )
                                )
                        )

                        // Top Left: Severity Badge
                        val sevBadgeBg = when (pothole.status) {
                            PotholeStatus.VERIFIED_FIXED -> MintSecondaryContainer
                            else -> if (pothole.severity == Severity.SEVERE) HazardErrorContainer else RadiantGoldPrimary
                        }
                        val sevBadgeText = when (pothole.status) {
                            PotholeStatus.VERIFIED_FIXED -> OnMintSecondaryContainer
                            else -> if (pothole.severity == Severity.SEVERE) Color.White else OnGoldPrimary
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(sevBadgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (pothole.status) {
                                    PotholeStatus.VERIFIED_FIXED -> Icons.Default.CheckCircle
                                    else -> if (pothole.severity == Severity.SEVERE) Icons.Default.Warning else Icons.Default.ReportProblem
                                },
                                contentDescription = null,
                                tint = sevBadgeText,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = when (pothole.status) {
                                    PotholeStatus.VERIFIED_FIXED -> "REPAIRED • VERIFIED"
                                    else -> pothole.severity.levelText
                                },
                                color = sevBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Top Right: Ticket ID
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldSurfaceLowest.copy(alpha = 0.85f))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = pothole.ticketNumber,
                                color = RadiantGoldPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Bottom inside photo: Address & Distance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = pothole.address,
                                color = TextOnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmeraldSurfaceLowest.copy(alpha = 0.9f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = pothole.distanceDisplay,
                                    color = if (pothole.status == PotholeStatus.VERIFIED_FIXED) MintSecondary else RadiantGoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Card Body Details
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Lane info & Date
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.NearMe, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(15.dp))
                                Text(pothole.laneInfo, color = TextOnSurfaceVariant, fontSize = 12.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = TextOnSurfaceVariant, modifier = Modifier.size(14.dp))
                                Text(pothole.reportedAt, color = TextOnSurfaceVariant, fontSize = 12.sp)
                            }
                        }

                        // Investigation / Repair status bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldSurface)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            when (pothole.status) {
                                PotholeStatus.ACTIVE -> {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RadiantGoldPrimary))
                                    Text(pothole.statusNote, color = RadiantGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                PotholeStatus.IN_PROGRESS -> {
                                    Icon(Icons.Default.Handyman, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                    Text(pothole.statusNote, color = MintSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                PotholeStatus.VERIFIED_FIXED -> {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                                    Text(pothole.statusNote, color = MintSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Upvote / Action Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Upvote button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (pothole.status == PotholeStatus.VERIFIED_FIXED) MintSecondaryContainer else EmeraldSurface)
                                        .clickable { viewModel.upvoteHazard(pothole.id) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (pothole.status == PotholeStatus.VERIFIED_FIXED) Icons.Default.Check else Icons.Default.ThumbUp,
                                        contentDescription = null,
                                        tint = if (pothole.status == PotholeStatus.VERIFIED_FIXED) OnMintSecondaryContainer else MintSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${pothole.confirmationCount}",
                                        color = if (pothole.status == PotholeStatus.VERIFIED_FIXED) OnMintSecondaryContainer else MintSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text("riders confirmed", color = TextOnSurfaceVariant, fontSize = 11.sp)
                            }

                            // Secondary button ("Route Map", "Status Log", "Receipt")
                            Button(
                                onClick = {
                                    viewModel.setTab(AppTab.SAFE_ROUTE)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (pothole.status) {
                                        PotholeStatus.ACTIVE -> RadiantGoldPrimary
                                        PotholeStatus.IN_PROGRESS -> EmeraldSurfaceHighest
                                        PotholeStatus.VERIFIED_FIXED -> EmeraldSurface
                                    },
                                    contentColor = when (pothole.status) {
                                        PotholeStatus.ACTIVE -> OnGoldPrimary
                                        else -> TextOnSurface
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = when (pothole.status) {
                                        PotholeStatus.ACTIVE -> Icons.Default.Map
                                        PotholeStatus.IN_PROGRESS -> Icons.Default.Update
                                        PotholeStatus.VERIFIED_FIXED -> Icons.Default.ReceiptLong
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (pothole.status) {
                                        PotholeStatus.ACTIVE -> "Route Map"
                                        PotholeStatus.IN_PROGRESS -> "Status Log"
                                        PotholeStatus.VERIFIED_FIXED -> "Receipt"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Bottom Card: Encountered an Unmapped Hazard?
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(EmeraldSurfaceHigh)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(20.dp))
                    }

                    Text("Encountered an Unmapped Hazard?", color = TextOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "RideVision HUD computer vision tags impact shocks automatically, but manual reporting alerts the city maintenance squad immediately.",
                        color = TextOnSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.setTab(AppTab.REPORT) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadiantGoldPrimary,
                            contentColor = OnGoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log New Road Hazard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
