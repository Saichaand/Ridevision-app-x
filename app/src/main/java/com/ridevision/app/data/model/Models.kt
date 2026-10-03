package com.ridevision.app.data.model

import android.graphics.RectF
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

enum class Severity(val label: String, val levelText: String) {
    MINOR("Minor", "MINOR • LVL 1"),
    MODERATE("Moderate", "MODERATE • LVL 2"),
    SEVERE("Severe", "HIGH RISK • LVL 4")
}

enum class PotholeStatus(val label: String, val filterKey: String) {
    ACTIVE("Under Investigation by City Works", "pending"),
    IN_PROGRESS("Scheduled for Patching", "progress"),
    VERIFIED_FIXED("Resolved & Verified by Riders", "repaired")
}

data class Pothole(
    val id: String = "",
    val userId: String = "",
    val ticketNumber: String = "#RV-88219",
    val lat: Double = 12.9141,
    val lon: Double = 74.8560,
    val city: String = "Mangaluru",
    val address: String = "NH 73 near SJEC Gate, Vamanjoor",
    val laneInfo: String = "Lane 2, Northbound Edge",
    val severity: Severity = Severity.MODERATE,
    val status: PotholeStatus = PotholeStatus.ACTIVE,
    val confirmationCount: Int = 1,
    val fixedConfirmationCount: Int = 0,
    val drawableResId: Int? = null,
    val reportedAt: String = "Today",
    val updatedAtDisplay: String = "Just now",
    val notes: String = "",
    val depthCm: Float = 14.2f,
    val widthCm: Float = 45f,
    val distanceDisplay: String = "0.4 km away",
    val statusNote: String = "Reported to Municipal Works",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "userId" to userId,
            "ticketNumber" to ticketNumber,
            "lat" to lat,
            "lon" to lon,
            "city" to city,
            "address" to address,
            "laneInfo" to laneInfo,
            "severity" to severity.name,
            "status" to status.name,
            "confirmationCount" to confirmationCount,
            "fixedConfirmationCount" to fixedConfirmationCount,
            "notes" to notes,
            "depthCm" to depthCm,
            "widthCm" to widthCm,
            "statusNote" to statusNote,
            "reportedAt" to reportedAt,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (createdAt == null) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }
}

data class Detection(
    val id: String,
    val boxNorm: RectF,
    val confidence: Float,
    val severity: Severity,
    val areaRatio: Float,
    val engine: String = "RideVision Edge-CV"
)

data class DetectionResult(
    val detections: List<Detection>,
    val processingTimeMs: Long,
    val maxConfidence: Float,
    val roadConditionScore: Int,
    val summary: String,
    val estimatedDepthCm: Float = 14.2f
)

data class HazardWarning(
    val potholeId: String,
    val distanceMeters: Int,
    val angularDeviationDeg: Float,
    val severity: Severity,
    val address: String,
    val confirmationCount: Int,
    val message: String,
    val isUrgent: Boolean
)

data class SafeRouteOption(
    val id: String = "",
    val name: String = "",
    val etaMin: Int = 12,
    val distanceKm: Float = 4.2f,
    val potholeCount: Int = 1,
    val conditionIndex: Int = 96,
    val riskLabel: String = "Optimal",
    val description: String = "",
    val isOptimal: Boolean = false,
    val liveMapsIntel: String = ""
)

data class RouteGroundingIntel(
    val routeId: String = "",
    val summary: String = "",
    val roadConditions: List<String> = emptyList(),
    val groundedPlaces: List<String> = emptyList(),
    val lastRefreshed: String = ""
)

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "Rider Sentinel",
    val photoUrl: String = "",
    val riderId: String = "#RV-8824-OK",
    val statusTitle: String = "Active Road Sentinel",
    val safetyTier: String = "Gold Guardian",
    val tierSubtitle: String = "Top Reporter",
    val verifiedCount: Int = 1,
    val precisionScore: String = "99.4%",
    val milesCovered: String = "12.4 km",
    val dateOfBirth: String = "March 14, 1998",
    val age: Int = 26,
    val residentialBase: String = "Kadri Hills, Mangaluru, Karnataka",
    val directLine: String = "+91 98450 12345",
    val vehicleModel: String = "Yamaha MT-07",
    val vehicleSpec: String = "Daily Commuter • Class: Roadster 689cc",
    val emergencyIce: String = "Priya Vance (+91 98450 88219)",
    val earbudAudioPing: Boolean = true,
    val handlebarHapticPulse: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "safetyTier" to safetyTier,
            "verifiedCount" to verifiedCount,
            "milesCovered" to milesCovered,
            "precisionScore" to precisionScore,
            "vehicleModel" to vehicleModel,
            "emergencyIce" to emergencyIce,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (createdAt == null) {
            map["createdAt"] = FieldValue.serverTimestamp()
        }
        return map
    }
}

data class CityConfig(
    val cityName: String,
    val authorityName: String,
    val channelType: String,
    val contactValue: String,
    val instructions: String
)

data class MunicipalDispatch(
    val cityName: String,
    val authorityName: String,
    val channelType: String,
    val contactValue: String,
    val prefilledMessage: String,
    val actionUrl: String,
    val instructions: String
)
