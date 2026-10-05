import React, { useState, useMemo } from 'react';
import {
  Navigation,
  ArrowUpDown,
  Bike,
  Car,
  Truck,
  ShieldCheck,
  ShieldAlert,
  Layers,
  Crosshair,
  Plus,
  Minus,
  Sparkles,
  MapPin,
  Clock,
  Sliders,
  CheckCircle,
  Flag,
  AlertCircle
} from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';
import { SafeRouteOption, TransportMode } from '../types/models';

export const SafeRouteScreen: React.FC = () => {
  const {
    safeRouteOptions,
    selectedRouteId,
    selectRoute,
    transportMode,
    setTransportMode,
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
    detectGps,
    gpsStatus,
    routingError,
    crossBorderNotice,
    avoidHolesNotice,
    alternativesNote
  } = useRideVision();

  const [is3DMode, setIs3DMode] = useState(false);
  const [zoomLevel, setZoomLevel] = useState(1);

  const selectedOption: SafeRouteOption | undefined =
    safeRouteOptions.find(o => o.id === selectedRouteId) || safeRouteOptions[0];

  const currentIntel = selectedOption ? routeMapsIntel[selectedOption.id] : undefined;

  // Real SVG Projection of GeoJSON LineString Coordinates
  const { routePaths, originPoint, destPoint, hazardMarkers } = useMemo(() => {
    if (!safeRouteOptions.length) {
      return { routePaths: [], originPoint: null, destPoint: null, hazardMarkers: [] };
    }

    // Collect all coordinates across all routes to determine common bounding box
    let allCoords: [number, number][] = [];
    safeRouteOptions.forEach(opt => {
      if (opt.coordinates && opt.coordinates.length) {
        allCoords = allCoords.concat(opt.coordinates);
      }
    });

    if (!allCoords.length) {
      return { routePaths: [], originPoint: null, destPoint: null, hazardMarkers: [] };
    }

    let minLon = Infinity;
    let maxLon = -Infinity;
    let minLat = Infinity;
    let maxLat = -Infinity;

    allCoords.forEach(([lon, lat]) => {
      if (lon < minLon) minLon = lon;
      if (lon > maxLon) maxLon = lon;
      if (lat < minLat) minLat = lat;
      if (lat > maxLat) maxLat = lat;
    });

    const lonSpan = maxLon - minLon || 0.001;
    const latSpan = maxLat - minLat || 0.001;

    // ViewBox area: X between 50 and 350, Y between 180 and 420
    const project = (lon: number, lat: number) => {
      const x = 50 + ((lon - minLon) / lonSpan) * 300;
      const y = 420 - ((lat - minLat) / latSpan) * 240;
      return { x: Math.max(30, Math.min(370, x)), y: Math.max(160, Math.min(440, y)) };
    };

    const paths = safeRouteOptions.map((opt, idx) => {
      const coords = opt.coordinates || [];
      if (coords.length === 0) return { id: opt.id, d: '', isSelected: opt.id === selectedOption?.id, idx };

      // Sample every few points if very long to smooth SVG rendering
      const step = Math.max(1, Math.floor(coords.length / 100));
      const sampled = coords.filter((_, i) => i === 0 || i === coords.length - 1 || i % step === 0);
      const points = sampled.map(c => project(c[0], c[1]));
      const d = `M ${points.map(p => `${p.x.toFixed(1)} ${p.y.toFixed(1)}`).join(' L ')}`;

      return {
        id: opt.id,
        d,
        isSelected: opt.id === selectedOption?.id,
        idx
      };
    });

    const activeCoords = selectedOption?.coordinates || [];
    const orig = activeCoords.length ? project(activeCoords[0][0], activeCoords[0][1]) : null;
    const dest = activeCoords.length ? project(activeCoords[activeCoords.length - 1][0], activeCoords[activeCoords.length - 1][1]) : null;

    const hazards = (selectedOption?.matchedHazardPoints || []).map(h => {
      const pt = project(h.lon, h.lat);
      return { ...h, x: pt.x, y: pt.y };
    });

    return { routePaths: paths, originPoint: orig, destPoint: dest, hazardMarkers: hazards };
  }, [safeRouteOptions, selectedOption]);

  return (
    <div className="flex flex-col pb-24 max-w-md mx-auto min-h-screen">
      {/* 1. Interactive Vector Map Canvas & HUD Overlays */}
      <div className="relative w-full h-[470px] bg-[#001208] overflow-hidden select-none border-b border-[#162F22]">
        {/* SVG Vector Map Rendering with Grid & Glowing Curves */}
        <svg
          className={`w-full h-full transition-transform duration-300 ${
            is3DMode ? 'scale-[1.08] -rotate-1 skew-x-1' : ''
          }`}
          style={{ transform: `scale(${zoomLevel})` }}
          viewBox="0 0 400 480"
          preserveAspectRatio="none"
        >
          <defs>
            {/* Grid pattern */}
            <pattern id="radarGrid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path
                d="M 40 0 L 0 0 0 40"
                fill="none"
                stroke="#0F3020"
                strokeWidth="1"
                strokeDasharray="3,6"
                strokeOpacity="0.45"
              />
            </pattern>

            {/* Glowing route filters */}
            <filter id="goldGlow" x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="5" result="blur" />
              <feMerge>
                <feMergeNode in="blur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
            <filter id="hazardGlow" x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="4" result="blur" />
              <feMerge>
                <feMergeNode in="blur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
          </defs>

          {/* Grid Background */}
          <rect width="100%" height="100%" fill="url(#radarGrid)" />

          {/* Dark Arterial Roads Base Network */}
          <path
            d="M 60 0 L 60 480 M 0 220 L 400 220 M 180 0 L 180 480 M 0 350 L 400 350"
            stroke="#072014"
            strokeWidth="24"
            strokeLinecap="round"
          />

          {/* Real routes rendered dynamically from OpenStreetMap / OSRM */}
          {routePaths.map(p => {
            if (!p.d) return null;
            if (p.isSelected) {
              return (
                <g key={p.id}>
                  {/* Outer Glow Ribbon */}
                  <path
                    d={p.d}
                    fill="none"
                    stroke="#FFD56D"
                    strokeWidth="14"
                    strokeOpacity="0.25"
                    strokeLinecap="round"
                  />
                  {/* Glowing Core */}
                  <path
                    d={p.d}
                    fill="none"
                    stroke="#FFD56D"
                    strokeWidth="5.5"
                    strokeLinecap="round"
                    filter="url(#goldGlow)"
                  />
                  {/* Center Tracer */}
                  <path
                    d={p.d}
                    fill="none"
                    stroke="#FFFFFF"
                    strokeWidth="1.8"
                    strokeLinecap="round"
                    strokeOpacity="0.85"
                  />
                </g>
              );
            }
            // Alternative route paths in contrasting colors
            const altColor = p.idx === 1 ? '#E5B842' : '#93000A';
            return (
              <path
                key={p.id}
                d={p.d}
                fill="none"
                stroke={altColor}
                strokeWidth="3.5"
                strokeDasharray="6,6"
                strokeLinecap="round"
                strokeOpacity="0.75"
              />
            );
          })}
        </svg>

        {/* Top Floating Route Planning Capsule */}
        <div className="absolute top-2.5 inset-x-2.5 z-20">
          <div className="rounded-2xl bg-[#001208]/95 border border-[#FFD56D]/25 p-3 shadow-2xl backdrop-blur-md space-y-2.5">
            <div className="flex items-center gap-2">
              {/* Waypoint Line */}
              <div className="flex flex-col items-center justify-between h-16 py-1">
                <span className="w-2.5 h-2.5 rounded-full bg-[#A4D1B6] shadow-sm shadow-[#A4D1B6]" />
                <span className="w-0.5 h-6 bg-[#A4D1B6]/40" />
                <span className="w-2.5 h-2.5 rounded-sm bg-[#FFD56D] shadow-sm shadow-[#FFD56D]" />
              </div>

              {/* Waypoint Text Inputs */}
              <div className="flex-1 space-y-1.5">
                {/* Origin */}
                <div className="relative flex items-center">
                  <input
                    type="text"
                    value={routeOrigin}
                    onChange={e => setRouteOrigin(e.target.value)}
                    placeholder="START LOCATION"
                    className="w-full text-xs font-semibold text-[#CDE9D6] bg-[#0B2418] border border-[#162F22] rounded-lg py-1.5 pl-2.5 pr-14 focus:outline-none focus:border-[#A4D1B6]"
                  />
                  <button
                    onClick={detectGps}
                    className={`absolute right-1 text-[9px] font-extrabold px-1.5 py-0.5 rounded transition-colors ${
                      gpsStatus === 'ACTIVE'
                        ? 'bg-[#254F3A] text-[#A4D1B6]'
                        : 'bg-[#162F22] text-[#D1C5AF] hover:bg-[#213A2C]'
                    }`}
                    title={gpsStatus === 'ACTIVE' ? 'GPS Active' : 'Calibrate GPS'}
                  >
                    {gpsStatus === 'ACTIVE' ? 'GPS OK' : 'GPS'}
                  </button>
                </div>

                {/* Destination */}
                <div className="relative flex items-center">
                  <input
                    type="text"
                    value={routeDestination}
                    onChange={e => setRouteDestination(e.target.value)}
                    placeholder="DESTINATION"
                    className="w-full text-xs font-semibold text-[#CDE9D6] bg-[#0B2418] border border-[#162F22] rounded-lg py-1.5 pl-2.5 pr-8 focus:outline-none focus:border-[#FFD56D]"
                  />
                  <MapPin className="absolute right-2 w-3.5 h-3.5 text-[#FFD56D]" />
                </div>
              </div>

              {/* Invert / Swap Locations Button */}
              <button
                onClick={swapOriginDestination}
                className="w-9 h-9 rounded-xl bg-[#162F22] border border-[#FFD56D]/30 flex items-center justify-center text-[#FFD56D] hover:bg-[#213A2C] transition-colors shrink-0"
                title="Swap origin and destination"
              >
                <ArrowUpDown className="w-4 h-4" />
              </button>
            </div>

            {/* Error or Cross-border notification banner */}
            {routingError && (
              <div className="p-2 rounded-lg bg-red-950/80 border border-red-500/40 text-xs text-red-200 flex items-center gap-2">
                <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
                <span>{routingError}</span>
              </div>
            )}
            {crossBorderNotice && !routingError && (
              <div className="p-2 rounded-lg bg-amber-950/80 border border-amber-500/40 text-[11px] text-amber-200 flex items-center gap-1.5">
                <AlertCircle className="w-3.5 h-3.5 text-amber-400 shrink-0" />
                <span>{crossBorderNotice}</span>
              </div>
            )}
            {avoidHolesNotice && !routingError && (
              <div className="p-1.5 rounded-lg bg-[#0B2418] border border-[#162F22] text-[10px] text-[#A4D1B6]">
                {avoidHolesNotice}
              </div>
            )}

            {/* Calculate & Grounding Trigger */}
            <button
              onClick={() => refreshRouteMapsIntel()}
              disabled={isAnalyzingMaps}
              className="w-full h-8 rounded-lg bg-[#FFD56D] text-[#3E2E00] font-black text-xs flex items-center justify-center gap-1.5 hover:bg-[#EEC14A] transition-colors shadow-sm disabled:opacity-60"
            >
              <Sparkles className="w-3.5 h-3.5" />
              <span>{isAnalyzingMaps ? 'Calculating Real Route from OpenStreetMap...' : 'Analyze & Calculate Safe Route'}</span>
            </button>

            {/* Transport Modes & Avoid Holes Toggle Bar */}
            <div className="flex items-center justify-between pt-0.5 border-t border-[#162F22]">
              <div className="flex items-center gap-1.5">
                {(['RIDE', 'CAR', 'FLEET'] as TransportMode[]).map(mode => {
                  const isSelected = transportMode === mode;
                  const IconComp = mode === 'RIDE' ? Bike : mode === 'CAR' ? Car : Truck;
                  return (
                    <button
                      key={mode}
                      onClick={() => setTransportMode(mode)}
                      className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold transition-all ${
                        isSelected
                          ? 'bg-[#FFD56D] text-[#3E2E00] shadow-sm'
                          : 'bg-[#0B2418] text-[#D1C5AF] hover:bg-[#162F22]'
                      }`}
                    >
                      <IconComp className="w-3.5 h-3.5" />
                      <span>{mode === 'RIDE' ? 'Ride' : mode === 'CAR' ? 'Car' : 'Fleet'}</span>
                    </button>
                  );
                })}
              </div>

              {/* Avoid Holes Active Pill */}
              <button
                onClick={toggleAvoidHoles}
                className={`flex items-center gap-1 px-2 py-1 rounded-full text-[10px] font-bold border transition-colors ${
                  avoidHoles
                    ? 'bg-[#254F3A] text-[#A4D1B6] border-[#A4D1B6]/30'
                    : 'bg-[#0B2418] text-[#D1C5AF] border-[#162F22]'
                }`}
              >
                <ShieldCheck className="w-3.5 h-3.5" />
                <span>Avoid Holes</span>
                {avoidHoles && <span className="w-1.5 h-1.5 rounded-full bg-[#A4D1B6]" />}
              </button>
            </div>
          </div>
        </div>

        {/* Real Map Waypoint Pins */}
        {originPoint && (
          <div
            style={{ position: 'absolute', top: `${originPoint.y - 14}px`, left: `${originPoint.x - 14}px` }}
            className="flex flex-col items-center pointer-events-none z-10"
          >
            <div className="w-7 h-7 rounded-full bg-[#254F3A] border-2 border-[#A4D1B6] flex items-center justify-center text-[#A4D1B6] shadow-lg">
              <Navigation className="w-4 h-4" />
            </div>
            <span className="text-[9px] font-black text-[#A4D1B6] bg-[#001208]/90 px-1.5 py-0.5 rounded mt-0.5 border border-[#162F22] whitespace-nowrap">
              Origin
            </span>
          </div>
        )}

        {destPoint && (
          <div
            style={{ position: 'absolute', top: `${destPoint.y - 16}px`, left: `${destPoint.x - 16}px` }}
            className="flex flex-col items-center pointer-events-none z-10"
          >
            <div className="w-8 h-8 rounded-full bg-[#FFD56D] border-2 border-[#3E2E00] flex items-center justify-center text-[#3E2E00] shadow-xl">
              <Flag className="w-4 h-4" />
            </div>
            <span className="text-[9px] font-black text-[#3E2E00] bg-[#FFD56D] px-2 py-0.5 rounded mt-0.5 shadow whitespace-nowrap">
              Destination
            </span>
          </div>
        )}

        {/* Real verified hazard points matching the corridor from Firestore */}
        {hazardMarkers.map((h, i) => (
          <div
            key={i}
            style={{ position: 'absolute', top: `${h.y - 12}px`, left: `${h.x - 12}px` }}
            className="flex items-center gap-1.5 pointer-events-none z-10"
          >
            <div className="w-6 h-6 rounded-full bg-[#93000A] border-2 border-[#FFB4AB] flex items-center justify-center text-[#FFB4AB] shadow-lg animate-pulse">
              <ShieldAlert className="w-3 h-3" />
            </div>
            <div className="bg-[#001208]/95 border border-[#93000A] rounded px-1.5 py-0.5 text-[9px] text-[#FFB4AB] font-bold">
              {h.severity} Hazard
            </div>
          </div>
        ))}

        {/* Honest Road Condition Ribbon Badge */}
        <div className="absolute bottom-[20px] left-[35px] flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#254F3A]/90 border border-[#A4D1B6]/30 text-[#A4D1B6] text-xs font-black shadow-lg">
          <CheckCircle className="w-3.5 h-3.5" />
          <span>
            {selectedOption?.conditionIndex !== null && selectedOption?.conditionIndex !== undefined
              ? `${selectedOption.conditionIndex}% Condition Index`
              : 'Road condition data unavailable'}
          </span>
        </div>

        {/* Right Cockpit Floating Controls */}
        <div className="absolute top-[170px] right-3 flex flex-col gap-2 z-20">
          <button
            onClick={() => {}}
            className="w-9 h-9 rounded-xl bg-[#001208]/90 border border-[#213A2C] flex items-center justify-center text-[#FFD56D] hover:bg-[#162F22] transition-colors"
            title="Toggle Layers"
          >
            <Layers className="w-4 h-4" />
          </button>

          <button
            onClick={() => setIs3DMode(!is3DMode)}
            className={`w-9 h-9 rounded-xl border flex items-center justify-center font-black text-xs transition-colors ${
              is3DMode
                ? 'bg-[#FFD56D] text-[#3E2E00] border-[#FFD56D]'
                : 'bg-[#001208]/90 text-[#FFD56D] border-[#213A2C] hover:bg-[#162F22]'
            }`}
            title="Toggle 3D View"
          >
            3D
          </button>

          <button
            onClick={detectGps}
            className={`w-9 h-9 rounded-xl bg-[#001208]/90 border flex items-center justify-center transition-colors ${
              gpsStatus === 'ACTIVE'
                ? 'border-[#A4D1B6] text-[#A4D1B6]'
                : 'border-[#213A2C] text-[#D1C5AF] hover:bg-[#162F22]'
            }`}
            title={gpsStatus === 'ACTIVE' ? 'GPS Active' : 'Calibrate GPS'}
          >
            <Crosshair className="w-4 h-4" />
          </button>

          <div className="flex flex-col rounded-xl bg-[#001208]/90 border border-[#213A2C] overflow-hidden">
            <button
              onClick={() => setZoomLevel(z => Math.min(1.4, z + 0.1))}
              className="w-9 h-8 flex items-center justify-center text-[#CDE9D6] hover:bg-[#162F22] transition-colors"
              title="Zoom in"
            >
              <Plus className="w-4 h-4" />
            </button>
            <div className="w-full h-px bg-[#213A2C]" />
            <button
              onClick={() => setZoomLevel(z => Math.max(0.8, z - 0.1))}
              className="w-9 h-8 flex items-center justify-center text-[#CDE9D6] hover:bg-[#162F22] transition-colors"
              title="Zoom out"
            >
              <Minus className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* 2. Bottom Drawer / Route Comparison & Telemetry */}
      <div className="bg-[#001208] border-t border-[#162F22] p-4 space-y-4">
        {selectedOption ? (
          <>
            {/* Selected Route Title & Safe Badge */}
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-[#FFD56D]" />
                <h2 className="text-base font-black text-[#CDE9D6]">{selectedOption.name}</h2>
              </div>
              <div className="flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[#254F3A] text-[#A4D1B6] text-[10px] font-black tracking-wider">
                <CheckCircle className="w-3 h-3" />
                <span>{selectedOption.recommendation || (selectedOption.isOptimal ? 'SAFEST ROUTE' : (selectedOption.riskLabel.toUpperCase() + ' ROUTE'))}</span>
              </div>
            </div>

            {/* Road Surface & Hazard Telemetry Bento Grid */}
            <div className="grid grid-cols-3 gap-2">
              {/* Potholes Card */}
              <div className="rounded-xl bg-[#0B2418] border border-[#162F22] p-2.5 flex flex-col justify-between">
                <div className="flex items-center justify-between text-[9px] font-bold text-[#A4D1B6] uppercase">
                  <span>Potholes</span>
                  <ShieldCheck className="w-3.5 h-3.5 text-[#A4D1B6]" />
                </div>
                <div className="text-2xl font-black text-[#A4D1B6] my-0.5">
                  {selectedOption.verifiedHazardCount !== null && selectedOption.verifiedHazardCount !== undefined
                    ? selectedOption.verifiedHazardCount
                    : 'N/A'}
                </div>
                <div className="text-[10px] text-[#D1C5AF]">
                  {selectedOption.verifiedHazardDataAvailable
                    ? (selectedOption.verifiedHazardCount === 0 ? '0 verified hazards' : 'Verified hazards')
                    : 'No verified data'}
                </div>
              </div>

              {/* ETA Card */}
              <div className="rounded-xl bg-[#0B2418] border border-[#162F22] p-2.5 flex flex-col justify-between">
                <div className="flex items-center justify-between text-[9px] font-bold text-[#FFD56D] uppercase">
                  <span>ETA</span>
                  <Clock className="w-3.5 h-3.5 text-[#FFD56D]" />
                </div>
                <div className="flex items-baseline text-2xl font-black text-[#FFD56D] my-0.5">
                  <span>{selectedOption.etaMin}</span>
                  <span className="text-xs font-normal text-[#D1C5AF] ml-0.5">min</span>
                </div>
                <div className="text-[10px] text-[#D1C5AF]">{selectedOption.distanceKm} km total</div>
              </div>

              {/* Index Card */}
              <div className="rounded-xl bg-[#0B2418] border border-[#162F22] p-2.5 flex flex-col justify-between">
                <div className="flex items-center justify-between text-[9px] font-bold text-[#FFD56F] uppercase">
                  <span>Index</span>
                  <Sliders className="w-3.5 h-3.5 text-[#FFD56F]" />
                </div>
                <div className="flex items-baseline text-2xl font-black text-[#CDE9D6] my-0.5">
                  {selectedOption.conditionIndex !== null ? (
                    <>
                      <span>{selectedOption.conditionIndex}</span>
                      <span className="text-xs font-normal text-[#FFD56F] ml-0.5">/100</span>
                    </>
                  ) : (
                    <span>N/A</span>
                  )}
                </div>
                <div className="text-[10px] text-[#D1C5AF]">
                  {selectedOption.conditionIndex !== null ? selectedOption.riskLabel : 'Data unavailable'}
                </div>
              </div>
            </div>

            {/* Honest Grounding Intelligence Block */}
            {currentIntel && (
              <div className="rounded-xl bg-[#072014] border border-[#254F3A] p-3 space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1.5 text-[#FFD56D] text-xs font-black">
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>Corridor Road & Hazard Intelligence</span>
                  </div>
                  <span className="text-[10px] text-[#D1C5AF]">{currentIntel.lastRefreshed}</span>
                </div>
                <p className="text-xs text-[#CDE9D6] leading-relaxed">
                  {currentIntel.summary}
                </p>
                <div className="space-y-1">
                  {currentIntel.roadConditions.map((cond, idx) => (
                    <div key={idx} className="flex items-start gap-1.5 text-[11px] text-[#A4D1B6]">
                      <span className="text-[#FFD56D] font-bold mt-0.5">•</span>
                      <span>{cond}</span>
                    </div>
                  ))}
                </div>
                <div className="text-[10px] text-[#D1C5AF]/70 pt-1 border-t border-[#162F22]">
                  {currentIntel.sourceLabel || 'Source: OpenStreetMap Routing Engine & Karnataka Civic Registry'}
                </div>
              </div>
            )}

            {/* Alternative Safe Corridors List (Renders ONLY actual routes returned by OSRM) */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="text-[11px] font-black text-[#D1C5AF] tracking-wider uppercase">
                  Alternative Safe Corridors ({safeRouteOptions.length})
                </div>
                {(alternativesNote || safeRouteOptions.length <= 1) && (
                  <span className="text-[10px] text-[#A4D1B6]/80 font-bold">
                    {alternativesNote || 'No additional alternative route available.'}
                  </span>
                )}
              </div>

              {safeRouteOptions.map(opt => {
                const isSelected = selectedRouteId === opt.id;
                return (
                  <div
                    key={opt.id}
                    onClick={() => selectRoute(opt.id)}
                    className={`p-3 rounded-xl border flex items-center justify-between cursor-pointer transition-all ${
                      isSelected
                        ? 'bg-[#162F22] border-[#FFD56D]/50 shadow-md'
                        : 'bg-[#0B2418]/60 border-[#162F22] hover:bg-[#162F22]/40'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-9 h-9 rounded-lg flex items-center justify-center shrink-0 ${
                          opt.isOptimal
                            ? 'bg-[#FFD56D]/20 text-[#FFD56D]'
                            : opt.riskLabel === 'Severe Risk'
                            ? 'bg-[#93000A]/40 text-[#FFB4AB]'
                            : 'bg-[#213A2C] text-[#D1C5AF]'
                        }`}
                      >
                        {opt.isOptimal ? (
                          <Bike className="w-4 h-4" />
                        ) : opt.riskLabel === 'Severe Risk' ? (
                          <AlertCircle className="w-4 h-4" />
                        ) : (
                          <Navigation className="w-4 h-4" />
                        )}
                      </div>
                      <div>
                        <div className="flex items-center gap-1.5">
                          <span className="text-xs font-bold text-[#CDE9D6]">{opt.name}</span>
                          {opt.isOptimal && <span className="w-1.5 h-1.5 rounded-full bg-[#A4D1B6]" />}
                        </div>
                        <div
                          className={`text-[11px] mt-0.5 ${
                            opt.riskLabel === 'Severe Risk' ? 'text-[#FFB4AB]' : 'text-[#D1C5AF]'
                          }`}
                        >
                          {opt.description}
                        </div>
                      </div>
                    </div>

                    <div className="text-right">
                      <div
                        className={`text-xs font-black ${
                          opt.isOptimal ? 'text-[#FFD56D]' : 'text-[#CDE9D6]'
                        }`}
                      >
                        {opt.etaMin} min
                      </div>
                      <div
                        className={`text-[11px] font-bold ${
                          opt.riskLabel === 'Safest' || opt.riskLabel === 'Optimal'
                            ? 'text-[#A4D1B6]'
                            : opt.riskLabel === 'Fastest' || opt.riskLabel === 'Balanced'
                            ? 'text-[#FFD56F]'
                            : 'text-[#FFB4AB]'
                        }`}
                      >
                        {opt.riskLabel}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </>
        ) : (
          <div className="text-center py-6 text-xs text-[#D1C5AF]">
            {routingError || 'Enter origin and destination within Karnataka to calculate safe routing.'}
          </div>
        )}

        {/* Primary Safe Navigation Launcher Button */}
        <button
          onClick={toggleNavigation}
          className="w-full h-12 rounded-2xl bg-[#FFD56D] text-[#3E2E00] font-black text-sm flex items-center justify-center gap-2 shadow-xl shadow-[#FFD56D]/15 hover:bg-[#EEC14A] active:scale-[0.99] transition-all"
        >
          <Navigation className="w-4 h-4" />
          <span>{isNavigating ? 'Route Ready (Tap to Reset)' : 'Start Safe Navigation'}</span>
        </button>
      </div>
    </div>
  );
};
