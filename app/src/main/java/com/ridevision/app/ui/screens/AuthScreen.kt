package com.ridevision.app.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ridevision.app.R
import com.ridevision.app.ui.theme.EmeraldBackground
import com.ridevision.app.ui.theme.EmeraldSurface
import com.ridevision.app.ui.theme.EmeraldSurfaceHigh
import com.ridevision.app.ui.theme.EmeraldSurfaceHighest
import com.ridevision.app.ui.theme.EmeraldSurfaceLowest
import com.ridevision.app.ui.theme.MintSecondary
import com.ridevision.app.ui.theme.OnGoldPrimary
import com.ridevision.app.ui.theme.RadiantGoldPrimary
import com.ridevision.app.ui.theme.TextOnSurface
import com.ridevision.app.ui.theme.TextOnSurfaceVariant
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.launch

private const val TAG = "AuthScreen"

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun extractIdToken(credential: Credential): String? {
    if (credential is GoogleIdTokenCredential) {
        return credential.idToken
    }
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        return try {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } catch (e: Exception) {
            Log.e("AuthScreen", "Failed to parse GoogleIdTokenCredential from CustomCredential bundle", e)
            null
        }
    }
    return null
}

@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val credentialManager = remember { CredentialManager.create(context) }
    val auth = remember { FirebaseAuth.getInstance() }

    fun launchGoogleSignIn() {
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            try {
                val serverClientId = context.getString(R.string.default_web_client_id)
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val targetContext = context.findActivity() ?: context
                val result = credentialManager.getCredential(
                    context = targetContext,
                    request = request
                )

                val idToken = extractIdToken(result.credential)
                if (idToken != null) {
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCred)
                        .addOnSuccessListener {
                            isLoading = false
                            onSignInSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Firebase Auth credential exchange failed", e)
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Firebase Sign-in failed."
                        }
                } else {
                    isLoading = false
                    errorMessage = "Unable to process Google ID Token (${result.credential.type})."
                }
            } catch (e: GetCredentialCancellationException) {
                Log.i(TAG, "Google Sign-In canceled by user")
                isLoading = false
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager error: ${e.message}", e)
                isLoading = false
                val rawMsg = e.message.orEmpty()
                errorMessage = when {
                    rawMsg.contains("16") || rawMsg.contains("No matching credentials", ignoreCase = true) || rawMsg.contains("No credentials", ignoreCase = true) -> {
                        "No Google Account found on this device/emulator. Please add or sign in to a Google account in Android Settings → Passwords & Accounts to proceed."
                    }
                    rawMsg.contains("10") || rawMsg.contains("DEVELOPER_ERROR", ignoreCase = true) -> {
                        "OAuth configuration error (code 10). Verify that the package name and debug keystore SHA-1 are registered in Google Cloud / Firebase Console."
                    }
                    rawMsg.contains("7") || rawMsg.contains("NETWORK_ERROR", ignoreCase = true) -> {
                        "Network error connecting to Google servers. Please check your internet connection."
                    }
                    else -> {
                        "Google Sign-in failed: ${e.localizedMessage ?: e.message ?: "Unknown error"}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in exception: ${e.message}", e)
                isLoading = false
                errorMessage = e.localizedMessage ?: e.message ?: "Sign-in failed."
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EmeraldBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Emblem Header
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(RadiantGoldPrimary, MintSecondary)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "RideVision Radar",
                    tint = EmeraldBackground,
                    modifier = Modifier.size(44.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "RideVision",
                    color = TextOnSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cloud-Synchronized Road Sentinel Network",
                    color = RadiantGoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Mission Value Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmeraldSurface)
                    .border(1.dp, RadiantGoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = RadiantGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Cloud Firestore Persistence",
                            color = TextOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time sync of verified road hazards across Mangaluru",
                            color = TextOnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSurfaceHighest)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MintSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Reputation & Sentinel Stats",
                            color = TextOnSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Log civic reports with authenticated identity and live tracking",
                            color = TextOnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Error Display
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3E1414))
                        .padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Google Sign-In Action Button
            Button(
                onClick = { launchGoogleSignIn() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RadiantGoldPrimary,
                    contentColor = OnGoldPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = OnGoldPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Authenticating...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Google Sign In",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Secured with Firebase Auth & Zero-Trust Cloud Firestore",
                color = TextOnSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
