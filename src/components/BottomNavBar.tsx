import React from 'react';
import { Camera, Route, History, User } from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';
import { AppTab } from '../types/models';

export const BottomNavBar: React.FC = () => {
  const { currentTab, setTab } = useRideVision();

  const tabs: { id: AppTab; label: string; icon: React.FC<{ className?: string }> }[] = [
    { id: 'REPORT', label: 'Report', icon: Camera },
    { id: 'SAFE_ROUTE', label: 'Safe Route', icon: Route },
    { id: 'HISTORY', label: 'History', icon: History },
    { id: 'PROFILE', label: 'Profile', icon: User }
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#001208]/95 backdrop-blur-md border-t border-[#162F22] py-1.5 px-4 shadow-2xl safe-area-pb">
      <div className="max-w-md mx-auto grid grid-cols-4 gap-1">
        {tabs.map(({ id, label, icon: IconComponent }) => {
          const isSelected = currentTab === id;
          return (
            <button
              key={id}
              onClick={() => setTab(id)}
              className="flex flex-col items-center justify-center py-1 rounded-xl transition-all duration-200 group focus:outline-none"
            >
              <div
                className={`flex items-center justify-center w-12 h-7 rounded-full transition-all duration-200 ${
                  isSelected
                    ? 'bg-[#FFD56D] text-[#3E2E00] shadow-md shadow-[#FFD56D]/20'
                    : 'text-[#D1C5AF] hover:text-[#CDE9D6] group-hover:bg-[#162F22]/40'
                }`}
              >
                <IconComponent className="w-5 h-5" />
              </div>
              <span
                className={`text-[11px] font-semibold mt-1 transition-colors ${
                  isSelected ? 'text-[#FFD56D]' : 'text-[#D1C5AF]'
                }`}
              >
                {label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
