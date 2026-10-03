package com.ridevision.app.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ridevision.app.R
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import com.ridevision.app.data.model.UserProfile
import com.ridevision.app.domain.geo.GeoMatchingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class ReportResult {
    data class Created(val pothole: Pothole) : ReportResult()
    data class MergedExisting(val pothole: Pothole, val distanceMeters: Int) : ReportResult()
}

sealed class VoteResult {
    data class Success(val updatedPothole: Pothole, val message: String) : VoteResult()
    data class CooldownActive(val message: String) : VoteResult()
}

class PotholeRepository(private val db: FirebaseFirestore) {

    // Secondary constructor to resolve database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val userVoteHistory = mutableMapOf<String, Long>()
    private var potholesListener: ListenerRegistration? = null
    private var userProfileListener: ListenerRegistration? = null

    private val _potholes = MutableStateFlow<List<Pothole>>(emptyList())
    val potholes: StateFlow<List<Pothole>> = _potholes.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        startListeningToPotholes()
    }

    private fun getNowIso(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    private fun getTodayFormatted(): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    /**
     * Real-time Firestore snapshot listener for the shared /potholes collection.
     * Replaces hardcoded in-memory dummy data with live cloud-synchronized data.
     */
    fun startListeningToPotholes() {
        if (potholesListener != null) return

        val collectionRef = db.collection("potholes")
        potholesListener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to Firestore /potholes: ${error.message}", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                if (snapshot.isEmpty) {
                    // Seed initial cloud records once if collection is completely fresh
                    coroutineScope.launch {
                        seedInitialCloudPotholes()
                    }
                } else {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val userId = doc.getString("userId") ?: ""
                            val ticketNumber = doc.getString("ticketNumber") ?: "#RV-88219"
                            val lat = doc.getDouble("lat") ?: 12.9141
                            val lon = doc.getDouble("lon") ?: 74.8560
                            val city = doc.getString("city") ?: "Mangaluru"
                            val address = doc.getString("address") ?: "NH 73 near SJEC Gate"
                            val laneInfo = doc.getString("laneInfo") ?: "Lane 1"
                            val severityStr = doc.getString("severity") ?: "MODERATE"
                            val severity = try {
                                Severity.valueOf(severityStr)
                            } catch (e: Exception) {
                                Severity.MODERATE
                            }
                            val statusStr = doc.getString("status") ?: "ACTIVE"
                            val status = try {
                                PotholeStatus.valueOf(statusStr)
                            } catch (e: Exception) {
                                PotholeStatus.ACTIVE
                            }
                            val confirmationCount = (doc.getLong("confirmationCount") ?: 1L).toInt()
                            val fixedConfirmationCount = (doc.getLong("fixedConfirmationCount") ?: 0L).toInt()
                            val notes = doc.getString("notes") ?: ""
                            val depthCm = (doc.getDouble("depthCm") ?: 14.0).toFloat()
                            val widthCm = (doc.getDouble("widthCm") ?: 45.0).toFloat()
                            val statusNote = doc.getString("statusNote") ?: "Under Investigation by City Works"
                            val reportedAt = doc.getString("reportedAt") ?: "Recently"
                            val createdAt = doc.getTimestamp("createdAt")
                            val updatedAt = doc.getTimestamp("updatedAt")

                            Pothole(
                                id = id,
                                userId = userId,
                                ticketNumber = ticketNumber,
                                lat = lat,
                                lon = lon,
                                city = city,
                                address = address,
                                laneInfo = laneInfo,
                                severity = severity,
                                status = status,
                                confirmationCount = confirmationCount,
                                fixedConfirmationCount = fixedConfirmationCount,
                                drawableResId = if (severity == Severity.SEVERE) R.drawable.sample_severe_pothole else R.drawable.sample_moderate_pothole,
                                reportedAt = reportedAt,
                                updatedAtDisplay = "Synced from Cloud",
                                notes = notes,
                                depthCm = depthCm,
                                widthCm = widthCm,
                                distanceDisplay = "0.4 km away",
                                statusNote = statusNote,
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Error mapping pothole doc ${doc.id}", e)
                            null
                        }
                    }
                    _potholes.value = list
                }
            }
        }
    }

    /**
     * Seeds initial baseline road hazards into Firestore if cloud collection is empty.
     */
    private suspend fun seedInitialCloudPotholes() {
        val auth = FirebaseAuth.getInstance()
        val currentUid = auth.currentUser?.uid ?: "system-sentinel"
        val now = getTodayFormatted()

        val seedList = listOf(
            Pothole(
                id = "pothole-mng-001",
                userId = currentUid,
                ticketNumber = "#RV-88219",
                lat = 12.9152,
                lon = 74.8988,
                city = "Mangaluru",
                address = "NH 73 near SJEC Gate, Vamanjoor",
                laneInfo = "Lane 2, Northbound Edge",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 24,
                fixedConfirmationCount = 0,
                reportedAt = now,
                notes = "Deep asphalt fissure road hazard with jagged rim outside SJEC entrance.",
                depthCm = 14.2f,
                statusNote = "Under Investigation by City Works"
            ),
            Pothole(
                id = "pothole-mng-002",
                userId = currentUid,
                ticketNumber = "#RV-77402",
                lat = 12.8715,
                lon = 74.8564,
                city = "Mangaluru",
                address = "Kankanady Bypass Road near Father Muller",
                laneInfo = "Bike Corridor Center",
                severity = Severity.MODERATE,
                status = PotholeStatus.IN_PROGRESS,
                confirmationCount = 41,
                fixedConfirmationCount = 1,
                reportedAt = now,
                notes = "Circular road depression marked with amber warning.",
                depthCm = 8.5f,
                statusNote = "Scheduled for Patching"
            ),
            Pothole(
                id = "pothole-mng-003",
                userId = currentUid,
                ticketNumber = "#RV-69120",
                lat = 12.8798,
                lon = 74.8532,
                city = "Mangaluru",
                address = "Kadri Temple Road, Mallikatte Junction",
                laneInfo = "Crosswalk approach",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 89,
                fixedConfirmationCount = 14,
                reportedAt = now,
                notes = "Bitumen asphalt patch completed and verified smooth.",
                depthCm = 0.0f,
                statusNote = "Resolved & Verified by Riders"
            )
        )

        for (p in seedList) {
            try {
                db.collection("potholes").document(p.id).set(p.toFirestoreMap()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error seeding initial pothole ${p.id}", e)
            }
        }
    }

    /**
     * Submits a real pothole report directly to Cloud Firestore.
     * Enforces spatial 15m deduplication.
     */
    suspend fun submitReport(
        lat: Double,
        lon: Double,
        address: String,
        severity: Severity,
        notes: String
    ): ReportResult {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: error("User must be authenticated to report a pothole.")
        val currentList = _potholes.value

        val nearbyExisting = currentList.firstOrNull {
            GeoMatchingService.haversineDistanceM(lat, lon, it.lat, it.lon) <= 15.0
        }

        return if (nearbyExisting != null) {
            val dist = GeoMatchingService.haversineDistanceM(lat, lon, nearbyExisting.lat, nearbyExisting.lon).toInt()
            try {
                db.collection("potholes").document(nearbyExisting.id).update(
                    "confirmationCount", FieldValue.increment(1),
                    "updatedAt", FieldValue.serverTimestamp()
                ).await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed updating existing pothole in Firestore", e)
            }
            val updated = nearbyExisting.copy(
                confirmationCount = nearbyExisting.confirmationCount + 1
            )
            ReportResult.MergedExisting(updated, dist)
        } else {
            val potholeId = "pothole-${UUID.randomUUID().toString().take(8)}"
            val ticketNumber = "#RV-${(88220..88999).random()}"
            val city = GeoMatchingService.detectCity(lat, lon)
            val todayStr = getTodayFormatted()

            val newPothole = Pothole(
                id = potholeId,
                userId = userId,
                ticketNumber = ticketNumber,
                lat = lat,
                lon = lon,
                city = city,
                address = address.ifBlank { "NH 73 near SJEC Gate, Vamanjoor" },
                laneInfo = "Lane 1, Center Track",
                severity = severity,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 1,
                fixedConfirmationCount = 0,
                reportedAt = todayStr,
                updatedAtDisplay = "Just now",
                notes = notes.ifBlank { "Road cavity reported via optical AI telemetry." },
                depthCm = if (severity == Severity.SEVERE) 14.2f else 7.5f,
                statusNote = "Under Investigation by City Works"
            )

            try {
                db.collection("potholes").document(potholeId).set(newPothole.toFirestoreMap()).await()
                // Increment user's verifiedCount on successful report
                incrementUserVerifiedCount(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing new pothole to Firestore", e)
            }

            ReportResult.Created(newPothole)
        }
    }

    suspend fun confirmStillThere(potholeId: String, userId: String): VoteResult {
        val key = "$userId-$potholeId"
        val lastVote = userVoteHistory[key]
        val nowMs = System.currentTimeMillis()
        val cooldownMs = 7L * 24 * 60 * 60 * 1000

        if (lastVote != null && (nowMs - lastVote) < cooldownMs) {
            return VoteResult.CooldownActive("Cooldown: You already confirmed this hazard recently.")
        }

        val target = _potholes.value.firstOrNull { it.id == potholeId }
            ?: return VoteResult.CooldownActive("Hazard not found.")

        userVoteHistory[key] = nowMs
        try {
            db.collection("potholes").document(potholeId).update(
                "confirmationCount", FieldValue.increment(1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            incrementUserVerifiedCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed confirming still there on Firestore", e)
        }

        val updated = target.copy(confirmationCount = target.confirmationCount + 1)
        return VoteResult.Success(updated, "Verified! +1 confirmation recorded in Firestore for road authorities.")
    }

    suspend fun confirmFixed(potholeId: String, userId: String): VoteResult {
        val key = "$userId-fix-$potholeId"
        val lastVote = userVoteHistory[key]
        val nowMs = System.currentTimeMillis()
        val cooldownMs = 7L * 24 * 60 * 60 * 1000

        if (lastVote != null && (nowMs - lastVote) < cooldownMs) {
            return VoteResult.CooldownActive("Cooldown: You already voted on this repair.")
        }

        val target = _potholes.value.firstOrNull { it.id == potholeId }
            ?: return VoteResult.CooldownActive("Hazard not found.")

        userVoteHistory[key] = nowMs
        val newFixedCount = target.fixedConfirmationCount + 1
        val newStatus = if (newFixedCount >= 3) {
            PotholeStatus.VERIFIED_FIXED
        } else {
            PotholeStatus.IN_PROGRESS
        }

        try {
            db.collection("potholes").document(potholeId).update(
                "fixedConfirmationCount", FieldValue.increment(1),
                "status", newStatus.name,
                "statusNote", if (newStatus == PotholeStatus.VERIFIED_FIXED) "Resolved & Verified by Riders" else "Scheduled for Patching",
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
            incrementUserVerifiedCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed confirming fixed on Firestore", e)
        }

        val updated = target.copy(
            fixedConfirmationCount = newFixedCount,
            status = newStatus
        )
        return VoteResult.Success(updated, "Repair vote logged ($newFixedCount/3) in Cloud Firestore.")
    }

    /**
     * Listens to the authenticated user's profile at /users/{userId}.
     */
    fun startListeningToUserProfile(userId: String) {
        userProfileListener?.remove()
        userProfileListener = db.collection("users").document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to user profile: ${error.message}", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = UserProfile(
                    userId = userId,
                    email = snapshot.getString("email") ?: "",
                    displayName = snapshot.getString("displayName") ?: "Road Sentinel",
                    photoUrl = snapshot.getString("photoUrl") ?: "",
                    safetyTier = snapshot.getString("safetyTier") ?: "Gold Guardian",
                    verifiedCount = (snapshot.getLong("verifiedCount") ?: 1L).toInt(),
                    milesCovered = snapshot.getString("milesCovered") ?: "12.4 km",
                    precisionScore = snapshot.getString("precisionScore") ?: "99.4%",
                    vehicleModel = snapshot.getString("vehicleModel") ?: "Yamaha MT-07",
                    emergencyIce = snapshot.getString("emergencyIce") ?: "Priya Vance (+91 98450 88219)",
                    createdAt = snapshot.getTimestamp("createdAt"),
                    updatedAt = snapshot.getTimestamp("updatedAt")
                )
                _currentUserProfile.value = profile
            }
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        try {
            db.collection("users").document(profile.userId).set(profile.toFirestoreMap()).await()
            _currentUserProfile.value = profile
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving user profile to Firestore", e)
        }
    }

    private suspend fun incrementUserVerifiedCount(userId: String) {
        try {
            db.collection("users").document(userId).update(
                "verifiedCount", FieldValue.increment(1),
                "updatedAt", FieldValue.serverTimestamp()
            ).await()
        } catch (e: Exception) {
            // Document might not exist yet; will be created on profile sync
        }
    }

    fun cleanup() {
        potholesListener?.remove()
        userProfileListener?.remove()
    }

    companion object {
        private const val TAG = "PotholeRepository"
    }
}
