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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.ui.theme.EmeraldBackground
import com.ridevision.app.ui.theme.EmeraldSurface
import com.ridevision.app.ui.theme.EmeraldSurfaceHigh
import com.ridevision.app.ui.theme.EmeraldSurfaceHighest
import com.ridevision.app.ui.theme.EmeraldSurfaceLowest
import com.ridevision.app.ui.theme.HazardErrorContainer
import com.ridevision.app.ui.theme.MintSecondary
import com.ridevision.app.ui.theme.MintSecondaryContainer
import com.ridevision.app.ui.theme.OnGoldPrimary
import com.ridevision.app.ui.theme.OnMintSecondaryContainer
import com.ridevision.app.ui.theme.OutlineColor
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
    val profile by viewModel.userProfile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var isEditingVehicle by remember { mutableStateOf(false) }
    var vehicleModelInput by remember(profile.vehicleModel) { mutableStateOf(profile.vehicleModel) }
    var vehicleSpecInput by remember(profile.vehicleSpec) { mutableStateOf(profile.vehicleSpec) }
    var residentialBaseInput by remember(profile.residentialBase) { mutableStateOf(profile.residentialBase) }
    var directLineInput by remember(profile.directLine) { mutableStateOf(profile.directLine) }
    var emergencyIceInput by remember(profile.emergencyIce) { mutableStateOf(profile.emergencyIce) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Cockpit Profile Card (Authenticated User)
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
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = RadiantGoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(RadiantGoldPrimary)
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = OnGoldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    // Name & Email
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.riderId,
                            color = RadiantGoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = profile.displayName.ifBlank { currentUser?.displayName ?: "Road Sentinel" },
                            color = TextOnSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.email.ifBlank { currentUser?.email ?: "Authenticated Account" },
                            color = TextOnSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MintSecondary))
                            Text("Authenticated with Firebase", color = MintSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                            Text("RIDER SENTINEL STATUS", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(profile.safetyTier, color = RadiantGoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = profile.tierSubtitle,
                        color = OnMintSecondaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintSecondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // 2. Metrics & Telemetry Triad (From Cloud Firestore)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stat 1: Verified
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HAZARDS LOGGED", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${profile.verifiedCount}", color = RadiantGoldPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Synced to Cloud", color = MintSecondary, fontSize = 9.sp)
                }
            }

            // Stat 2: Precision
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("AI PRECISION", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(profile.precisionScore, color = TextOnSurface, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Optical Scans", color = TextOnSurfaceVariant, fontSize = 9.sp)
                }
            }

            // Stat 3: Distance
            Card(
                colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CORRIDOR PATROL", color = TextOnSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(profile.milesCovered, color = TextOnSurface, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("GPS Tracked", color = TextOnSurfaceVariant, fontSize = 9.sp)
                }
            }
        }

        // 3. Cloud Database Connection Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(18.dp))
                    Text("CLOUD FIRESTORE ACTIVE", color = MintSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }

                Text(
                    text = "All reported road fissures, hazard votes, and repair confirmations sync in real time to the decentralized Mangaluru municipal registry.",
                    color = TextOnSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // 4. Hardware & Telemetry Link Preferences
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                    Text("COMMUTER TELEMETRY LINK", color = RadiantGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }

                // Earbud Audio Alert
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = TextOnSurfaceVariant, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Earbud Chime Ahead", color = TextOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Audio warning on 250m cone", color = TextOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = profile.earbudAudioPing,
                        onCheckedChange = { viewModel.toggleEarbudAudio(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnGoldPrimary,
                            checkedTrackColor = RadiantGoldPrimary
                        )
                    )
                }

                // Handlebar Haptics
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = TextOnSurfaceVariant, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Handlebar Haptic Pulse", color = TextOnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Vibrate phone on severe craters", color = TextOnSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = profile.handlebarHapticPulse,
                        onCheckedChange = { viewModel.toggleHandlebarHaptics(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnGoldPrimary,
                            checkedTrackColor = RadiantGoldPrimary
                        )
                    )
                }
            }
        }

        // 5. Commuter Specification & Base Card (User Editable)
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = RadiantGoldPrimary, modifier = Modifier.size(18.dp))
                        Text("COMMUTER VEHICLE & BASE", color = RadiantGoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldSurfaceHigh)
                            .clickable { isEditingVehicle = !isEditingVehicle }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditingVehicle) Icons.Default.Cancel else Icons.Default.Edit,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isEditingVehicle) "Cancel" else "Edit Details",
                            color = RadiantGoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!isEditingVehicle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registered Ride", color = TextOnSurfaceVariant, fontSize = 13.sp)
                        Text(profile.vehicleModel, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vehicle Specs", color = TextOnSurfaceVariant, fontSize = 13.sp)
                        Text(profile.vehicleSpec, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Home Sector", color = TextOnSurfaceVariant, fontSize = 13.sp)
                        Text(profile.residentialBase, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Direct Line", color = TextOnSurfaceVariant, fontSize = 13.sp)
                        Text(profile.directLine, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Emergency ICE", color = TextOnSurfaceVariant, fontSize = 13.sp)
                        Text(profile.emergencyIce, color = TextOnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedTextField(
                        value = vehicleModelInput,
                        onValueChange = { vehicleModelInput = it },
                        label = { Text("Registered Vehicle Model", color = TextOnSurfaceVariant, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGoldPrimary,
                            unfocusedBorderColor = OutlineColor,
                            focusedTextColor = TextOnSurface,
                            unfocusedTextColor = TextOnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = vehicleSpecInput,
                        onValueChange = { vehicleSpecInput = it },
                        label = { Text("Vehicle Class / Specification", color = TextOnSurfaceVariant, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGoldPrimary,
                            unfocusedBorderColor = OutlineColor,
                            focusedTextColor = TextOnSurface,
                            unfocusedTextColor = TextOnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = residentialBaseInput,
                        onValueChange = { residentialBaseInput = it },
                        label = { Text("Residential Base / Home Sector", color = TextOnSurfaceVariant, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGoldPrimary,
                            unfocusedBorderColor = OutlineColor,
                            focusedTextColor = TextOnSurface,
                            unfocusedTextColor = TextOnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = directLineInput,
                        onValueChange = { directLineInput = it },
                        label = { Text("Direct Mobile Line", color = TextOnSurfaceVariant, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGoldPrimary,
                            unfocusedBorderColor = OutlineColor,
                            focusedTextColor = TextOnSurface,
                            unfocusedTextColor = TextOnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emergencyIceInput,
                        onValueChange = { emergencyIceInput = it },
                        label = { Text("Emergency Contact (ICE)", color = TextOnSurfaceVariant, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RadiantGoldPrimary,
                            unfocusedBorderColor = OutlineColor,
                            focusedTextColor = TextOnSurface,
                            unfocusedTextColor = TextOnSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.updateUserProfileVehicleDetails(
                                vehicleModel = vehicleModelInput,
                                vehicleSpec = vehicleSpecInput,
                                residentialBase = residentialBaseInput,
                                directLine = directLineInput,
                                emergencyIce = emergencyIceInput
                            )
                            isEditingVehicle = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RadiantGoldPrimary,
                            contentColor = OnGoldPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Vehicle & Base Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 6. Sign Out Button
        Button(
            onClick = { viewModel.signOut() },
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldSurfaceHigh,
                contentColor = Color(0xFFFF5252)
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Sign Out", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out of RideVision", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
