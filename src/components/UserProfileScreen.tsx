import React, { useState } from 'react';
import {
  User,
  CheckCircle,
  Award,
  Cloud,
  Headphones,
  Vibrate,
  Bike,
  Edit2,
  X,
  Save,
  LogOut,
  Shield,
  Phone,
  Home,
  AlertCircle
} from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';

export const UserProfileScreen: React.FC = () => {
  const {
    userProfile,
    updateUserProfileVehicleDetails,
    toggleEarbudAudio,
    toggleHandlebarHaptics,
    signOut
  } = useRideVision();

  const [isEditing, setIsEditing] = useState(false);
  const [vehicleModel, setVehicleModel] = useState(userProfile.vehicleModel);
  const [vehicleSpec, setVehicleSpec] = useState(userProfile.vehicleSpec);
  const [residentialBase, setResidentialBase] = useState(userProfile.residentialBase);
  const [directLine, setDirectLine] = useState(userProfile.directLine);
  const [emergencyIce, setEmergencyIce] = useState(userProfile.emergencyIce);

  const handleSave = () => {
    updateUserProfileVehicleDetails({
      vehicleModel,
      vehicleSpec,
      residentialBase,
      directLine,
      emergencyIce
    });
    setIsEditing(false);
  };

  return (
    <div className="flex flex-col gap-3.5 pb-24 px-3.5 pt-3 max-w-md mx-auto">
      {/* 1. Hero Cockpit Profile Card */}
      <div className="rounded-2xl bg-[#162F22] border border-[#213A2C] p-4 space-y-3.5 shadow-xl">
        <div className="flex items-center gap-3.5">
          {/* Avatar with Verified Badge */}
          <div className="relative">
            <div className="w-16 h-16 rounded-2xl bg-[#213A2C] border border-[#FFD56D]/40 flex items-center justify-center text-[#FFD56D] overflow-hidden">
              {userProfile.photoUrl ? (
                <img
                  src={userProfile.photoUrl}
                  alt={userProfile.displayName}
                  className="w-full h-full object-cover"
                />
              ) : (
                <User className="w-8 h-8" />
              )}
            </div>
            <div className="absolute -bottom-1 -right-1 w-5 h-5 rounded-full bg-[#FFD56D] text-[#3E2E00] flex items-center justify-center border-2 border-[#162F22] shadow">
              <CheckCircle className="w-3.5 h-3.5" />
            </div>
          </div>

          {/* Name & Account Details */}
          <div className="space-y-0.5 flex-1 min-w-0">
            <div className="text-[10px] font-black tracking-widest text-[#FFD56D] font-mono">
              {userProfile.riderId}
            </div>
            <h2 className="text-lg font-black text-[#CDE9D6] truncate">
              {userProfile.displayName}
            </h2>
            <div className="text-xs text-[#D1C5AF] truncate">{userProfile.email}</div>
            <div className="flex items-center gap-1.5 pt-0.5">
              <span className="w-1.5 h-1.5 rounded-full bg-[#A4D1B6]" />
              <span className="text-[10px] font-bold text-[#A4D1B6]">
                Authenticated Road Sentinel
              </span>
            </div>
          </div>
        </div>

        {/* Status Tier Sub-card */}
        <div className="rounded-xl bg-[#001208]/90 border border-[#213A2C] p-3 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-[#E5B842]/20 text-[#FFD56D] flex items-center justify-center">
              <Award className="w-4 h-4" />
            </div>
            <div>
              <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">
                Rider Sentinel Status
              </div>
              <div className="text-sm font-black text-[#FFD56D]">{userProfile.safetyTier}</div>
            </div>
          </div>
          <span className="px-2.5 py-1 rounded-full bg-[#254F3A] text-[#BFEDD1] text-[10px] font-black tracking-wider">
            {userProfile.tierSubtitle}
          </span>
        </div>
      </div>

      {/* 2. Metrics & Telemetry Triad */}
      <div className="grid grid-cols-3 gap-2.5">
        <div className="rounded-xl bg-[#0B2418] border border-[#162F22] p-3 text-center space-y-1">
          <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">Hazards Logged</div>
          <div className="text-xl font-black text-[#FFD56D]">{userProfile.verifiedCount}</div>
          <div className="text-[9px] text-[#A4D1B6]">Synced to Cloud</div>
        </div>

        <div className="rounded-xl bg-[#0B2418] border border-[#162F22] p-3 text-center space-y-1">
          <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">AI Precision</div>
          <div className="text-xl font-black text-[#CDE9D6]">{userProfile.precisionScore}</div>
          <div className="text-[9px] text-[#D1C5AF]">Optical Scans</div>
        </div>

        <div className="rounded-xl bg-[#0B2418] border border-[#162F22] p-3 text-center space-y-1">
          <div className="text-[9px] font-bold text-[#D1C5AF] uppercase">Corridor Patrol</div>
          <div className="text-xl font-black text-[#CDE9D6]">{userProfile.milesCovered}</div>
          <div className="text-[9px] text-[#D1C5AF]">GPS Tracked</div>
        </div>
      </div>

      {/* 3. Cloud Database Connection Status Card */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-3.5 space-y-1.5 shadow-lg">
        <div className="flex items-center gap-2">
          <Cloud className="w-4 h-4 text-[#A4D1B6]" />
          <span className="text-[11px] font-black text-[#A4D1B6] tracking-wider uppercase">
            Cloud Firestore Active
          </span>
        </div>
        <p className="text-xs text-[#D1C5AF] leading-relaxed">
          All reported road fissures, hazard votes, and repair confirmations sync in real time to the decentralized Mangaluru municipal registry.
        </p>
      </div>

      {/* 4. Commuter Telemetry Link Preferences */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-3.5 space-y-3 shadow-lg">
        <div className="flex items-center gap-2">
          <Shield className="w-4 h-4 text-[#FFD56D]" />
          <span className="text-[11px] font-black text-[#FFD56D] tracking-wider uppercase">
            Commuter Telemetry Link
          </span>
        </div>

        {/* Earbud Audio Chime */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Headphones className="w-4 h-4 text-[#D1C5AF]" />
            <div>
              <div className="text-xs font-bold text-[#CDE9D6]">Earbud Chime Ahead</div>
              <div className="text-[11px] text-[#D1C5AF]">Audio warning on 250m cone</div>
            </div>
          </div>
          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={userProfile.earbudAudioPing}
              onChange={e => toggleEarbudAudio(e.target.checked)}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-[#162F22] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#001208] after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#FFD56D] peer-checked:after:bg-[#3E2E00]" />
          </label>
        </div>

        {/* Handlebar Haptic Pulse */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Vibrate className="w-4 h-4 text-[#D1C5AF]" />
            <div>
              <div className="text-xs font-bold text-[#CDE9D6]">Handlebar Haptic Pulse</div>
              <div className="text-[11px] text-[#D1C5AF]">Vibrate phone on severe craters</div>
            </div>
          </div>
          <label className="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              checked={userProfile.handlebarHapticPulse}
              onChange={e => toggleHandlebarHaptics(e.target.checked)}
              className="sr-only peer"
            />
            <div className="w-11 h-6 bg-[#162F22] peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-[#001208] after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#FFD56D] peer-checked:after:bg-[#3E2E00]" />
          </label>
        </div>
      </div>

      {/* 5. Commuter Specification & Base Card (User Editable) */}
      <div className="rounded-2xl bg-[#0B2418] border border-[#162F22] p-3.5 space-y-3 shadow-lg">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Bike className="w-4 h-4 text-[#FFD56D]" />
            <span className="text-[11px] font-black text-[#FFD56D] tracking-wider uppercase">
              Commuter Vehicle & Base
            </span>
          </div>
          <button
            onClick={() => setIsEditing(!isEditing)}
            className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[#162F22] text-[#FFD56D] text-[11px] font-bold border border-[#FFD56D]/30 hover:bg-[#213A2C] transition-colors"
          >
            {isEditing ? <X className="w-3 h-3" /> : <Edit2 className="w-3 h-3" />}
            <span>{isEditing ? 'Cancel' : 'Edit Details'}</span>
          </button>
        </div>

        {!isEditing ? (
          <div className="space-y-2 text-xs divide-y divide-[#162F22]">
            <div className="flex justify-between py-1">
              <span className="text-[#D1C5AF]">Registered Ride</span>
              <span className="font-bold text-[#CDE9D6]">{userProfile.vehicleModel}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-[#D1C5AF]">Vehicle Specs</span>
              <span className="font-bold text-[#CDE9D6]">{userProfile.vehicleSpec}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-[#D1C5AF]">Home Sector</span>
              <span className="font-bold text-[#CDE9D6]">{userProfile.residentialBase}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-[#D1C5AF]">Direct Line</span>
              <span className="font-bold text-[#CDE9D6]">{userProfile.directLine}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-[#D1C5AF]">Emergency ICE</span>
              <span className="font-bold text-[#CDE9D6]">{userProfile.emergencyIce}</span>
            </div>
          </div>
        ) : (
          <div className="space-y-2.5">
            <div>
              <label className="text-[10px] font-bold text-[#D1C5AF]">Registered Vehicle Model</label>
              <input
                type="text"
                value={vehicleModel}
                onChange={e => setVehicleModel(e.target.value)}
                className="w-full text-xs text-[#CDE9D6] bg-[#001208] border border-[#162F22] rounded-lg p-2 mt-0.5 focus:outline-none focus:border-[#FFD56D]"
              />
            </div>
            <div>
              <label className="text-[10px] font-bold text-[#D1C5AF]">Vehicle Specs</label>
              <input
                type="text"
                value={vehicleSpec}
                onChange={e => setVehicleSpec(e.target.value)}
                className="w-full text-xs text-[#CDE9D6] bg-[#001208] border border-[#162F22] rounded-lg p-2 mt-0.5 focus:outline-none focus:border-[#FFD56D]"
              />
            </div>
            <div>
              <label className="text-[10px] font-bold text-[#D1C5AF]">Residential Base</label>
              <input
                type="text"
                value={residentialBase}
                onChange={e => setResidentialBase(e.target.value)}
                className="w-full text-xs text-[#CDE9D6] bg-[#001208] border border-[#162F22] rounded-lg p-2 mt-0.5 focus:outline-none focus:border-[#FFD56D]"
              />
            </div>
            <div>
              <label className="text-[10px] font-bold text-[#D1C5AF]">Direct Mobile Line</label>
              <input
                type="text"
                value={directLine}
                onChange={e => setDirectLine(e.target.value)}
                className="w-full text-xs text-[#CDE9D6] bg-[#001208] border border-[#162F22] rounded-lg p-2 mt-0.5 focus:outline-none focus:border-[#FFD56D]"
              />
            </div>
            <div>
              <label className="text-[10px] font-bold text-[#D1C5AF]">Emergency Contact (ICE)</label>
              <input
                type="text"
                value={emergencyIce}
                onChange={e => setEmergencyIce(e.target.value)}
                className="w-full text-xs text-[#CDE9D6] bg-[#001208] border border-[#162F22] rounded-lg p-2 mt-0.5 focus:outline-none focus:border-[#FFD56D]"
              />
            </div>

            <button
              onClick={handleSave}
              className="w-full h-10 rounded-xl bg-[#FFD56D] text-[#3E2E00] font-black text-xs flex items-center justify-center gap-1.5 hover:bg-[#EEC14A] transition-colors"
            >
              <Save className="w-4 h-4" />
              <span>Save Vehicle & Base Details</span>
            </button>
          </div>
        )}
      </div>

      {/* 6. Sign Out Button */}
      <button
        onClick={signOut}
        className="w-full h-11 rounded-xl bg-[#162F22] border border-[#93000A]/40 text-[#FF5252] font-black text-xs flex items-center justify-center gap-2 hover:bg-[#93000A]/20 transition-colors"
      >
        <LogOut className="w-4 h-4" />
        <span>Sign Out of RideVision</span>
      </button>
    </div>
  );
};
