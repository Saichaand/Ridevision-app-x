export type Severity = 'MINOR' | 'MODERATE' | 'SEVERE';

export interface SeverityMeta {
  label: string;
  levelText: string;
}

export const SEVERITY_INFO: Record<Severity, SeverityMeta> = {
  MINOR: { label: 'Minor', levelText: 'MINOR • LVL 1' },
  MODERATE: { label: 'Moderate', levelText: 'MODERATE • LVL 2' },
  SEVERE: { label: 'Severe', levelText: 'HIGH RISK • LVL 4' }
};

export type PotholeStatus = 'ACTIVE' | 'IN_PROGRESS' | 'VERIFIED_FIXED';

export interface PotholeStatusMeta {
  label: string;
  filterKey: string;
}

export const STATUS_INFO: Record<PotholeStatus, PotholeStatusMeta> = {
  ACTIVE: { label: 'Under Investigation by City Works', filterKey: 'pending' },
  IN_PROGRESS: { label: 'Scheduled for Patching', filterKey: 'progress' },
  VERIFIED_FIXED: { label: 'Resolved & Verified by Riders', filterKey: 'repaired' }
};

export interface Pothole {
  id: string;
  userId: string;
  ticketNumber: string;
  lat: number;
  lon: number;
  city: string;
  address: string;
  laneInfo: string;
  severity: Severity;
  status: PotholeStatus;
  confirmationCount: number;
  fixedConfirmationCount: number;
  imageUrl?: string;
  reportedAt: string;
  updatedAtDisplay: string;
  notes: string;
  depthCm: number;
  widthCm: number;
  distanceDisplay: string;
  statusNote: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface BoxNorm {
  left: number;
  top: number;
  right: number;
  bottom: number;
}

export interface Detection {
  id: string;
  boxNorm: BoxNorm;
  confidence: number;
  severity: Severity;
  areaRatio: number;
  engine: string;
}

export interface DetectionResult {
  detections: Detection[];
  processingTimeMs: number;
  maxConfidence: number;
  roadConditionScore: number;
  summary: string;
  estimatedDepthCm: number;
}

export interface HazardWarning {
  potholeId: string;
  distanceMeters: number;
  angularDeviationDeg: number;
  severity: Severity;
  address: string;
  confirmationCount: number;
  message: string;
  isUrgent: boolean;
}

export interface SafeRouteOption {
  id: string;
  name: string;
  etaMin: number;
  distanceKm: number;
  distanceMeters?: number;
  durationSeconds?: number;
  potholeCount: number | null;
  conditionIndex: number | null;
  riskLabel: 'Safest' | 'Fastest' | 'Balanced' | 'Optimal' | 'Moderate' | 'Severe Risk';
  description: string;
  isOptimal: boolean;
  liveMapsIntel?: string;
  coordinates?: [number, number][];
  hasVerifiedData?: boolean;
  roadNames?: string[];
  roadSegments?: string[];
  verifiedHazardDataAvailable?: boolean;
  verifiedHazardCount?: number | null;
  weightedHazardScore?: number | null;
  hazardDensity?: number | null;
  safetyScore?: number | null;
  riskLevel?: string;
  recommendation?: string;
  matchedHazardPoints?: { lat: number; lon: number; severity: Severity; notes: string }[];
}

export interface RouteGroundingIntel {
  routeId: string;
  summary: string;
  roadConditions: string[];
  groundedPlaces: string[];
  lastRefreshed: string;
  sourceLabel?: string;
  borderNote?: string | null;
}

export interface UserProfile {
  userId: string;
  email: string;
  displayName: string;
  photoUrl: string;
  riderId: string;
  statusTitle: string;
  safetyTier: string;
  tierSubtitle: string;
  verifiedCount: number;
  precisionScore: string;
  milesCovered: string;
  dateOfBirth: string;
  age: number;
  residentialBase: string;
  directLine: string;
  vehicleModel: string;
  vehicleSpec: string;
  emergencyIce: string;
  earbudAudioPing: boolean;
  handlebarHapticPulse: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CityConfig {
  cityName: string;
  authorityName: string;
  channelType: 'whatsapp' | 'helpline' | 'email';
  contactValue: string;
  instructions: string;
}

export interface MunicipalDispatch {
  cityName: string;
  authorityName: string;
  channelType: 'whatsapp' | 'helpline' | 'email';
  contactValue: string;
  prefilledMessage: string;
  actionUrl: string;
  instructions: string;
}

export type AppTab = 'REPORT' | 'SAFE_ROUTE' | 'HISTORY' | 'PROFILE';
export type TransportMode = 'RIDE' | 'CAR' | 'FLEET';
