import React, { useState } from 'react';
import {
  BarChart3,
  Search,
  SlidersHorizontal,
  ShieldAlert,
  Clock,
  Compass,
  ThumbsUp,
  Map,
  PlusCircle,
  Wrench,
  CheckCircle2,
  ExternalLink,
  Trash2,
  Loader2,
  Users,
  UserCheck
} from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';
import { Pothole, SEVERITY_INFO } from '../types/models';
import { GeoMatchingService } from '../services/geoMatchingService';

export const ComplaintHistoryScreen: React.FC = () => {
  const {
    potholes,
    myReportedPotholes,
    isLoadingPotholes,
    dbError,
    currentUser,
    historySearchQuery,
    setHistorySearchQuery,
    historyFilterTab,
    setHistoryFilterTab,
    upvoteHazard,
    confirmHazardFixed,
    deleteHazardReport,
    setTab
  } = useRideVision();

  const [viewScope, setViewScope] = useState<'community' | 'mine'>('community');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  const targetList = viewScope === 'mine' ? myReportedPotholes : potholes;

  const totalCount = targetList.length;
  const resolvedCount = targetList.filter(p => p.status === 'VERIFIED_FIXED').length;
  const progressCount = targetList.filter(p => p.status === 'IN_PROGRESS').length;
  const criticalCount = targetList.filter(p => p.status === 'ACTIVE').length;

  const filterTabs = [
    { key: 'all', label: `All Reports (${totalCount})` },
    { key: 'pending', label: `Pending (${criticalCount})` },
    { key: 'progress', label: `In Progress (${progressCount})` },
    { key: 'repaired', label: `Repaired (${resolvedCount})` }
  ];

  const filteredList = targetList.filter(p => {
    const matchesTab =
      historyFilterTab === 'all'
        ? true
        : historyFilterTab === 'pending'
        ? p.status === 'ACTIVE'
        : historyFilterTab === 'progress'
        ? p.status === 'IN_PROGRESS'
        : p.status === 'VERIFIED_FIXED';

    const query = historySearchQuery.toLowerCase().trim();
    const matchesQuery =
      !query ||
      p.address.toLowerCase().includes(query) ||
      p.ticketNumber.toLowerCase().includes(query) ||
      p.notes.toLowerCase().includes(query) ||
      p.city.toLowerCase().includes(query);

    return matchesTab && matchesQuery;
  });

  const handleDelete = async (id: string) => {
    await deleteHazardReport(id);
    setDeleteConfirmId(null);
  };

  return (
    <div className="flex flex-col gap-3.5 pb-24 px-3.5 pt-3 max-w-md mx-auto">
      {/* Scope Switcher: Community vs My Reports */}
      <div className="flex rounded-xl bg-[#0B2418] p-1 border border-[#162F22]">
        <button
          onClick={() => setViewScope('community')}
          className={`flex-1 py-1.5 rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 transition-all ${
            viewScope === 'community'
              ? 'bg-[#213A2C] text-[#FFD56D] shadow-sm'
              : 'text-[#D1C5AF] hover:text-[#CDE9D6]'
          }`}
        >
          <Users className="w-3.5 h-3.5" />
          <span>Community Grid ({potholes.length})</span>
        </button>
        <button
          onClick={() => setViewScope('mine')}
          className={`flex-1 py-1.5 rounded-lg text-xs font-bold flex items-center justify-center gap-1.5 transition-all ${
            viewScope === 'mine'
              ? 'bg-[#213A2C] text-[#FFD56D] shadow-sm'
              : 'text-[#D1C5AF] hover:text-[#CDE9D6]'
          }`}
        >
          <UserCheck className="w-3.5 h-3.5" />
          <span>My Dispatches ({myReportedPotholes.length})</span>
        </button>
      </div>

      {dbError && (
        <div className="p-3 bg-amber-950/60 border border-amber-500/40 rounded-xl text-xs text-amber-200">
          {dbError}
        </div>
      )}

      {/* 1. Hazard Registry Analytics Bento Card */}
      <div className="rounded-2xl bg-[#162F22] border border-[#213A2C] p-3.5 space-y-3 shadow-xl">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BarChart3 className="w-4 h-4 text-[#FFD56D]" />
            <span className="text-sm font-black text-[#CDE9D6]">
              {viewScope === 'community' ? 'Regional Hazard Registry' : 'My Dispatches'}
            </span>
          </div>
          <div className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#254F3A] text-[#A4D1B6] text-[9px] font-black tracking-widest uppercase">
            <span className="w-1.5 h-1.5 rounded-full bg-[#A4D1B6] animate-pulse" />
            <span>CLOUD FIRESTORE</span>
          </div>
        </div>

        {/* 4-Grid Analytics Bento */}
        <div className="grid grid-cols-2 gap-2">
          {/* Total Reported */}
          <div className="rounded-xl bg-[#0B2418] p-2.5 space-y-0.5">
            <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">Total Reported</div>
            <div className="flex items-baseline gap-1">
              <span className="text-xl font-black text-[#CDE9D6]">{totalCount}</span>
              <span className="text-[10px] text-[#A4D1B6] font-semibold">cases</span>
            </div>
          </div>

          {/* Resolved */}
          <div className="rounded-xl bg-[#0B2418] p-2.5 space-y-0.5">
            <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">Resolved</div>
            <div className="flex items-baseline gap-1">
              <span className="text-xl font-black text-[#A4D1B6]">{resolvedCount}</span>
              <span className="text-[10px] text-[#A4D1B6]/70 font-semibold">
                {totalCount > 0 ? `${Math.round((resolvedCount / totalCount) * 100)}%` : '0%'}
              </span>
            </div>
          </div>

          {/* Municipal Review */}
          <div className="rounded-xl bg-[#0B2418] p-2.5 space-y-0.5">
            <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">Municipal Review</div>
            <div className="flex items-baseline gap-1">
              <span className="text-xl font-black text-[#FFD56D]">{progressCount}</span>
              <span className="text-[10px] text-[#D1C5AF] font-semibold">active</span>
            </div>
          </div>

          {/* Critical Risk */}
          <div className="rounded-xl bg-[#0B2418] p-2.5 space-y-0.5">
            <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">Critical Risk</div>
            <div className="flex items-baseline gap-1">
              <span className="text-xl font-black text-[#FFB4AB]">{criticalCount}</span>
              <span className="text-[10px] text-[#FFB4AB]/80 font-semibold">urgent</span>
            </div>
          </div>
        </div>
      </div>

      {/* 2. Search & Filter Bar */}
      <div className="flex items-center gap-2">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#D1C5AF]" />
          <input
            type="text"
            value={historySearchQuery}
            onChange={e => setHistorySearchQuery(e.target.value)}
            placeholder="Search avenue, ticket ID, or landmark..."
            className="w-full text-xs text-[#CDE9D6] bg-[#0B2418] border border-[#162F22] rounded-xl pl-9 pr-3 py-2.5 placeholder:text-[#9A907C] focus:outline-none focus:border-[#FFD56D]/50"
          />
        </div>
        <button
          onClick={() => setHistorySearchQuery('')}
          className="w-10 h-10 rounded-xl bg-[#213A2C] flex items-center justify-center text-[#D1C5AF] hover:bg-[#263E30] transition-colors shrink-0"
          title="Reset"
        >
          <SlidersHorizontal className="w-4 h-4" />
        </button>
      </div>

      {/* 3. Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
        {filterTabs.map(({ key, label }) => {
          const isSelected = historyFilterTab === key;
          return (
            <button
              key={key}
              onClick={() => setHistoryFilterTab(key)}
              className={`px-3 py-1.5 rounded-full text-xs font-bold whitespace-nowrap transition-all ${
                isSelected
                  ? 'bg-[#FFD56D] text-[#3E2E00] shadow-sm'
                  : 'bg-[#0B2418] text-[#D1C5AF] hover:bg-[#162F22]'
              }`}
            >
              {label}
            </button>
          );
        })}
      </div>

      {/* Loading State */}
      {isLoadingPotholes && (
        <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-8 text-center space-y-2">
          <Loader2 className="w-6 h-6 animate-spin text-[#FFD56D] mx-auto" />
          <div className="text-xs text-[#A4D1B6] font-bold">Synchronizing with Firestore Database...</div>
        </div>
      )}

      {/* Empty State */}
      {!isLoadingPotholes && filteredList.length === 0 && (
        <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-8 text-center space-y-3 shadow-lg">
          <div className="w-12 h-12 rounded-full bg-[#213A2C] text-[#FFD56D] flex items-center justify-center mx-auto">
            <ShieldAlert className="w-6 h-6" />
          </div>
          <div className="text-sm font-black text-[#CDE9D6]">
            {viewScope === 'mine' ? 'No Dispatches Found Under Your Account' : 'No Matching Hazards in Registry'}
          </div>
          <p className="text-xs text-[#D1C5AF] max-w-xs mx-auto leading-relaxed">
            {viewScope === 'mine'
              ? 'You have not reported any hazards yet. Switch to Community Grid or use the Camera / Report tab to log a road cavity.'
              : 'Try clearing your search query or switching to another filter category.'}
          </p>
          <div className="flex items-center justify-center gap-2 pt-1">
            {viewScope === 'mine' && (
              <button
                onClick={() => setViewScope('community')}
                className="px-3.5 py-1.5 rounded-xl bg-[#213A2C] text-[#CDE9D6] font-bold text-xs hover:bg-[#2C4A38]"
              >
                View Community Grid
              </button>
            )}
            <button
              onClick={() => setTab('REPORT')}
              className="px-4 py-1.5 rounded-xl bg-[#FFD56D] text-[#3E2E00] font-black text-xs hover:bg-[#EEC14A] transition-colors"
            >
              Report First Hazard
            </button>
          </div>
        </div>
      )}

      {/* 4. Hazard Cards List */}
      <div className="space-y-3.5">
        {filteredList.map((pothole: Pothole) => {
          const dispatch = GeoMatchingService.routeComplaint(
            pothole.city,
            pothole.address,
            pothole.lat,
            pothole.lon,
            pothole.severity,
            pothole.notes
          );

          const isRepaired = pothole.status === 'VERIFIED_FIXED';
          const isCritical = pothole.severity === 'SEVERE';
          const isOwner = currentUser && (pothole.userId === currentUser.userId || pothole.userId === 'system-sentinel');

          return (
            <div
              key={pothole.id}
              className="rounded-2xl bg-[#162F22] border border-[#213A2C] overflow-hidden shadow-xl"
            >
              {/* Photo Header with Badges */}
              <div className="relative w-full h-40 bg-[#001208] overflow-hidden">
                <img
                  src={pothole.imageUrl || '/sample_severe_pothole.png'}
                  alt={pothole.address}
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-[#162F22] via-transparent to-black/60 pointer-events-none" />

                {/* Top Left Severity Badge */}
                <div
                  className={`absolute top-2.5 left-2.5 flex items-center gap-1 px-2.5 py-1 rounded-full text-[10px] font-black shadow-md ${
                    isRepaired
                      ? 'bg-[#254F3A] text-[#BFEDD1]'
                      : isCritical
                      ? 'bg-[#93000A] text-white'
                      : 'bg-[#FFD56D] text-[#3E2E00]'
                  }`}
                >
                  {isRepaired ? (
                    <CheckCircle2 className="w-3.5 h-3.5" />
                  ) : (
                    <ShieldAlert className="w-3.5 h-3.5" />
                  )}
                  <span>{isRepaired ? 'REPAIRED • VERIFIED' : SEVERITY_INFO[pothole.severity]?.levelText || pothole.severity}</span>
                </div>

                {/* Top Right Actions: Ticket ID & Delete */}
                <div className="absolute top-2.5 right-2.5 flex items-center gap-1.5">
                  <div className="px-2 py-0.5 rounded-md bg-[#001208]/90 text-[#FFD56D] text-[11px] font-mono font-black border border-[#FFD56D]/30">
                    {pothole.ticketNumber}
                  </div>
                  {isOwner && (
                    <button
                      onClick={() => setDeleteConfirmId(pothole.id)}
                      className="w-6 h-6 rounded-md bg-red-950/80 border border-red-500/40 text-red-300 flex items-center justify-center hover:bg-red-900 transition-colors"
                      title="Delete this report"
                    >
                      <Trash2 className="w-3 h-3" />
                    </button>
                  )}
                </div>

                {/* Bottom Photo Overlay Info */}
                <div className="absolute bottom-2 inset-x-3 flex items-center justify-between text-white">
                  <div className="text-sm font-black text-[#CDE9D6] drop-shadow-md truncate max-w-[240px]">
                    {pothole.address}
                  </div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-[#001208]/85 text-[#FFD56D] border border-[#162F22]">
                    {pothole.distanceDisplay || 'Nearby'}
                  </span>
                </div>
              </div>

              {/* Confirm Delete Dialog Overlay */}
              {deleteConfirmId === pothole.id && (
                <div className="p-3 bg-red-950/90 border-b border-red-500/30 text-xs flex items-center justify-between gap-2">
                  <span className="text-red-200">Permanently delete report {pothole.ticketNumber}?</span>
                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => handleDelete(pothole.id)}
                      className="px-2.5 py-1 rounded bg-red-600 text-white font-bold text-[10px]"
                    >
                      Delete
                    </button>
                    <button
                      onClick={() => setDeleteConfirmId(null)}
                      className="px-2.5 py-1 rounded bg-[#213A2C] text-[#CDE9D6] text-[10px]"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              )}

              {/* Card Body Details */}
              <div className="p-3.5 space-y-3">
                {/* Lane Info & Date */}
                <div className="flex items-center justify-between text-xs text-[#D1C5AF]">
                  <div className="flex items-center gap-1.5">
                    <Compass className="w-3.5 h-3.5 text-[#A4D1B6]" />
                    <span>{pothole.laneInfo || 'Center Lane'}</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Clock className="w-3.5 h-3.5" />
                    <span>{pothole.reportedAt}</span>
                  </div>
                </div>

                {/* Status bar */}
                <div className="flex items-center gap-2 p-2.5 rounded-xl bg-[#0B2418] border border-[#213A2C]">
                  {isRepaired ? (
                    <CheckCircle2 className="w-4 h-4 text-[#A4D1B6] shrink-0" />
                  ) : pothole.status === 'IN_PROGRESS' ? (
                    <Wrench className="w-4 h-4 text-[#A4D1B6] shrink-0" />
                  ) : (
                    <span className="w-2.5 h-2.5 rounded-full bg-[#FFD56D] shrink-0 animate-pulse" />
                  )}
                  <span
                    className={`text-xs font-bold ${
                      isRepaired ? 'text-[#A4D1B6]' : pothole.status === 'IN_PROGRESS' ? 'text-[#A4D1B6]' : 'text-[#FFD56D]'
                    }`}
                  >
                    {pothole.statusNote}
                  </span>
                </div>

                {/* Notes if present */}
                {pothole.notes && (
                  <p className="text-xs text-[#CDE9D6]/90 italic bg-[#001208]/40 p-2 rounded-lg border border-[#162F22]">
                    "{pothole.notes}"
                  </p>
                )}

                {/* Actions & Upvote Row */}
                <div className="flex items-center justify-between pt-1 border-t border-[#213A2C]">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => upvoteHazard(pothole.id)}
                      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-extrabold transition-colors ${
                        isRepaired
                          ? 'bg-[#254F3A] text-[#BFEDD1]'
                          : 'bg-[#0B2418] text-[#A4D1B6] hover:bg-[#213A2C]'
                      }`}
                      title="+1 Still There confirmation"
                    >
                      <ThumbsUp className="w-3.5 h-3.5" />
                      <span>{pothole.confirmationCount}</span>
                    </button>
                    <span className="text-[11px] text-[#D1C5AF]">riders verified</span>
                  </div>

                  {/* Secondary buttons */}
                  <div className="flex items-center gap-1.5">
                    {!isRepaired && (
                      <button
                        onClick={() => confirmHazardFixed(pothole.id)}
                        className="px-2.5 py-1.5 rounded-lg bg-[#254F3A] text-[#BFEDD1] text-[11px] font-bold hover:bg-[#254F3A]/80 transition-colors"
                        title="Confirm pothole has been patched"
                      >
                        Confirm Fixed
                      </button>
                    )}

                    <button
                      onClick={() => setTab('SAFE_ROUTE')}
                      className={`flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                        pothole.status === 'ACTIVE'
                          ? 'bg-[#FFD56D] text-[#3E2E00] hover:bg-[#EEC14A]'
                          : 'bg-[#213A2C] text-[#CDE9D6] hover:bg-[#263E30]'
                      }`}
                    >
                      <Map className="w-3.5 h-3.5" />
                      <span>Route Map</span>
                    </button>
                  </div>
                </div>

                {/* Dispatch to Municipal Authority button */}
                <div className="pt-1">
                  <a
                    href={dispatch.actionUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center justify-center gap-1.5 w-full py-1.5 rounded-lg bg-[#001208] border border-[#FFD56D]/30 text-[#FFD56D] text-[11px] font-bold hover:bg-[#0B2418] transition-colors"
                  >
                    <span>Send Notice to {dispatch.authorityName}</span>
                    <ExternalLink className="w-3 h-3" />
                  </a>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* 5. Bottom Prompt */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-5 text-center space-y-2 shadow-lg">
        <div className="w-10 h-10 rounded-full bg-[#162F22] text-[#FFD56D] flex items-center justify-center mx-auto">
          <PlusCircle className="w-5 h-5" />
        </div>
        <div className="text-sm font-black text-[#CDE9D6]">Encountered an Unmapped Hazard?</div>
        <p className="text-xs text-[#D1C5AF] leading-relaxed">
          RideVision HUD computer vision tags impact shocks automatically, but manual reporting alerts the city maintenance squad immediately.
        </p>
        <button
          onClick={() => setTab('REPORT')}
          className="mt-2 px-4 py-2.5 rounded-xl bg-[#FFD56D] text-[#3E2E00] font-black text-xs hover:bg-[#EEC14A] transition-colors inline-flex items-center gap-1.5"
        >
          <span>Log New Road Hazard</span>
        </button>
      </div>
    </div>
  );
};
