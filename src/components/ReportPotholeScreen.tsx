import React, { useRef, useState } from 'react';
import {
  AlertTriangle,
  Volume2,
  VolumeX,
  Camera,
  Image as ImageIcon,
  CheckCircle,
  MapPin,
  Crosshair,
  Clock,
  Calendar,
  Send,
  X,
  ShieldAlert,
  MessageSquare
} from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';
import { GeoMatchingService } from '../services/geoMatchingService';

export const ReportPotholeScreen: React.FC = () => {
  const {
    currentLat,
    currentLon,
    currentStreetAddress,
    gpsStatus,
    isRadarAudioEnabled,
    toggleRadarAudio,
    currentImage,
    detectionResult,
    isFlashing,
    setCustomImage,
    loadSample,
    detectGps,
    submitComplaint,
    submissionConfirmed,
    dismissSubmissionPill,
    activeWarning,
    isSubmitting
  } = useRideVision();

  const [notes, setNotes] = useState('');
  const [isCameraActive, setIsCameraActive] = useState(false);
  const [facingMode, setFacingMode] = useState<'environment' | 'user'>('environment');
  const videoRef = useRef<HTMLVideoElement>(null);
  const mediaStreamRef = useRef<MediaStream | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  const formattedDate = new Date().toLocaleDateString('en-US', {
    month: 'short',
    day: '2-digit',
    year: 'numeric'
  }) + ' • ' + new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  const stopCameraStream = () => {
    if (mediaStreamRef.current) {
      mediaStreamRef.current.getTracks().forEach(track => track.stop());
      mediaStreamRef.current = null;
    }
    setIsCameraActive(false);
  };

  const startCamera = async (facing: 'environment' | 'user' = 'environment') => {
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        // Fall back to native file input
        cameraInputRef.current?.click();
        return;
      }
      stopCameraStream();
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: facing, width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: false
      });
      mediaStreamRef.current = stream;
      setIsCameraActive(true);
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play().catch(() => {});
      }
    } catch (err) {
      console.warn('Camera access error / fallback to file picker:', err);
      // Seamless fallback to file picker on browser block / permission decline
      cameraInputRef.current?.click();
    }
  };

  const capturePhoto = () => {
    if (videoRef.current) {
      const video = videoRef.current;
      const canvas = document.createElement('canvas');
      canvas.width = video.videoWidth || 640;
      canvas.height = video.videoHeight || 480;
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        const dataUrl = canvas.toDataURL('image/jpeg', 0.9);
        setCustomImage(dataUrl);
      }
    }
    stopCameraStream();
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = event => {
        if (event.target?.result) {
          setCustomImage(event.target.result as string);
        }
      };
      reader.readAsDataURL(file);
    }
    e.target.value = '';
  };

  const handleSubmit = async () => {
    await submitComplaint(notes);
    setNotes('');
  };

  const latForDispatch = currentLat !== null ? currentLat : 12.9169;
  const lonForDispatch = currentLon !== null ? currentLon : 74.9005;

  const dispatchInfo = GeoMatchingService.routeComplaint(
    GeoMatchingService.detectCity(latForDispatch, lonForDispatch),
    currentStreetAddress,
    latForDispatch,
    lonForDispatch,
    detectionResult?.detections?.[0]?.severity || 'SEVERE',
    notes
  );

  return (
    <div className="flex flex-col gap-3.5 pb-24 px-3.5 pt-3 max-w-md mx-auto">
      {/* 1. Live Warning Banner */}
      <div className="rounded-2xl bg-[#162F22] border border-[#213A2C] p-3.5 shadow-lg flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-[#213A2C] flex items-center justify-center shrink-0">
            <AlertTriangle className="w-5 h-5 text-[#FFD56D]" />
          </div>
          <div>
            <div className="text-[11px] font-black tracking-wider text-[#FFD56D] uppercase">
              Rider Alert Radar {gpsStatus === 'ACTIVE' ? 'Active' : 'Standby'}
            </div>
            <div className="text-xs text-[#CDE9D6] mt-0.5 leading-tight">
              {activeWarning
                ? `Verified ${activeWarning.severity} hazard ${activeWarning.distanceMeters}m ahead`
                : gpsStatus === 'ACTIVE'
                ? 'Scanning 300m bearing cone • 0 hazards ahead'
                : 'GPS standby • Scan will activate once located'}
            </div>
          </div>
        </div>
        <button
          onClick={toggleRadarAudio}
          className="w-9 h-9 rounded-full bg-[#0B2418] border border-[#213A2C] flex items-center justify-center text-[#FFD56D] hover:bg-[#162F22] transition-colors shrink-0"
          title={isRadarAudioEnabled ? 'Mute radar alert chime' : 'Enable radar alert chime'}
        >
          {isRadarAudioEnabled ? <Volume2 className="w-4 h-4" /> : <VolumeX className="w-4 h-4" />}
        </button>
      </div>

      {/* 2. Optical Capture Viewfinder Card */}
      <div className="rounded-2xl bg-[#001208] border border-[#FFD56D]/30 overflow-hidden shadow-2xl">
        {/* Card Header */}
        <div className="bg-[#0B2418] px-3.5 py-2.5 flex items-center justify-between border-b border-[#162F22]">
          <div className="flex items-center gap-2">
            <ImageIcon className="w-4 h-4 text-[#FFD56D]" />
            <span className="text-sm font-bold text-[#CDE9D6]">Captured Pothole Image</span>
          </div>
          <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#162F22] border border-[#FFD56D]/20">
            <CheckCircle className="w-3.5 h-3.5 text-[#FFD56D]" />
            <span className="text-[11px] font-extrabold text-[#FFD56D]">
              {detectionResult ? 'AI Scanned' : 'Awaiting Scan'}
            </span>
          </div>
        </div>

        {/* Viewfinder Image Display with HUD Reticle */}
        <div className="relative w-full h-64 bg-[#001208] overflow-hidden select-none">
          <img
            src={currentImage}
            alt="Pothole Viewfinder"
            className="w-full h-full object-cover"
          />

          {/* Camera Flash effect */}
          {isFlashing && (
            <div className="absolute inset-0 bg-[#FFD56D]/80 z-20 transition-opacity duration-150 animate-ping" />
          )}

          {/* Tactical Reticle Overlay */}
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none p-4">
            <div className="w-56 h-36 rounded-lg border-2 border-[#FFD56D]/90 bg-[#FFD56D]/10 backdrop-blur-[1px] p-3 flex flex-col justify-between shadow-[0_0_15px_rgba(255,213,109,0.3)]">
              <div>
                <div className="text-[11px] font-black font-mono tracking-wider text-[#FFD56D]">
                  {detectionResult?.detections?.length
                    ? `SEVERITY: ${detectionResult.detections[0].severity}`
                    : 'OPTICAL SCAN: STANDBY'}
                </div>
                <div className="text-[9px] font-mono text-white/90 mt-0.5">
                  {detectionResult?.detections?.length
                    ? `CONFIDENCE: ${Math.round(detectionResult.maxConfidence * 100)}% • SOURCE: EDGE-CV`
                    : 'Capture or select image to detect'}
                </div>
              </div>
              <div className="space-y-0.5">
                <div className="text-[8px] font-mono text-white/75">
                  {currentLat !== null && currentLon !== null
                    ? `LOC: ${currentLat.toFixed(4)}°N, ${Math.abs(currentLon).toFixed(4)}°E`
                    : 'LOC: GPS PENDING'}
                </div>
                <div className="text-[8px] font-mono text-[#FFD56D]/80">
                  {detectionResult?.detections?.length
                    ? `SCAN ID: ${detectionResult.detections[0].id.toUpperCase()}`
                    : 'SYSTEM READY • KARNATAKA GRID'}
                </div>
              </div>
            </div>
          </div>

          {/* Severity status pill */}
          <div className="absolute bottom-2.5 left-2.5 z-10 flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#001208]/90 border border-[#162F22] backdrop-blur-md">
            <span className="w-2 h-2 rounded-full bg-[#A4D1B6] animate-pulse" />
            <span className="text-[11px] font-bold text-[#CDE9D6]">
              {detectionResult
                ? detectionResult.detections.length
                  ? `Severity: ${detectionResult.detections[0].severity} Hazard`
                  : 'Road Surface Clear'
                : 'Awaiting Optical Frame'}
            </span>
          </div>

          {/* Sample quick loader */}
          <div className="absolute bottom-2.5 right-2.5 z-10 flex items-center gap-1">
            <button
              onClick={() => loadSample('severe')}
              className="text-[9px] font-extrabold px-2 py-1 rounded-md bg-[#93000A]/85 text-white border border-[#FFB4AB]/40 hover:bg-[#93000A] transition-colors"
            >
              Severe
            </button>
            <button
              onClick={() => loadSample('moderate')}
              className="text-[9px] font-extrabold px-2 py-1 rounded-md bg-[#E5B842]/85 text-[#3E2E00] border border-[#FFD56D]/40 hover:bg-[#FFD56D] transition-colors"
            >
              Moderate
            </button>
            <button
              onClick={() => loadSample('clean')}
              className="text-[9px] font-extrabold px-2 py-1 rounded-md bg-[#254F3A]/85 text-[#BFEDD1] border border-[#A4D1B6]/40 hover:bg-[#254F3A] transition-colors"
            >
              Clean
            </button>
          </div>
        </div>

        {/* Accuracy Bar & Capture Controls */}
        <div className="bg-[#0B2418] p-3.5 space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-[10px] font-black tracking-widest text-[#A4D1B6] uppercase">
                Scan Accuracy
              </div>
              <div className="text-xs font-bold text-[#CDE9D6]">
                {detectionResult ? detectionResult.summary : 'No optical scan performed yet - capture road frame to analyze'}
              </div>
            </div>
            <div className="flex items-center gap-1 px-2 py-1 rounded-lg bg-[#162F22] text-[#A4D1B6] text-[11px] font-bold">
              <CheckCircle className="w-3.5 h-3.5 text-[#A4D1B6]" />
              <span>{detectionResult ? 'Processed' : 'Ready'}</span>
            </div>
          </div>

          {/* Hidden File Inputs */}
          <input
            type="file"
            accept="image/*"
            ref={cameraInputRef}
            onChange={handleFileUpload}
            className="hidden"
          />
          <input
            type="file"
            accept="image/*"
            ref={fileInputRef}
            onChange={handleFileUpload}
            className="hidden"
          />

          <div className="grid grid-cols-2 gap-2.5">
            <button
              onClick={() => startCamera(facingMode)}
              className="flex items-center justify-center gap-2 h-11 rounded-xl bg-[#FFD56D] text-[#3E2E00] font-extrabold text-xs shadow-md hover:bg-[#EEC14A] active:scale-[0.98] transition-all"
            >
              <Camera className="w-4 h-4" />
              <span>Take Picture</span>
            </button>

            <button
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center justify-center gap-2 h-11 rounded-xl bg-[#162F22] text-[#FFD56D] font-bold text-xs border border-[#FFD56D]/30 hover:bg-[#213A2C] active:scale-[0.98] transition-all"
            >
              <ImageIcon className="w-4 h-4" />
              <span>Gallery Pick</span>
            </button>
          </div>
        </div>
      </div>

      {/* Interactive Live Camera Viewfinder Modal Overlay */}
      {isCameraActive && (
        <div className="fixed inset-0 z-50 bg-[#001208]/95 backdrop-blur-md flex flex-col items-center justify-between p-4 animate-in fade-in">
          {/* Top Bar */}
          <div className="w-full max-w-md flex items-center justify-between">
            <div className="flex items-center gap-2 text-[#FFD56D]">
              <Camera className="w-5 h-5 animate-pulse" />
              <span className="text-xs font-black tracking-widest uppercase">Live Camera Viewfinder</span>
            </div>
            <button
              onClick={stopCameraStream}
              className="w-8 h-8 rounded-full bg-[#162F22] border border-[#FFD56D]/30 flex items-center justify-center text-[#FFD56D] hover:bg-[#213A2C]"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Video Preview with Reticle */}
          <div className="relative w-full max-w-md aspect-[4/3] rounded-2xl overflow-hidden bg-black border-2 border-[#FFD56D]/40 shadow-2xl flex items-center justify-center">
            <video
              ref={videoRef}
              autoPlay
              playsInline
              muted
              className="w-full h-full object-cover"
            />
            {/* Tactical Reticle Overlay */}
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none p-6">
              <div className="w-64 h-40 rounded-xl border-2 border-dashed border-[#FFD56D] bg-[#FFD56D]/10 backdrop-blur-[1px] flex flex-col justify-between p-3">
                <div className="text-[10px] font-mono font-bold text-[#FFD56D] uppercase tracking-widest">
                  ALIGN ROAD SURFACE & POTHOLE HERE
                </div>
                <div className="text-[9px] font-mono text-white/80 self-end">
                  LIVE OPTICAL TELEMETRY
                </div>
              </div>
            </div>
          </div>

          {/* Controls Bar */}
          <div className="w-full max-w-md flex items-center justify-around pb-6">
            <button
              onClick={() => {
                const nextFacing = facingMode === 'environment' ? 'user' : 'environment';
                setFacingMode(nextFacing);
                startCamera(nextFacing);
              }}
              className="px-3 py-2 rounded-xl bg-[#162F22] border border-[#FFD56D]/30 text-[#FFD56D] text-xs font-bold"
            >
              Flip Camera
            </button>

            <button
              onClick={capturePhoto}
              className="w-16 h-16 rounded-full bg-[#FFD56D] border-4 border-[#3E2E00] flex items-center justify-center shadow-2xl hover:scale-105 active:scale-95 transition-all"
              title="Capture Photo"
            >
              <div className="w-12 h-12 rounded-full border-2 border-[#3E2E00] flex items-center justify-center">
                <Camera className="w-6 h-6 text-[#3E2E00]" />
              </div>
            </button>

            <button
              onClick={stopCameraStream}
              className="px-3 py-2 rounded-xl bg-[#162F22] border border-[#FFD56D]/30 text-[#FF5252] text-xs font-bold"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* 3. Geolocation Field Card */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-3.5 space-y-2.5 shadow-lg">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <MapPin className="w-4 h-4 text-[#FFD56D]" />
            <span className="text-[11px] font-black tracking-wider text-[#A4D1B6] uppercase">
              Pothole Location
            </span>
          </div>
          <button
            onClick={detectGps}
            className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold border transition-colors ${
              gpsStatus === 'ACTIVE'
                ? 'bg-[#254F3A] text-[#A4D1B6] border-[#A4D1B6]/30'
                : 'bg-[#162F22] text-[#FFD56D] border-[#FFD56D]/30 hover:bg-[#213A2C]'
            }`}
          >
            <Crosshair className="w-3.5 h-3.5 text-[#FFD56D]" />
            <span>{gpsStatus === 'ACTIVE' ? 'GPS Active' : 'Detect My GPS'}</span>
          </button>
        </div>

        <div className="rounded-xl bg-[#001208] p-3 flex items-center justify-between border border-[#162F22]/70">
          <div className="space-y-0.5">
            <div className="text-sm font-black text-[#CDE9D6]">
              {currentLat !== null && currentLon !== null
                ? `${currentLat.toFixed(4)}° N, ${Math.abs(currentLon).toFixed(4)}° E`
                : 'GPS unavailable'}
            </div>
            <div className="text-xs text-[#D1C5AF] font-medium truncate max-w-[240px]">
              {currentStreetAddress}
            </div>
          </div>
          <div
            className={`w-8 h-8 rounded-full flex items-center justify-center ${
              gpsStatus === 'ACTIVE' ? 'bg-[#254F3A] text-[#A4D1B6]' : 'bg-[#162F22] text-[#D1C5AF]'
            }`}
          >
            <CheckCircle className="w-4 h-4" />
          </div>
        </div>
      </div>

      {/* 4. Date & Time Field Card */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-3.5 flex items-center justify-between shadow-lg">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-[#213A2C] flex items-center justify-center text-[#FFD56D]">
            <Clock className="w-4 h-4" />
          </div>
          <div>
            <div className="text-[10px] font-black tracking-wider text-[#A4D1B6] uppercase">
              Date & Time of Detection
            </div>
            <div className="text-xs font-semibold text-[#CDE9D6]">
              {formattedDate}
            </div>
          </div>
        </div>
        <div className="w-8 h-8 rounded-lg bg-[#162F22] flex items-center justify-center text-[#FFD56D]">
          <Calendar className="w-4 h-4" />
        </div>
      </div>

      {/* 5. Commuter Notes / Description */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-3.5 space-y-1.5 shadow-lg">
        <label className="text-[10px] font-black tracking-wider text-[#A4D1B6] uppercase flex items-center gap-1.5">
          <MessageSquare className="w-3.5 h-3.5 text-[#FFD56D]" />
          <span>Commuter Notes (Optional)</span>
        </label>
        <textarea
          rows={2}
          value={notes}
          onChange={e => setNotes(e.target.value)}
          placeholder="Add landmark or hazard details..."
          className="w-full text-xs text-[#CDE9D6] bg-[#001208] border border-[#162F22] rounded-xl p-2.5 placeholder:text-[#9A907C] focus:outline-none focus:border-[#FFD56D]/50 transition-colors"
        />
      </div>

      {/* 6. Primary Submit CTA */}
      <button
        onClick={handleSubmit}
        disabled={isSubmitting}
        className="w-full h-12 rounded-2xl bg-[#FFD56D] text-[#3E2E00] font-extrabold text-sm shadow-xl shadow-[#FFD56D]/15 flex items-center justify-center gap-2 hover:bg-[#EEC14A] active:scale-[0.99] transition-all disabled:opacity-60"
      >
        <Send className="w-4 h-4" />
        <span>{isSubmitting ? 'Publishing Report to Firestore...' : 'Submit Complaint'}</span>
      </button>

      {/* Confirmation Pill Notification */}
      {submissionConfirmed && (
        <div className="rounded-2xl bg-[#254F3A] border border-[#A4D1B6]/30 p-3.5 shadow-xl flex flex-col gap-2.5 transition-all animate-in fade-in">
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-start gap-2.5">
              <ShieldAlert className="w-5 h-5 text-[#FFD56D] shrink-0 mt-0.5" />
              <div>
                <div className="text-xs font-black text-[#BFEDD1]">
                  Pothole Vector Published!
                </div>
                <div className="text-[11px] text-[#CDE9D6]/90 mt-0.5">
                  Report submitted and published to nearby riders and city works dispatch.
                </div>
              </div>
            </div>
            <button
              onClick={dismissSubmissionPill}
              className="text-[#CDE9D6] hover:text-white p-1"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <a
            href={dispatchInfo.actionUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center justify-center gap-2 py-2 px-3 rounded-xl bg-[#001208] border border-[#FFD56D]/40 text-[#FFD56D] text-xs font-bold hover:bg-[#0B2418] transition-colors"
          >
            <span>Dispatch to Official {dispatchInfo.cityName} Works ({dispatchInfo.channelType.toUpperCase()})</span>
          </a>
        </div>
      )}
    </div>
  );
};
