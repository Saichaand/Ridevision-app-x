package com.ridevision.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ridevision.app.R
import com.ridevision.app.data.model.DetectionResult
import com.ridevision.app.data.model.HazardWarning
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.SafeRouteOption
import com.ridevision.app.data.model.Severity
import com.ridevision.app.data.model.UserProfile
import com.ridevision.app.data.repository.PotholeRepository
import com.ridevision.app.data.repository.ReportResult
import com.ridevision.app.data.repository.VoteResult
import com.ridevision.app.domain.detector.RoadHazardDetector
import com.ridevision.app.domain.geo.GeoMatchingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    REPORT("Report"),
    SAFE_ROUTE("Safe Route"),
    HISTORY("History"),
    PROFILE("Profile")
}

enum class TransportMode(val label: String) {
    RIDE("Ride"),
    CAR("Car"),
    FLEET("Fleet")
}

class RideVisionViewModel(
    private val repository: PotholeRepository = PotholeRepository()
) : ViewModel() {

    val potholes: StateFlow<List<Pothole>> = repository.potholes

    // Current Tab
    private val _currentTab = MutableStateFlow(AppTab.REPORT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Location & Vehicle Telemetry (Mangaluru Defaults)
    private val _currentLat = MutableStateFlow(12.9141)
    val currentLat: StateFlow<Double> = _currentLat.asStateFlow()

    private val _currentLon = MutableStateFlow(74.8560)
    val currentLon: StateFlow<Double> = _currentLon.asStateFlow()

    private val _currentStreetAddress = MutableStateFlow("NH 73 near SJEC Gate, Vamanjoor")
    val currentStreetAddress: StateFlow<String> = _currentStreetAddress.asStateFlow()

    private val _currentHeading = MutableStateFlow(42f)
    val currentHeading: StateFlow<Float> = _currentHeading.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(32f)
    val currentSpeedKmh: StateFlow<Float> = _currentSpeedKmh.asStateFlow()

    // Rider Alert Radar Banner State
    private val _isRadarAudioEnabled = MutableStateFlow(true)
    val isRadarAudioEnabled: StateFlow<Boolean> = _isRadarAudioEnabled.asStateFlow()

    private val _activeWarning = MutableStateFlow<HazardWarning?>(
        HazardWarning(
            potholeId = "pothole-88219",
            distanceMeters = 300,
            angularDeviationDeg = 4.2f,
            severity = Severity.SEVERE,
            address = "NH 73 near SJEC Gate (14cm Crater ahead)",
            confirmationCount = 24,
            message = "Warning you of road hazards 300m ahead",
            isUrgent = true
        )
    )
    val activeWarning: StateFlow<HazardWarning?> = _activeWarning.asStateFlow()

    // Vision / Viewfinder State
    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()

    private val _detectionResult = MutableStateFlow<DetectionResult?>(
        DetectionResult(
            detections = emptyList(),
            processingTimeMs = 18,
            maxConfidence = 0.984f,
            roadConditionScore = 96,
            summary = "98.4% Confidence • 14.2 cm Depth",
            estimatedDepthCm = 14.2f
        )
    )
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()

    private val _flashTrigger = MutableSharedFlow<Unit>()
    val flashTrigger: SharedFlow<Unit> = _flashTrigger.asSharedFlow()

    private val _submissionConfirmed = MutableStateFlow(false)
    val submissionConfirmed: StateFlow<Boolean> = _submissionConfirmed.asStateFlow()

    // Safe Route Navigation State
    val safeRouteOptions = listOf(
        SafeRouteOption(
            id = "route-1",
            name = "Kankanady & Kadri Bypass",
            etaMin = 12,
            distanceKm = 4.2f,
            potholeCount = 1,
            conditionIndex = 96,
            riskLabel = "Optimal",
            description = "Only 1 shallow crack • 4.2 km",
            isOptimal = true
        ),
        SafeRouteOption(
            id = "route-2",
            name = "Hampankatta - Bejai Corridor",
            etaMin = 14,
            distanceKm = 3.9f,
            potholeCount = 8,
            conditionIndex = 74,
            riskLabel = "Moderate",
            description = "8 detected craters • 3.9 km",
            isOptimal = false
        ),
        SafeRouteOption(
            id = "route-3",
            name = "NH 73 Vamanjoor Direct",
            etaMin = 10,
            distanceKm = 3.9f,
            potholeCount = 15,
            conditionIndex = 42,
            riskLabel = "Severe Risk",
            description = "15 Severe potholes • High impact",
            isOptimal = false
        )
    )

    private val _selectedRouteId = MutableStateFlow("route-1")
    val selectedRouteId: StateFlow<String> = _selectedRouteId.asStateFlow()

    private val _transportMode = MutableStateFlow(TransportMode.RIDE)
    val transportMode: StateFlow<TransportMode> = _transportMode.asStateFlow()

    private val _avoidHoles = MutableStateFlow(true)
    val avoidHoles: StateFlow<Boolean> = _avoidHoles.asStateFlow()

    private val _routeOrigin = MutableStateFlow("NH 73 near SJEC Gate, Vamanjoor")
    val routeOrigin: StateFlow<String> = _routeOrigin.asStateFlow()

    private val _routeDestination = MutableStateFlow("Kankanady Circle, Mangaluru")
    val routeDestination: StateFlow<String> = _routeDestination.asStateFlow()

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    // History & Registry Search / Filter State
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilterTab = MutableStateFlow("all") // "all", "pending", "progress", "repaired"
    val historyFilterTab: StateFlow<String> = _historyFilterTab.asStateFlow()

    // Profile State
    private val _isLoginView = MutableStateFlow(false)
    val isLoginView: StateFlow<Boolean> = _isLoginView.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Feedback Toast / Event Message
    private val _userFeedback = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateLocation(lat: Double, lon: Double, heading: Float, speed: Float) {
        _currentLat.value = lat
        _currentLon.value = lon
        _currentHeading.value = heading
        _currentSpeedKmh.value = speed
    }

    fun toggleRadarAudio() {
        _isRadarAudioEnabled.value = !_isRadarAudioEnabled.value
    }

    // Photo / Optical Capture
    fun loadInitialSample(context: Context) {
        if (_currentBitmap.value == null) {
            val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.sample_severe_pothole)
            _currentBitmap.value = bmp
            runDetection(bmp)
        }
    }

    fun setCustomBitmap(bitmap: Bitmap) {
        _currentBitmap.value = bitmap
        triggerFlash()
        runDetection(bitmap)
    }

    fun triggerFlash() {
        viewModelScope.launch {
            _flashTrigger.emit(Unit)
        }
    }

    fun runDetection(bitmap: Bitmap) {
        viewModelScope.launch {
            val result = RoadHazardDetector.analyzeBitmap(bitmap, false)
            _detectionResult.value = result
        }
    }

    fun detectGps(context: Context) {
        viewModelScope.launch {
            val client = LocationServices.getFusedLocationProviderClient(context)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            _currentLat.value = location.latitude
                            _currentLon.value = location.longitude
                            val city = GeoMatchingService.detectCity(location.latitude, location.longitude)
                            _currentStreetAddress.value = "Road in $city (${"%.4f".format(location.latitude)}, ${"%.4f".format(location.longitude)})"
                            viewModelScope.launch {
                                _userFeedback.emit("GPS Calibrated: $city")
                            }
                        }
                    }
            } else {
                _currentLat.value = 12.9141
                _currentLon.value = 74.8560
                _currentStreetAddress.value = "NH 73 near SJEC Gate, Vamanjoor"
                _userFeedback.emit("GPS Calibrated: Mangaluru (SJEC Vamanjoor)")
            }
        }
    }

    fun submitComplaint(notes: String = "") {
        viewModelScope.launch {
            val res = repository.submitReport(
                lat = _currentLat.value,
                lon = _currentLon.value,
                address = _currentStreetAddress.value,
                severity = Severity.SEVERE,
                notes = notes.ifBlank { "Deep road fissure logged via RideVision optical HUD scanner." }
            )
            _submissionConfirmed.value = true
            _userFeedback.emit("Report submitted and published.")
        }
    }

    fun dismissSubmissionPill() {
        _submissionConfirmed.value = false
    }

    // Safe Route Controls
    fun setTransportMode(mode: TransportMode) {
        _transportMode.value = mode
    }

    fun toggleAvoidHoles() {
        _avoidHoles.value = !_avoidHoles.value
    }

    fun selectRoute(routeId: String) {
        _selectedRouteId.value = routeId
    }

    fun swapOriginDestination() {
        val orig = _routeOrigin.value
        _routeOrigin.value = _routeDestination.value
        _routeDestination.value = orig
    }

    fun toggleNavigation() {
        val next = !_isNavigating.value
        _isNavigating.value = next
        viewModelScope.launch {
            _userFeedback.emit(if (next) "Cockpit HUD Live • Safe Navigation Started" else "Navigation Paused")
        }
    }

    // History Controls
    fun setHistoryFilter(filter: String) {
        _historyFilterTab.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun upvoteHazard(potholeId: String) {
        viewModelScope.launch {
            when (val res = repository.confirmStillThere(potholeId)) {
                is VoteResult.Success -> {
                    _userFeedback.emit(res.message)
                }
                is VoteResult.CooldownActive -> {
                    _userFeedback.emit(res.message)
                }
            }
        }
    }

    // Profile Controls
    fun setLoginView(isLogin: Boolean) {
        _isLoginView.value = isLogin
    }

    fun toggleEarbudAudio(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(earbudAudioPing = enabled)
    }

    fun toggleHandlebarHaptics(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(handlebarHapticPulse = enabled)
    }

    fun performLogin(username: String) {
        _isLoginView.value = false
        viewModelScope.launch {
            _userFeedback.emit("Authenticated as $username • Welcome back, Alex!")
        }
    }
}
