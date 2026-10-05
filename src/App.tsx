import React from 'react';
import { useRideVision, RideVisionProvider } from './context/RideVisionContext';
import { HeaderBar } from './components/HeaderBar';
import { BottomNavBar } from './components/BottomNavBar';
import { ReportPotholeScreen } from './components/ReportPotholeScreen';
import { SafeRouteScreen } from './components/SafeRouteScreen';
import { ComplaintHistoryScreen } from './components/ComplaintHistoryScreen';
import { UserProfileScreen } from './components/UserProfileScreen';
import { AuthScreen } from './components/AuthScreen';

const MainAppContent: React.FC = () => {
  const { currentUser, currentTab, toastMessage } = useRideVision();

  if (!currentUser) {
    return <AuthScreen />;
  }

  return (
    <div className="min-h-screen bg-[#01180C] text-[#CDE9D6] flex flex-col justify-between selection:bg-[#FFD56D] selection:text-[#3E2E00]">
      <HeaderBar />

      <main className="flex-1 w-full max-w-md mx-auto">
        {currentTab === 'REPORT' && <ReportPotholeScreen />}
        {currentTab === 'SAFE_ROUTE' && <SafeRouteScreen />}
        {currentTab === 'HISTORY' && <ComplaintHistoryScreen />}
        {currentTab === 'PROFILE' && <UserProfileScreen />}
      </main>

      <BottomNavBar />

      {/* Floating Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-20 left-1/2 -translate-x-1/2 z-50 px-4 py-2 rounded-xl bg-[#162F22]/95 border border-[#FFD56D]/40 text-[#FFD56D] text-xs font-bold shadow-2xl backdrop-blur-md animate-in fade-in slide-in-from-bottom-2">
          {toastMessage}
        </div>
      )}
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <RideVisionProvider>
      <MainAppContent />
    </RideVisionProvider>
  );
};

export default App;
