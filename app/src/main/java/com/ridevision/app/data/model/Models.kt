package com.ridevision.app.data.model

import android.graphics.RectF

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
    val id: String,
    val ticketNumber: String = "#RV-88219",
    val lat: Double,
    val lon: Double,
    val city: String,
    val address: String,
    val laneInfo: String = "Lane 2, Northbound Edge",
    val severity: Severity,
    val status: PotholeStatus = PotholeStatus.ACTIVE,
    val confirmationCount: Int = 24,
    val fixedConfirmationCount: Int = 0,
    val drawableResId: Int? = null,
    val reportedAt: String,
    val updatedAt: String,
    val notes: String = "",
    val depthCm: Float = 14.2f,
    val widthCm: Float = 45f,
    val distanceDisplay: String = "0.4 mi away",
    val statusNote: String = "Under Investigation by City Works"
)

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
    val id: String,
    val name: String,
    val etaMin: Int,
    val distanceKm: Float,
    val potholeCount: Int,
    val conditionIndex: Int,
    val riskLabel: String,
    val description: String,
    val isOptimal: Boolean
)

data class UserProfile(
    val riderId: String = "#RV-8824-OK",
    val fullName: String = "Alex M. Vance",
    val statusTitle: String = "Active Road Sentinel",
    val safetyTier: String = "Gold Guardian",
    val tierSubtitle: String = "Top 5% Reporter",
    val verifiedCount: Int = 142,
    val precisionScore: String = "99.4%",
    val milesCovered: String = "3.8k",
    val dateOfBirth: String = "March 14, 1998",
    val age: Int = 26,
    val residentialBase: String = "742 Evergreen Terrace, Sector 4, Metro City",
    val directLine: String = "+1 (555) 234-8901",
    val vehicleModel: String = "Yamaha MT-07",
    val vehicleSpec: String = "Daily Commuter • Class: Roadster 689cc",
    val emergencyIce: String = "Sarah Vance (+1 555-884-2190)",
    val earbudAudioPing: Boolean = true,
    val handlebarHapticPulse: Boolean = true
)

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
