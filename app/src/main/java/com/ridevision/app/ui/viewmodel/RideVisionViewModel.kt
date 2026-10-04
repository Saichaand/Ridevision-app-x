package com.ridevision.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ridevision.app.R
import com.ridevision.app.data.model.DetectionResult
import com.ridevision.app.data.model.HazardWarning
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.RouteGroundingIntel
import com.ridevision.app.data.model.SafeRouteOption
import com.ridevision.app.data.model.Severity
import com.ridevision.app.data.model.UserProfile
import com.ridevision.app.data.remote.GeminiMapsGroundingService
import com.ridevision.app.data.repository.PotholeRepository
import com.ridevision.app.data.repository.ReportResult
import com.ridevision.app.data.repository.VoteResult
import com.ridevision.app.domain.detector.RoadHazardDetector
import com.ridevision.app.domain.geo.GeoMatchingService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "RideVisionViewModel"

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
    private val repository: PotholeRepository
) : ViewModel() {

    // Auth State (Single source of truth)
    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            repository.startListeningToUserProfile(user.uid)
            syncFirebaseUserToProfile(user)
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        auth.currentUser?.let { user ->
            repository.startListeningToUserProfile(user.uid)
            syncFirebaseUserToProfile(user)
        }
    }

    // Live Cloud Firestore Potholes Stream
    val potholes: StateFlow<List<Pothole>> = repository.potholes

    // Filtered stream containing ONLY potholes reported by the current logged-in user
    val myReportedPotholes: StateFlow<List<Pothole>> = combine(
        potholes,
        currentUser
    ) { list, user ->
        val uid = user?.uid ?: ""
        if (uid.isBlank()) emptyList() else list.filter { it.userId == uid }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            potholeId = "pothole-mng-001",
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

    // Google Maps Grounding via Gemini 3.5 Flash
    private val _routeMapsIntel = MutableStateFlow<Map<String, RouteGroundingIntel>>(emptyMap())
    val routeMapsIntel: StateFlow<Map<String, RouteGroundingIntel>> = _routeMapsIntel.asStateFlow()

    private val _isAnalyzingMaps = MutableStateFlow(false)
    val isAnalyzingMaps: StateFlow<Boolean> = _isAnalyzingMaps.asStateFlow()

    // History & Registry Search / Filter State
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _historyFilterTab = MutableStateFlow("all") // "all", "pending", "progress", "repaired"
    val historyFilterTab: StateFlow<String> = _historyFilterTab.asStateFlow()

    // User Profile synced from Firestore
    private val defaultProfile = UserProfile(
        displayName = "Road Sentinel",
        safetyTier = "Gold Guardian",
        verifiedCount = 1
    )

    val userProfile: StateFlow<UserProfile> = combine(
        _currentUser,
        repository.currentUserProfile
    ) { user, firestoreProfile ->
        if (firestoreProfile != null) {
            firestoreProfile
        } else if (user != null) {
            UserProfile(
                userId = user.uid,
                email = user.email.orEmpty(),
                displayName = user.displayName ?: "Road Sentinel",
                photoUrl = user.photoUrl?.toString().orEmpty(),
                riderId = "#RV-${user.uid.take(6).uppercase()}",
                safetyTier = "Gold Guardian",
                verifiedCount = 1
            )
        } else {
            defaultProfile
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultProfile)

    // Feedback Toast / Event Message
    private val _userFeedback = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

    private fun syncFirebaseUserToProfile(user: FirebaseUser) {
        viewModelScope.launch {
            val existing = repository.currentUserProfile.value
            if (existing == null) {
                val newProfile = UserProfile(
                    userId = user.uid,
                    email = user.email.orEmpty(),
                    displayName = user.displayName ?: "Road Sentinel",
                    photoUrl = user.photoUrl?.toString().orEmpty(),
                    riderId = "#RV-${user.uid.take(6).uppercase()}",
                    safetyTier = "Gold Guardian",
                    verifiedCount = 1
                )
                repository.saveUserProfile(newProfile)
            }
        }
    }

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

    /**
     * Real Firestore Pothole Submission.
     */
    fun submitComplaint(notes: String = "") {
        viewModelScope.launch {
            val res = repository.submitReport(
                lat = _currentLat.value,
                lon = _currentLon.value,
                address = _currentStreetAddress.value,
                severity = Severity.SEVERE,
                notes = notes.ifBlank { "Road cavity detected via optical AI telemetry." }
            )
            _submissionConfirmed.value = true
            when (res) {
                is ReportResult.Created -> _userFeedback.emit("Report logged to Cloud Firestore.")
                is ReportResult.MergedExisting -> _userFeedback.emit("Merged with existing hazard (${res.distanceMeters}m away).")
            }
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
        refreshRouteMapsIntel(routeId)
    }

    fun setRouteOrigin(origin: String) {
        _routeOrigin.value = origin
    }

    fun setRouteDestination(dest: String) {
        _routeDestination.value = dest
    }

    fun swapOriginDestination() {
        val orig = _routeOrigin.value
        _routeOrigin.value = _routeDestination.value
        _routeDestination.value = orig
        refreshRouteMapsIntel(_selectedRouteId.value)
    }

    fun updateUserProfileVehicleDetails(
        vehicleModel: String,
        vehicleSpec: String,
        residentialBase: String,
        directLine: String,
        emergencyIce: String
    ) {
        val user = auth.currentUser ?: return
        val current = userProfile.value
        val updated = current.copy(
            userId = user.uid,
            vehicleModel = vehicleModel.ifBlank { "Yamaha MT-07" },
            vehicleSpec = vehicleSpec.ifBlank { "Commuter Bike" },
            residentialBase = residentialBase.ifBlank { "Mangaluru Base" },
            directLine = directLine.ifBlank { "+91 98450 12345" },
            emergencyIce = emergencyIce.ifBlank { "Emergency Contact" }
        )
        viewModelScope.launch {
            repository.saveUserProfile(updated)
            _userFeedback.emit("Commuter Vehicle & Base details saved to Cloud Firestore.")
        }
    }

    fun toggleNavigation() {
        val next = !_isNavigating.value
        _isNavigating.value = next
        viewModelScope.launch {
            _userFeedback.emit(if (next) "Cockpit HUD Live • Safe Navigation Started" else "Navigation Paused")
        }
    }

    /**
     * Executes Google Maps Grounding via Gemini 3.5 Flash for live route analysis.
     */
    fun refreshRouteMapsIntel(routeId: String) {
        val selected = safeRouteOptions.firstOrNull { it.id == routeId } ?: safeRouteOptions.first()
        _isAnalyzingMaps.value = true
        viewModelScope.launch {
            val intel = GeminiMapsGroundingService.analyzeRouteWithMaps(
                routeId = selected.id,
                routeName = selected.name,
                origin = _routeOrigin.value,
                destination = _routeDestination.value,
                city = "Mangaluru"
            )
            val currentMap = _routeMapsIntel.value.toMutableMap()
            currentMap[routeId] = intel
            _routeMapsIntel.value = currentMap
            _isAnalyzingMaps.value = false
            _userFeedback.emit("Google Maps Intel: ${selected.name} refreshed")
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
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            when (val res = repository.confirmStillThere(potholeId, uid)) {
                is VoteResult.Success -> _userFeedback.emit(res.message)
                is VoteResult.CooldownActive -> _userFeedback.emit(res.message)
            }
        }
    }

    fun confirmHazardFixed(potholeId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            when (val res = repository.confirmFixed(potholeId, uid)) {
                is VoteResult.Success -> _userFeedback.emit(res.message)
                is VoteResult.CooldownActive -> _userFeedback.emit(res.message)
            }
        }
    }

    // Profile & Auth Controls
    fun toggleEarbudAudio(enabled: Boolean) {
        val current = repository.currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.saveUserProfile(current.copy(earbudAudioPing = enabled))
            _userFeedback.emit(if (enabled) "Earbud Chime: Enabled" else "Earbud Chime: Muted")
        }
    }

    fun toggleHandlebarHaptics(enabled: Boolean) {
        val current = repository.currentUserProfile.value ?: return
        viewModelScope.launch {
            repository.saveUserProfile(current.copy(handlebarHapticPulse = enabled))
            _userFeedback.emit(if (enabled) "Handlebar Haptics: Enabled" else "Handlebar Haptics: Disabled")
        }
    }

    fun signOut() {
        auth.signOut()
        viewModelScope.launch {
            _userFeedback.emit("Signed out of RideVision account.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        repository.cleanup()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val dbId = context.applicationContext.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                RideVisionViewModel(PotholeRepository(db))
            }
        }
    }
}
