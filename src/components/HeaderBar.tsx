import React from 'react';
import { Radar, Gauge, User } from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';

export const HeaderBar: React.FC = () => {
  const { setTab, userProfile } = useRideVision();

  return (
    <header className="sticky top-0 z-40 w-full bg-[#001208]/95 backdrop-blur-md border-b border-[#162F22] px-4 py-3">
      <div className="max-w-md mx-auto flex items-center justify-between">
        {/* Left brand cluster */}
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 rounded-xl bg-[#162F22]/80 border border-[#FFD56D]/30 flex items-center justify-center shadow-inner">
            <Radar className="w-5 h-5 text-[#FFD56D]" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <h1 className="text-base font-black tracking-tight text-[#FFD56D] leading-none">
                RideVision
              </h1>
            </div>
            <div className="flex items-center gap-1.5 mt-0.5">
              <span className="w-1.5 h-1.5 rounded-full bg-[#A4D1B6] animate-pulse" />
              <span className="text-[10px] font-bold text-[#A4D1B6] tracking-wider uppercase">
                HUD ACTIVE
              </span>
            </div>
          </div>
        </div>

        {/* Right status badges */}
        <div className="flex items-center gap-2">
          {/* Profile Avatar button */}
          <button
            onClick={() => setTab('PROFILE')}
            className="w-8 h-8 rounded-full bg-[#162F22] border border-[#FFD56D]/50 flex items-center justify-center hover:border-[#FFD56D] transition-colors focus:outline-none"
            title="Sentinel Profile"
            aria-label="Profile"
          >
            {userProfile?.photoUrl ? (
              <img
                src={userProfile.photoUrl}
                alt={userProfile.displayName}
                className="w-full h-full rounded-full object-cover"
              />
            ) : (
              <User className="w-4 h-4 text-[#FFD56D]" />
            )}
          </button>
        </div>
      </div>
    </header>
  );
};
