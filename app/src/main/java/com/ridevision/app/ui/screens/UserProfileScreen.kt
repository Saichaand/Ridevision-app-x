package com.ridevision.app.ui.screens

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.ui.theme.EmeraldBackground
import com.ridevision.app.ui.theme.EmeraldSurface
import com.ridevision.app.ui.theme.EmeraldSurfaceHigh
import com.ridevision.app.ui.theme.EmeraldSurfaceHighest
import com.ridevision.app.ui.theme.EmeraldSurfaceLow
import com.ridevision.app.ui.theme.EmeraldSurfaceLowest
import com.ridevision.app.ui.theme.HazardErrorContainer
import com.ridevision.app.ui.theme.MintSecondary
import com.ridevision.app.ui.theme.MintSecondaryContainer
import com.ridevision.app.ui.theme.OnGoldPrimary
import com.ridevision.app.ui.theme.OnMintSecondaryContainer
import com.ridevision.app.ui.theme.RadiantGoldContainer
import com.ridevision.app.ui.theme.RadiantGoldPrimary
import com.ridevision.app.ui.theme.TextOnSurface
import com.ridevision.app.ui.theme.TextOnSurfaceVariant
import com.ridevision.app.ui.viewmodel.RideVisionViewModel

@Composable
fun UserProfileScreen(
    viewModel: RideVisionViewModel,
    modifier: Modifier = Modifier
) {
    val isLoginView by viewModel.isLoginView.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var usernameInput by remember { mutableStateOf("alex.vance@ridevision.net") }
    var passwordInput by remember { mutableStateOf("PrecisionSentinel98!") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Switcher Pill Banner (Active Profile vs Login Gateway)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(EmeraldSurfaceHigh)
                .padding(4.dp)
        ) {
            // Active Profile Pill
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (!isLoginView) RadiantGoldPrimary else Color.Transparent)
                    .clickable { viewModel.setLoginView(false) }
                    .padding(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (!isLoginView) OnGoldPrimary else TextOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Active Profile",
                        color = if (!isLoginView) OnGoldPrimary else TextOnSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Login Gateway Pill
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(if (isLoginView) RadiantGoldPrimary else Color.Transparent)
                    .clickable { viewModel.setLoginView(true) }
                    .padding(vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isLoginView) OnGoldPrimary else TextOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Login Gateway",
                        color = if (isLoginView) OnGoldPrimary else TextOnSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!isLoginView) {
            // ==================== STATE 1: ACTIVE AUTHENTICATED PROFILE ====================

            // 1. Hero Cockpit Card
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceHigh),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar with verified badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(EmeraldSurfaceHighest)
                                    .border(1.dp, RadiantGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(36.dp))
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(RadiantGoldPrimary)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = OnGoldPrimary, modifier = Modifier.size(13.dp))
                            }
                        }

                        // Name & ID
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ID ${profile.riderId}",
                                color = RadiantGoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = profile.fullName,
                                color = TextOnSurface,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(RadiantGoldPrimary))
                                Text(profile.statusTitle, color = MintSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tier Badge Sub-card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceLowest.copy(alpha = 0.85f))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RadiantGoldContainer.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("RIDER SAFETY STATUS", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(profile.safetyTier, color = RadiantGoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RadiantGoldPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(profile.tierSubtitle, color = RadiantGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. Quick Stats Telemetry Ribbon (3 columns)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                // Verified
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("VERIFIED", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${profile.verifiedCount}", color = RadiantGoldPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("Hazards", color = MintSecondary, fontSize = 10.sp)
                    }
                }

                // Precision
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("PRECISION", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(profile.precisionScore, color = MintSecondary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("Score", color = TextOnSurfaceVariant, fontSize = 10.sp)
                    }
                }

                // Covered
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("COVERED", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(profile.milesCovered, color = TextOnSurface, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("Miles", color = TextOnSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }

            // 3. Rider Profile & Identity Group
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                ) {
                    Text("RIDER PROFILE & IDENTITY", color = TextOnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(12.dp))
                        Text("Encrypted Vault", color = MintSecondary, fontSize = 10.sp)
                    }
                }

                // Full Legal Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHigh)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("FULL LEGAL NAME", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(profile.fullName, color = TextOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(18.dp))
                }

                // Date of Birth
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHigh)
                    ) {
                        Icon(Icons.Default.Cake, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("DATE OF BIRTH", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${profile.dateOfBirth} (Age ${profile.age})", color = TextOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Residential Base
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHigh)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("RESIDENTIAL BASE", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(profile.residentialBase, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Zone 04 Emergency Response District", color = MintSecondary, fontSize = 10.sp)
                    }
                }

                // Rider Direct Line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHigh)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RIDER DIRECT LINE", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(profile.directLine, color = TextOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MintSecondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("SMS Verified", color = OnMintSecondaryContainer, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Vehicle Spec & Deployment
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHigh)
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("VEHICLE SPEC & DEPLOYMENT", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(profile.vehicleModel, color = TextOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(profile.vehicleSpec, color = MintSecondary, fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Sos, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(14.dp))
                            Text("Emergency ICE: ${profile.emergencyIce}", color = TextOnSurfaceVariant, fontSize = 10.sp)
                        }
                    }
                }
            }

            // 4. HUD Alert Preferences (Audio & Haptics Toggles)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("HUD ALERT PREFERENCES", color = TextOnSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

                // Earbud Audio Ping
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSurfaceHigh)
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Earbud Audio Ping", color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Spatial 3D tone 150m before pothole", color = TextOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = profile.earbudAudioPing,
                        onCheckedChange = { viewModel.toggleEarbudAudio(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnGoldPrimary,
                            checkedTrackColor = RadiantGoldPrimary,
                            uncheckedThumbColor = TextOnSurfaceVariant,
                            uncheckedTrackColor = EmeraldSurfaceHighest
                        )
                    )
                }

                // Handlebar Haptic Pulse
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSurface)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldSurfaceHigh)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text("Handlebar Haptic Pulse", color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Double-buzz for severe fissures", color = TextOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = profile.handlebarHapticPulse,
                        onCheckedChange = { viewModel.toggleHandlebarHaptics(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnGoldPrimary,
                            checkedTrackColor = RadiantGoldPrimary,
                            uncheckedThumbColor = TextOnSurfaceVariant,
                            uncheckedTrackColor = EmeraldSurfaceHighest
                        )
                    )
                }
            }

            // 5. Profile Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                Button(
                    onClick = { /* edit profile */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSurfaceHigh,
                        contentColor = RadiantGoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile Details", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.setLoginView(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HazardErrorContainer.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Out of RideVision", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

        } else {
            // ==================== STATE 2: AUTHENTICATION / LOGIN VIEW ====================

            // 1. Visual Brand Emblem Card
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurfaceHigh),
                shape = RoundedCornerShape(18.dp),
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
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(EmeraldSurfaceLowest)
                            .border(1.dp, RadiantGoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(30.dp))
                    }

                    Text("Rider Cockpit Login", color = TextOnSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Access telemetry memory, road hazard radar logs, and guardian network stats.",
                        color = TextOnSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            // 2. Login Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Rider ID / Username Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("USERNAME OR RIDER ID", color = TextOnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            leadingIcon = {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(20.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = EmeraldSurfaceLow,
                                unfocusedContainerColor = EmeraldSurfaceLow,
                                focusedBorderColor = RadiantGoldPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextOnSurface,
                                unfocusedTextColor = TextOnSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Security Key / Password Input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SECURITY KEY / PASSWORD", color = TextOnSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                            Text("Forgot?", color = RadiantGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            leadingIcon = {
                                Icon(Icons.Default.LockOpen, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = TextOnSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = EmeraldSurfaceLow,
                                unfocusedContainerColor = EmeraldSurfaceLow,
                                focusedBorderColor = RadiantGoldPrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextOnSurface,
                                unfocusedTextColor = TextOnSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Remember Me & FIDO2
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RadiantGoldPrimary,
                                    checkmarkColor = OnGoldPrimary
                                )
                            )
                            Text("Remember this mount unit", color = TextOnSurface, fontSize = 12.sp)
                        }
                        Text("FIDO2 Ready", color = MintSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Primary Login Button
                    Button(
                        onClick = { viewModel.performLogin(usernameInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadiantGoldPrimary,
                            contentColor = OnGoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log In to RideVision", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    // Fast Biometric FaceID Pass
                    Button(
                        onClick = { viewModel.performLogin("Alex Vance (Biometric)") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSurfaceHigh,
                            contentColor = RadiantGoldPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(Icons.Default.Contactless, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Quick FaceID Unlock", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. Security Footnote
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldSurfaceLow)
                    .padding(14.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(18.dp))
                Text(
                    text = "RideVision telemetry is protected by zero-knowledge encryption. Local road hazard recordings remain anonymized until user cloud sync confirmation.",
                    color = TextOnSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
