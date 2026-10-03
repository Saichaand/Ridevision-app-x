package com.ridevision.app.domain.geo

import com.ridevision.app.data.model.CityConfig
import com.ridevision.app.data.model.HazardWarning
import com.ridevision.app.data.model.MunicipalDispatch
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoMatchingService {

    private const val EARTH_RADIUS_M = 6371000.0

    val CITY_CONFIGS = mapOf(
        "Mangaluru" to CityConfig(
            cityName = "Mangaluru",
            authorityName = "Mangaluru City Corporation (MCC)",
            channelType = "whatsapp",
            contactValue = "919449007722",
            instructions = "Direct dispatch to MCC Official Grievance WhatsApp (+91 9449007722)."
        ),
        "Bengaluru" to CityConfig(
            cityName = "Bengaluru",
            authorityName = "Bruhat Bengaluru Mahanagara Palike (BBMP)",
            channelType = "helpline",
            contactValue = "080-22660000",
            instructions = "BBMP 24x7 Sahaaya Pothole Control Room (080-22660000)."
        ),
        "Udupi" to CityConfig(
            cityName = "Udupi",
            authorityName = "Udupi City Municipal Council (CMC)",
            channelType = "helpline",
            contactValue = "0820-2520306",
            instructions = "Udupi CMC Grievance Cell (0820-2520306)."
        ),
        "Mysuru" to CityConfig(
            cityName = "Mysuru",
            authorityName = "Mysuru City Corporation (MCC)",
            channelType = "helpline",
            contactValue = "0821-2440890",
            instructions = "Mysuru City Corporation Grievance Desk (0821-2440890)."
        )
    )

    fun haversineDistanceM(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dPhi = Math.toRadians(lat2 - lat1)
        val dLambda = Math.toRadians(lon2 - lon1)

        val a = sin(dPhi / 2) * sin(dPhi / 2) +
                cos(phi1) * cos(phi2) * sin(dLambda / 2) * sin(dLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    fun bearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val dLambda = Math.toRadians(lon2 - lon1)

        val x = sin(dLambda) * cos(phi2)
        val y = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLambda)
        val initialBearing = Math.toDegrees(atan2(x, y))
        return ((initialBearing + 360) % 360).toFloat()
    }

    fun angleDiffDeg(a: Float, b: Float): Float {
        val diff = Math.abs(a - b) % 360f
        return if (diff <= 180f) diff else 360f - diff
    }

    fun detectCity(lat: Double, lon: Double): String {
        return when {
            lat in 12.80..13.05 && lon in 74.80..74.98 -> "Mangaluru"
            lat in 12.80..13.20 && lon in 77.40..77.85 -> "Bengaluru"
            lat in 13.20..13.50 && lon in 74.65..74.90 -> "Udupi"
            lat in 12.20..12.45 && lon in 76.50..76.80 -> "Mysuru"
            else -> "Mangaluru"
        }
    }

    /**
     * Checks if there is an active hazard within the alert cone ahead (default 250m, +/- 45 deg).
     */
    fun checkWarningAhead(
        currentLat: Double,
        currentLon: Double,
        headingDeg: Float,
        potholes: List<Pothole>,
        alertRadiusM: Double = 250.0,
        headingToleranceDeg: Float = 45.0f
    ): HazardWarning? {
        val qualifying = potholes.filter { it.status == PotholeStatus.ACTIVE }
            .mapNotNull { pothole ->
                val distance = haversineDistanceM(currentLat, currentLon, pothole.lat, pothole.lon)
                if (distance <= alertRadiusM) {
                    val potholeBearing = bearingDeg(currentLat, currentLon, pothole.lat, pothole.lon)
                    val deviation = angleDiffDeg(headingDeg, potholeBearing)
                    if (deviation <= headingToleranceDeg) {
                        Triple(distance, deviation, pothole)
                    } else null
                } else null
            }

        if (qualifying.isEmpty()) return null

        val nearest = qualifying.minByOrNull { it.first } ?: return null
        val distance = nearest.first.toInt()
        val deviation = nearest.second
        val pothole = nearest.third

        val isUrgent = pothole.severity == Severity.SEVERE || distance < 90

        return HazardWarning(
            potholeId = pothole.id,
            distanceMeters = distance,
            angularDeviationDeg = deviation,
            severity = pothole.severity,
            address = pothole.address,
            confirmationCount = pothole.confirmationCount,
            message = "⚠️ ${pothole.severity.label.uppercase()} POTHOLE ${distance}m AHEAD",
            isUrgent = isUrgent
        )
    }

    fun buildComplaintText(
        address: String,
        lat: Double,
        lon: Double,
        severity: Severity,
        notes: String = ""
    ): String {
        val timeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val mapsLink = "https://maps.google.com/?q=$lat,$lon"
        return """
            🚨 *ROAD POTHOLE HAZARD REPORT — RideVision*
            
            📍 *Location:* $address
            🌐 *Google Maps:* $mapsLink
            ⚠️ *Severity Assessment:* ${severity.label.uppercase()}
            🕒 *Reported At:* $timeStr
            📝 *Commuter Note:* ${notes.ifBlank { "Detected and verified via RideVision AI Road Safety System." }}
            
            _Auto-formatted via RideVision CV Civic Dispatch._
        """.trimIndent()
    }

    fun routeComplaint(
        city: String,
        address: String,
        lat: Double,
        lon: Double,
        severity: Severity,
        notes: String = ""
    ): MunicipalDispatch {
        val config = CITY_CONFIGS[city] ?: CITY_CONFIGS["Mangaluru"]!!
        val message = buildComplaintText(address, lat, lon, severity, notes)
        val encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())

        val actionUrl = when (config.channelType) {
            "whatsapp" -> {
                val cleanNumber = config.contactValue.replace("+", "").replace("-", "").replace(" ", "")
                "https://wa.me/$cleanNumber?text=$encodedMsg"
            }
            "helpline" -> "tel:${config.contactValue}"
            else -> "mailto:${config.contactValue}?subject=${URLEncoder.encode("Road Pothole Hazard Report - $city", StandardCharsets.UTF_8.toString())}&body=$encodedMsg"
        }

        return MunicipalDispatch(
            cityName = config.cityName,
            authorityName = config.authorityName,
            channelType = config.channelType,
            contactValue = config.contactValue,
            prefilledMessage = message,
            actionUrl = actionUrl,
            instructions = config.instructions
        )
    }
}
