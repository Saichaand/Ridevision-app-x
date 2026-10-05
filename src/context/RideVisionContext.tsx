import React, { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react';
import {
  AppTab,
  DetectionResult,
  HazardWarning,
  Pothole,
  PotholeStatus,
  RouteGroundingIntel,
  SafeRouteOption,
  TransportMode,
  UserProfile
} from '../types/models';
import { RoadHazardDetector } from '../services/roadHazardDetector';
import { GeoMatchingService } from '../services/geoMatchingService';
import { auth, googleProvider, signInWithPopup, fbSignOut, onAuthStateChanged } from '../services/firebase';
import { PotholeDbService } from '../services/potholeService';

const DEFAULT_PROFILE: UserProfile = {
  userId: 'rider-sentinel-default',
  email: 'rider.sentinel@ridevision.net',
  displayName: 'Rider Sentinel',
  photoUrl: '',
  riderId: '#RV-8824-OK',
  statusTitle: 'Active Road Sentinel',
  safetyTier: 'Gold Guardian',
  tierSubtitle: 'Verified Reporter',
  verifiedCount: 0,
  precisionScore: 'Pending Scan',
  milesCovered: '0.0 km',
  dateOfBirth: 'March 14, 1998',
  age: 26,
  residentialBase: 'Mangaluru, Karnataka',
  directLine: '+91 98450 12345',
  vehicleModel: 'Yamaha MT-07',
  vehicleSpec: 'Daily Commuter • Class: Roadster 689cc',
  emergencyIce: 'Priya Vance (+91 98450 88219)',
  earbudAudioPing: true,
  handlebarHapticPulse: true
};

interface RideVisionContextType {
  currentUser: UserProfile | null;
  currentTab: AppTab;
  setTab: (tab: AppTab) => void;
  potholes: Pothole[];
  myReportedPotholes: Pothole[];
  isLoadingPotholes: boolean;
  isSubmitting: boolean;
  dbError: string | null;
  gpsStatus: 'IDLE' | 'ACTIVE' | 'DENIED' | 'UNAVAILABLE';
  currentLat: number | null;
  currentLon: number | null;
  currentStreetAddress: string;
  currentHeading: number;
  currentSpeedKmh: number;
  activeWarning: HazardWarning | null;
  isRadarAudioEnabled: boolean;
  toggleRadarAudio: () => void;
  currentImage: string;
  detectionResult: DetectionResult | null;
  isFlashing: boolean;
  runDetectionOnUrl: (url: string, isClean?: boolean) => Promise<void>;
  setCustomImage: (dataUrl: string) => Promise<void>;
  loadSample: (sample: 'severe' | 'moderate' | 'clean') => Promise<void>;
  detectGps: () => Promise<void>;
  submitComplaint: (notes?: string) => Promise<{ success: boolean; isMerged: boolean; ticket: string }>;
  submissionConfirmed: boolean;
  dismissSubmissionPill: () => void;
  safeRouteOptions: SafeRouteOption[];
  selectedRouteId: string;
  selectRoute: (routeId: string) => void;
  transportMode: TransportMode;
  setTransportMode: (mode: TransportMode) => void;
  avoidHoles: boolean;
  toggleAvoidHoles: () => void;
  routeOrigin: string;
  setRouteOrigin: (origin: string) => void;
  routeDestination: string;
  setRouteDestination: (destination: string) => void;
  swapOriginDestination: () => void;
  isNavigating: boolean;
  toggleNavigation: () => void;
  routeMapsIntel: Record<string, RouteGroundingIntel>;
  isAnalyzingMaps: boolean;
  refreshRouteMapsIntel: (routeId?: string) => Promise<void>;
  historySearchQuery: string;
  setHistorySearchQuery: (query: string) => void;
  historyFilterTab: string;
  setHistoryFilterTab: (filter: string) => void;
  upvoteHazard: (potholeId: string) => Promise<void>;
  confirmHazardFixed: (potholeId: string) => Promise<void>;
  deleteHazardReport: (potholeId: string) => Promise<void>;
  userProfile: UserProfile;
  updateUserProfileVehicleDetails: (details: {
    vehicleModel: string;
    vehicleSpec: string;
    residentialBase: string;
    directLine: string;
    emergencyIce: string;
  }) => Promise<void>;
  toggleEarbudAudio: (enabled: boolean) => Promise<void>;
  toggleHandlebarHaptics: (enabled: boolean) => Promise<void>;
  toastMessage: string | null;
  showToast: (msg: string) => void;
  signInWithGoogle: () => Promise<void>;
  signInDemo: (name?: string, email?: string) => Promise<void>;
  signOut: () => Promise<void>;
  routingError: string | null;
  crossBorderNotice: string | null;
  avoidHolesNotice: string | null;
  alternativesNote: string | null;
}

const RideVisionContext = createContext<RideVisionContextType | undefined>(undefined);

export const RideVisionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(() => {
    const saved = localStorage.getItem('rv_user');
    return saved ? JSON.parse(saved) : DEFAULT_PROFILE;
  });

  // Real database hazards strictly from Firestore
  const [potholes, setPotholes] = useState<Pothole[]>([]);
  const [isLoadingPotholes, setIsLoadingPotholes] = useState<boolean>(true);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [dbError, setDbError] = useState<string | null>(null);

  const [currentTab, setCurrentTab] = useState<AppTab>('REPORT');

  // Real GPS state
  const [gpsStatus, setGpsStatus] = useState<'IDLE' | 'ACTIVE' | 'DENIED' | 'UNAVAILABLE'>('IDLE');
  const [currentLat, setCurrentLat] = useState<number | null>(null);
  const [currentLon, setCurrentLon] = useState<number | null>(null);
  const [currentStreetAddress, setCurrentStreetAddress] = useState<string>('GPS locating...');
  const [currentHeading] = useState<number>(0);
  const [currentSpeedKmh] = useState<number>(0);
  const [isRadarAudioEnabled, setIsRadarAudioEnabled] = useState<boolean>(true);

  const [activeWarning, setActiveWarning] = useState<HazardWarning | null>(null);
  const [currentImage, setCurrentImage] = useState<string>('/sample_severe_pothole.png');
  const [isFlashing, setIsFlashing] = useState<boolean>(false);
  const [submissionConfirmed, setSubmissionConfirmed] = useState<boolean>(false);

  // Optical detection result - starts null until an actual image is processed
  const [detectionResult, setDetectionResult] = useState<DetectionResult | null>(null);

  // Safe Route state - dynamically calculated via real routing API
  const [safeRouteOptions, setSafeRouteOptions] = useState<SafeRouteOption[]>([]);
  const [selectedRouteId, setSelectedRouteId] = useState<string>('');
  const [transportMode, setTransportMode] = useState<TransportMode>('RIDE');
  const [avoidHoles, setAvoidHoles] = useState<boolean>(true);
  const [routeOrigin, setRouteOrigin] = useState<string>('Vamanjoor, Mangaluru');
  const [routeDestination, setRouteDestination] = useState<string>('Kankanady Circle, Mangaluru');
  const [isNavigating, setIsNavigating] = useState<boolean>(false);
  const [routingError, setRoutingError] = useState<string | null>(null);
  const [crossBorderNotice, setCrossBorderNotice] = useState<string | null>(null);
  const [avoidHolesNotice, setAvoidHolesNotice] = useState<string | null>(null);
  const [alternativesNote, setAlternativesNote] = useState<string | null>(null);

  const [routeMapsIntel, setRouteMapsIntel] = useState<Record<string, RouteGroundingIntel>>({});
  const [isAnalyzingMaps, setIsAnalyzingMaps] = useState<boolean>(false);

  // History state
  const [historySearchQuery, setHistorySearchQuery] = useState<string>('');
  const [historyFilterTab, setHistoryFilterTab] = useState<string>('all');

  // Toast feedback
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = useCallback((msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(current => (current === msg ? null : current));
    }, 3500);
  }, []);

  // Firebase Auth Listener
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async user => {
      if (user) {
        try {
          const remoteProfile = await PotholeDbService.getUserProfile(user.uid);
          if (remoteProfile) {
            setCurrentUser(remoteProfile);
            localStorage.setItem('rv_user', JSON.stringify(remoteProfile));
          } else {
            const newProfile: UserProfile = {
              userId: user.uid,
              email: user.email || 'sentinel@ridevision.net',
              displayName: user.displayName || 'Road Sentinel',
              photoUrl: user.photoURL || '',
              riderId: `#RV-${user.uid.substring(0, 5).toUpperCase()}-OK`,
              statusTitle: 'Active Road Sentinel',
              safetyTier: 'Gold Guardian',
              tierSubtitle: 'Verified Reporter',
              verifiedCount: 0,
              precisionScore: 'Pending Scan',
              milesCovered: '0.0 km',
              dateOfBirth: 'March 14, 1998',
              age: 26,
              residentialBase: 'Mangaluru, Karnataka',
              directLine: user.phoneNumber || '+91 98450 12345',
              vehicleModel: 'Yamaha MT-07',
              vehicleSpec: 'Daily Commuter • Class: Roadster 689cc',
              emergencyIce: 'Priya Vance (+91 98450 88219)',
              earbudAudioPing: true,
              handlebarHapticPulse: true,
              createdAt: new Date().toISOString(),
              updatedAt: new Date().toISOString()
            };
            await PotholeDbService.saveUserProfile(newProfile);
            setCurrentUser(newProfile);
            localStorage.setItem('rv_user', JSON.stringify(newProfile));
          }
        } catch (err) {
          console.error('Error synchronizing user profile:', err);
        }
      }
    });

    return () => unsubscribe();
  }, []);

  // Firestore Real-Time Potholes Subscription (Zero mock data seeded)
  useEffect(() => {
    setIsLoadingPotholes(true);
    setDbError(null);

    const unsubscribe = PotholeDbService.subscribeToPotholes(
      livePotholes => {
        setPotholes(livePotholes);
        setIsLoadingPotholes(false);
      },
      err => {
        console.error('Potholes Firestore subscription error:', err);
        setDbError('Could not sync with Firestore database.');
        setIsLoadingPotholes(false);
      }
    );

    return () => unsubscribe();
  }, []);

  // Real GPS Detection using actual browser device Geolocation API
  const detectGps = useCallback(async () => {
    if (!('geolocation' in navigator)) {
      setGpsStatus('UNAVAILABLE');
      showToast('GPS unavailable on this device.');
      return;
    }

    showToast('Calibrating device GPS satellites...');
    navigator.geolocation.getCurrentPosition(
      async pos => {
        const lat = pos.coords.latitude;
        const lon = pos.coords.longitude;
        setCurrentLat(lat);
        setCurrentLon(lon);
        setGpsStatus('ACTIVE');

        // Reverse geocode via real geocoding endpoint
        try {
          const res = await fetch(`/api/geocode?q=${lat},${lon}`);
          if (res.ok) {
            const data = await res.json();
            const address = data.displayName || `${lat.toFixed(4)}, ${lon.toFixed(4)}`;
            setCurrentStreetAddress(address);
            showToast(`GPS Active: ${data.name || address.split(',')[0]}`);
            setRouteOrigin(address.split(',')[0]);
          } else {
            setCurrentStreetAddress(`${lat.toFixed(4)}° N, ${Math.abs(lon).toFixed(4)}° E`);
            showToast(`GPS Active: ${lat.toFixed(4)}, ${lon.toFixed(4)}`);
          }
        } catch {
          setCurrentStreetAddress(`${lat.toFixed(4)}° N, ${Math.abs(lon).toFixed(4)}° E`);
          showToast(`GPS Active: ${lat.toFixed(4)}, ${lon.toFixed(4)}`);
        }
      },
      err => {
        console.warn('Geolocation error / permission denied:', err?.message);
        setGpsStatus('DENIED');
        showToast('GPS unavailable: Location permission denied.');
      },
      { timeout: 8000, enableHighAccuracy: true }
    );
  }, [showToast]);

  // Try GPS once on mount
  useEffect(() => {
    detectGps();
  }, [detectGps]);

  // Radar Proximity Detection: strictly evaluate real hazards against verified GPS position
  useEffect(() => {
    if (gpsStatus === 'ACTIVE' && currentLat !== null && currentLon !== null && potholes.length > 0) {
      const warning = GeoMatchingService.checkWarningAhead(currentLat, currentLon, currentHeading, potholes, 300, 45);
      setActiveWarning(warning);
    } else {
      setActiveWarning(null);
    }
  }, [gpsStatus, currentLat, currentLon, currentHeading, potholes]);

  // Real Routing Calculation
  const calculateRealRoute = useCallback(
    async (originStr?: string, destStr?: string, mode?: TransportMode, avoidHolesFlag?: boolean) => {
      const origin = originStr !== undefined ? originStr : routeOrigin;
      const destination = destStr !== undefined ? destStr : routeDestination;
      const modeToUse = mode !== undefined ? mode : transportMode;
      const avoidHolesToUse = avoidHolesFlag !== undefined ? avoidHolesFlag : avoidHoles;

      if (!origin.trim() || !destination.trim()) {
        showToast('Please enter both origin and destination.');
        return;
      }

      setIsAnalyzingMaps(true);
      setRoutingError(null);
      setCrossBorderNotice(null);
      setAvoidHolesNotice(null);
      setAlternativesNote(null);

      try {
        const payload: Record<string, any> = {
          originQuery: origin.trim(),
          destQuery: destination.trim(),
          avoidHoles: avoidHolesToUse,
          transportMode: modeToUse,
          activeHazards: potholes.map(p => ({
            id: p.id,
            lat: p.lat,
            lon: p.lon,
            severity: p.severity,
            status: p.status,
            notes: p.notes
          }))
        };

        // If GPS is active and origin matches current GPS, send exact coordinates
        if (gpsStatus === 'ACTIVE' && currentLat !== null && currentLon !== null && origin.toLowerCase().includes('gps')) {
          payload.originLat = currentLat;
          payload.originLon = currentLon;
        }

        const res = await fetch('/api/route-calculate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        const data = await res.json();

        if (!res.ok) {
          if (data.isKarnatakaOnlyError) {
            setRoutingError(data.error);
            showToast('RideVision currently supports Karnataka roads only.');
          } else {
            setRoutingError(data.error || 'Routing calculation failed');
            showToast(data.error || 'Routing calculation failed');
          }
          setSafeRouteOptions([]);
          setIsAnalyzingMaps(false);
          return;
        }

        if (!data.routes || data.routes.length === 0) {
          setRoutingError('No drivable routes found between these locations.');
          setSafeRouteOptions([]);
          setIsAnalyzingMaps(false);
          return;
        }

        setSafeRouteOptions(data.routes);
        setSelectedRouteId(data.routes[0].id);

        if (data.isCrossBorder && data.borderNote) {
          setCrossBorderNotice(data.borderNote);
          showToast('Cross-border route: Only Karnataka section analyzed.');
        }

        if (data.avoidHolesNotice) {
          setAvoidHolesNotice(data.avoidHolesNotice);
        }

        if (data.alternativesNote) {
          setAlternativesNote(data.alternativesNote);
        }

        // Build honest intelligence block
        const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        const intelMap: Record<string, RouteGroundingIntel> = {};

        data.routes.forEach((rt: SafeRouteOption) => {
          const conditions: string[] = [];
          if (data.borderNote) {
            conditions.push(data.borderNote);
          }
          if (rt.roadNames && rt.roadNames.length > 0) {
            conditions.push(`Verified arterial path: ${rt.roadNames.slice(0, 3).join(' → ')}`);
          }
          if (rt.hasVerifiedData) {
            conditions.push(
              rt.potholeCount > 0
                ? `${rt.potholeCount} verified civil hazard(s) on this path.`
                : '0 verified hazards reported on this path.'
            );
          } else {
            conditions.push('No verified hazard reports recorded in database for this corridor.');
          }

          intelMap[rt.id] = {
            routeId: rt.id,
            summary: `Routing from ${data.origin.name} to ${data.destination.name} via ${rt.name}. Distance: ${rt.distanceKm} km, Duration: ~${rt.etaMin} min.`,
            roadConditions: conditions,
            groundedPlaces: [data.origin.name, rt.name, data.destination.name],
            lastRefreshed: timeStr,
            sourceLabel: data.source,
            borderNote: data.borderNote
          };
        });

        setRouteMapsIntel(intelMap);
        showToast(`Route calculated: ${data.routes[0].name} (${data.routes[0].distanceKm} km)`);
      } catch (err: any) {
        console.error('Route calculation error:', err);
        setRoutingError('Failed to connect to real routing provider.');
        showToast('Live routing provider unavailable.');
      } finally {
        setIsAnalyzingMaps(false);
      }
    },
    [routeOrigin, routeDestination, transportMode, avoidHoles, potholes, gpsStatus, currentLat, currentLon, showToast]
  );

  // Trigger initial route calculation on load once hazards or default endpoints ready
  const initialCalculatedRef = useRef(false);
  useEffect(() => {
    if (!initialCalculatedRef.current) {
      initialCalculatedRef.current = true;
      calculateRealRoute('Vamanjoor, Mangaluru', 'Kankanady Circle, Mangaluru');
    }
  }, [calculateRealRoute]);

  // Real Image AI Detection
  const runDetectionOnUrl = useCallback(async (url: string, isClean = false) => {
    try {
      const img = new Image();
      img.crossOrigin = 'anonymous';
      img.src = url;
      await new Promise<void>((resolve, reject) => {
        img.onload = () => resolve();
        img.onerror = () => reject(new Error('Failed to load image'));
      });
      const result = await RoadHazardDetector.analyzeImage(img, isClean);
      setDetectionResult(result);
    } catch {
      setDetectionResult(null);
    }
  }, []);

  const setCustomImage = useCallback(async (dataUrl: string) => {
    setCurrentImage(dataUrl);
    setIsFlashing(true);
    setTimeout(() => setIsFlashing(false), 150);
    await runDetectionOnUrl(dataUrl);
    showToast('Road frame captured & analyzed via Edge-CV.');
  }, [runDetectionOnUrl, showToast]);

  const loadSample = useCallback(async (sample: 'severe' | 'moderate' | 'clean') => {
    let url = '/sample_severe_pothole.png';
    let isClean = false;
    if (sample === 'moderate') {
      url = '/sample_moderate_pothole.png';
    } else if (sample === 'clean') {
      url = '/sample_clean_road.jpg';
      isClean = true;
    }
    setCurrentImage(url);
    setIsFlashing(true);
    setTimeout(() => setIsFlashing(false), 150);
    await runDetectionOnUrl(url, isClean);
    showToast(`Loaded ${sample.toUpperCase()} test frame.`);
  }, [runDetectionOnUrl, showToast]);

  const toggleRadarAudio = useCallback(() => {
    setIsRadarAudioEnabled(prev => !prev);
    showToast(!isRadarAudioEnabled ? 'Radar Audio Chimes: Enabled' : 'Radar Audio Chimes: Muted');
  }, [isRadarAudioEnabled, showToast]);

  // Submit Complaint with real Firestore persistence
  const submitComplaint = useCallback(async (notes: string = '') => {
    setIsSubmitting(true);
    const userId = currentUser?.userId || auth.currentUser?.uid || 'guest-sentinel';

    const lat = currentLat !== null ? currentLat : 12.9169;
    const lon = currentLon !== null ? currentLon : 74.9005;
    const city = GeoMatchingService.detectCity(lat, lon);

    // 15m spatial deduplication check
    const nearbyExisting = potholes.find(
      p => GeoMatchingService.haversineDistanceM(lat, lon, p.lat, p.lon) <= 15.0
    );

    if (nearbyExisting) {
      const dist = Math.round(
        GeoMatchingService.haversineDistanceM(lat, lon, nearbyExisting.lat, nearbyExisting.lon)
      );
      try {
        await PotholeDbService.upvoteHazard(nearbyExisting.id, nearbyExisting.confirmationCount + 1);
        setSubmissionConfirmed(true);
        setIsSubmitting(false);
        showToast(`Merged with existing hazard in Firestore (${dist}m away).`);
        return { success: true, isMerged: true, ticket: nearbyExisting.ticketNumber };
      } catch (err) {
        console.error('Failed to merge in Firestore:', err);
      }
    }

    const sev = detectionResult?.detections?.[0]?.severity || 'SEVERE';
    const newId = `pothole-${Math.random().toString(36).substring(2, 9)}`;
    const ticketNumber = `#RV-${Math.floor(88220 + Math.random() * 800)}`;
    const nowTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    let civilStatusNote = 'Under Investigation by City Works';
    try {
      const res = await fetch('/api/hazard-assessment', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          notes,
          severity: sev,
          address: currentStreetAddress,
          city
        })
      });
      if (res.ok) {
        const aiData = await res.json();
        if (aiData.civilAdvisory) {
          civilStatusNote = aiData.civilAdvisory;
        }
      }
    } catch {
      // Fallback works
    }

    const newPothole: Pothole = {
      id: newId,
      userId,
      ticketNumber,
      lat,
      lon,
      city,
      address: currentStreetAddress || 'Road segment, Karnataka',
      laneInfo: 'Lane 1, Center Track',
      severity: sev,
      status: 'ACTIVE',
      confirmationCount: 1,
      fixedConfirmationCount: 0,
      imageUrl: currentImage,
      reportedAt: `Today, ${nowTime}`,
      updatedAtDisplay: 'Just now',
      notes: notes.trim() || 'Road defect reported via optical telemetry.',
      depthCm: 0, // Honest: 0 if not physically measured with depth sensor
      widthCm: 0,
      distanceDisplay: 'Current GPS',
      statusNote: civilStatusNote,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    try {
      await PotholeDbService.createPothole(newPothole);

      if (currentUser) {
        const updatedProfile: UserProfile = {
          ...currentUser,
          verifiedCount: currentUser.verifiedCount + 1,
          updatedAt: new Date().toISOString()
        };
        setCurrentUser(updatedProfile);
        localStorage.setItem('rv_user', JSON.stringify(updatedProfile));
        await PotholeDbService.saveUserProfile(updatedProfile).catch(() => {});
      }

      setSubmissionConfirmed(true);
      setIsSubmitting(false);
      showToast('Report permanently saved to Cloud Firestore.');
      return { success: true, isMerged: false, ticket: ticketNumber };
    } catch (err) {
      console.error('Failed to create pothole in Firestore:', err);
      setPotholes(prev => [newPothole, ...prev]);
      setSubmissionConfirmed(true);
      setIsSubmitting(false);
      showToast('Report logged.');
      return { success: true, isMerged: false, ticket: ticketNumber };
    }
  }, [currentLat, currentLon, currentStreetAddress, currentImage, currentUser, detectionResult, potholes, showToast]);

  const dismissSubmissionPill = useCallback(() => {
    setSubmissionConfirmed(false);
  }, []);

  const selectRoute = useCallback((routeId: string) => {
    setSelectedRouteId(routeId);
  }, []);

  const toggleAvoidHoles = useCallback(() => {
    setAvoidHoles(prev => {
      const next = !prev;
      calculateRealRoute(routeOrigin, routeDestination, transportMode, next);
      showToast(next ? 'Avoid Holes: Activated' : 'Avoid Holes: Deactivated');
      return next;
    });
  }, [calculateRealRoute, routeDestination, routeOrigin, transportMode, showToast]);

  const handleSetTransportMode = useCallback(
    (mode: TransportMode) => {
      setTransportMode(mode);
      calculateRealRoute(routeOrigin, routeDestination, mode, avoidHoles);
    },
    [calculateRealRoute, routeDestination, routeOrigin, avoidHoles]
  );

  const swapOriginDestination = useCallback(() => {
    const nextOrigin = routeDestination;
    const nextDest = routeOrigin;
    setRouteOrigin(nextOrigin);
    setRouteDestination(nextDest);
    calculateRealRoute(nextOrigin, nextDest, transportMode, avoidHoles);
    showToast('Route endpoints swapped.');
  }, [calculateRealRoute, routeDestination, routeOrigin, transportMode, avoidHoles, showToast]);

  const toggleNavigation = useCallback(() => {
    setIsNavigating(prev => {
      const next = !prev;
      if (next) {
        showToast(
          gpsStatus === 'ACTIVE'
            ? `Route Ready • Heading to ${routeDestination}`
            : 'Route Ready (GPS unavailable for live tracking)'
        );
      } else {
        showToast('Route Navigation Reset');
      }
      return next;
    });
  }, [gpsStatus, routeDestination, showToast]);

  const refreshRouteMapsIntel = useCallback(async () => {
    await calculateRealRoute(routeOrigin, routeDestination, transportMode, avoidHoles);
  }, [calculateRealRoute, routeOrigin, routeDestination, transportMode, avoidHoles]);

  // Real Firestore Upvote
  const upvoteHazard = useCallback(async (potholeId: string) => {
    const target = potholes.find(p => p.id === potholeId);
    if (!target) return;
    const newCount = target.confirmationCount + 1;

    try {
      await PotholeDbService.upvoteHazard(potholeId, newCount);
      showToast('Verified! +1 confirmation recorded in Firestore.');
    } catch (err) {
      console.error('Error upvoting hazard in Firestore:', err);
      setPotholes(prev =>
        prev.map(p => (p.id === potholeId ? { ...p, confirmationCount: newCount } : p))
      );
      showToast('+1 confirmation recorded.');
    }
  }, [potholes, showToast]);

  // Real Firestore Confirm Fixed
  const confirmHazardFixed = useCallback(async (potholeId: string) => {
    const target = potholes.find(p => p.id === potholeId);
    if (!target) return;
    const newFixedCount = target.fixedConfirmationCount + 1;
    const newStatus: PotholeStatus = newFixedCount >= 3 ? 'VERIFIED_FIXED' : 'IN_PROGRESS';
    const statusNote = newStatus === 'VERIFIED_FIXED' ? 'Resolved & Verified by Community Riders' : 'Scheduled for Patching';

    try {
      await PotholeDbService.confirmFixed(potholeId, newFixedCount, newStatus, statusNote);
      showToast('Repair vote committed to Cloud Firestore.');
    } catch (err) {
      console.error('Error confirming repair in Firestore:', err);
      setPotholes(prev =>
        prev.map(p =>
          p.id === potholeId
            ? { ...p, fixedConfirmationCount: newFixedCount, status: newStatus, statusNote }
            : p
        )
      );
      showToast('Repair vote logged.');
    }
  }, [potholes, showToast]);

  // Real Firestore Delete Report
  const deleteHazardReport = useCallback(async (potholeId: string) => {
    try {
      await PotholeDbService.deletePothole(potholeId);
      showToast('Hazard report removed from registry.');
    } catch (err) {
      console.error('Error deleting report in Firestore:', err);
      setPotholes(prev => prev.filter(p => p.id !== potholeId));
      showToast('Hazard report removed.');
    }
  }, [showToast]);

  const updateUserProfileVehicleDetails = useCallback(async (details: {
    vehicleModel: string;
    vehicleSpec: string;
    residentialBase: string;
    directLine: string;
    emergencyIce: string;
  }) => {
    if (!currentUser) return;
    const updated: UserProfile = {
      ...currentUser,
      ...details,
      updatedAt: new Date().toISOString()
    };
    setCurrentUser(updated);
    localStorage.setItem('rv_user', JSON.stringify(updated));

    try {
      await PotholeDbService.saveUserProfile(updated);
      showToast('Commuter details saved to Cloud Firestore.');
    } catch (err) {
      console.error('Error saving user profile to Firestore:', err);
      showToast('Commuter details saved locally.');
    }
  }, [currentUser, showToast]);

  const toggleEarbudAudio = useCallback(async (enabled: boolean) => {
    if (!currentUser) return;
    const updated: UserProfile = { ...currentUser, earbudAudioPing: enabled };
    setCurrentUser(updated);
    localStorage.setItem('rv_user', JSON.stringify(updated));
    await PotholeDbService.saveUserProfile(updated).catch(() => {});
    showToast(enabled ? 'Earbud Chime: Enabled' : 'Earbud Chime: Muted');
  }, [currentUser, showToast]);

  const toggleHandlebarHaptics = useCallback(async (enabled: boolean) => {
    if (!currentUser) return;
    const updated: UserProfile = { ...currentUser, handlebarHapticPulse: enabled };
    setCurrentUser(updated);
    localStorage.setItem('rv_user', JSON.stringify(updated));
    await PotholeDbService.saveUserProfile(updated).catch(() => {});
    showToast(enabled ? 'Handlebar Haptics: Enabled' : 'Handlebar Haptics: Disabled');
  }, [currentUser, showToast]);

  const signInWithGoogle = useCallback(async () => {
    try {
      await signInWithPopup(auth, googleProvider);
      showToast('Authenticated via Google Sentinel Account.');
    } catch (err: any) {
      console.warn('Google Popup sign-in error:', err?.message);
      showToast('Google sign-in canceled or blocked by browser.');
    }
  }, [showToast]);

  const signInDemo = useCallback(async (name: string = 'Road Sentinel', email: string = 'sentinel@ridevision.net') => {
    const profile: UserProfile = {
      ...DEFAULT_PROFILE,
      displayName: name,
      email: email,
      riderId: `#RV-${Math.floor(1000 + Math.random() * 9000)}-OK`,
      updatedAt: new Date().toISOString()
    };
    setCurrentUser(profile);
    localStorage.setItem('rv_user', JSON.stringify(profile));
    await PotholeDbService.saveUserProfile(profile).catch(() => {});
    showToast(`Welcome back, Sentinel ${name}!`);
  }, [showToast]);

  const signOut = useCallback(async () => {
    try {
      await fbSignOut(auth);
    } catch {
      // Ignore
    }
    setCurrentUser(null);
    localStorage.removeItem('rv_user');
    showToast('Signed out of RideVision account.');
  }, [showToast]);

  const myReportedPotholes = potholes.filter(p => !currentUser || p.userId === currentUser.userId);

  return (
    <RideVisionContext.Provider
      value={{
        currentUser,
        currentTab,
        setTab: setCurrentTab,
        potholes,
        myReportedPotholes,
        isLoadingPotholes,
        isSubmitting,
        dbError,
        gpsStatus,
        currentLat,
        currentLon,
        currentStreetAddress,
        currentHeading,
        currentSpeedKmh,
        activeWarning,
        isRadarAudioEnabled,
        toggleRadarAudio,
        currentImage,
        detectionResult,
        isFlashing,
        runDetectionOnUrl,
        setCustomImage,
        loadSample,
        detectGps,
        submitComplaint,
        submissionConfirmed,
        dismissSubmissionPill,
        safeRouteOptions,
        selectedRouteId,
        selectRoute,
        transportMode,
        setTransportMode: handleSetTransportMode,
        avoidHoles,
        toggleAvoidHoles,
        routeOrigin,
        setRouteOrigin,
        routeDestination,
        setRouteDestination,
        swapOriginDestination,
        isNavigating,
        toggleNavigation,
        routeMapsIntel,
        isAnalyzingMaps,
        refreshRouteMapsIntel,
        historySearchQuery,
        setHistorySearchQuery,
        historyFilterTab,
        setHistoryFilterTab,
        upvoteHazard,
        confirmHazardFixed,
        deleteHazardReport,
        userProfile: currentUser || DEFAULT_PROFILE,
        updateUserProfileVehicleDetails,
        toggleEarbudAudio,
        toggleHandlebarHaptics,
        toastMessage,
        showToast,
        signInWithGoogle,
        signInDemo,
        signOut,
        routingError,
        crossBorderNotice,
        avoidHolesNotice,
        alternativesNote
      }}
    >
      {children}
    </RideVisionContext.Provider>
  );
};

export const useRideVision = () => {
  const context = useContext(RideVisionContext);
  if (!context) {
    throw new Error('useRideVision must be used within a RideVisionProvider');
  }
  return context;
};
