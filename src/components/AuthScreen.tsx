import React, { useState } from 'react';
import { Radar, Cloud, Shield, Lock, ArrowRight, User, AlertCircle, CheckCircle } from 'lucide-react';
import { useRideVision } from '../context/RideVisionContext';

export const AuthScreen: React.FC = () => {
  const { signInDemo, signInWithGoogle } = useRideVision();
  const [name, setName] = useState('Rider Sentinel');
  const [email, setEmail] = useState('sentinel@ridevision.net');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMsg('Please enter a Sentinel Call-Sign');
      return;
    }
    if (!email.trim() || !email.includes('@')) {
      setErrorMsg('Please enter a valid email address');
      return;
    }

    setErrorMsg(null);
    setIsLoading(true);
    try {
      await signInDemo(name.trim(), email.trim());
    } catch (err: any) {
      setErrorMsg(err?.message || 'Authentication failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setErrorMsg(null);
    setIsLoading(true);
    try {
      await signInWithGoogle();
    } catch (err: any) {
      setErrorMsg(err?.message || 'Google Authentication failed');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#01180C] text-[#CDE9D6] flex flex-col justify-center items-center px-4 py-8 max-w-md mx-auto">
      <div className="w-full space-y-6">
        {/* Emblem Header */}
        <div className="flex flex-col items-center text-center space-y-2.5">
          <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-[#FFD56D] to-[#A4D1B6] p-1 flex items-center justify-center shadow-2xl shadow-[#FFD56D]/20">
            <div className="w-full h-full rounded-full bg-[#01180C] flex items-center justify-center">
              <Radar className="w-10 h-10 text-[#FFD56D]" />
            </div>
          </div>
          <div>
            <h1 className="text-3xl font-black tracking-tight text-[#CDE9D6]">RideVision</h1>
            <p className="text-xs font-bold text-[#FFD56D] mt-0.5">
              Cloud-Synchronized Road Sentinel Network
            </p>
          </div>
        </div>

        {/* Mission Value Card */}
        <div className="rounded-2xl bg-[#0B2418] border border-[#FFD56D]/20 p-4 space-y-3.5 shadow-xl">
          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-xl bg-[#213A2C] flex items-center justify-center text-[#FFD56D] shrink-0 mt-0.5">
              <Cloud className="w-5 h-5" />
            </div>
            <div>
              <div className="text-sm font-bold text-[#CDE9D6] flex items-center gap-1.5">
                <span>Cloud Firestore Persistence</span>
                <CheckCircle className="w-3.5 h-3.5 text-[#A4D1B6]" />
              </div>
              <div className="text-xs text-[#D1C5AF] mt-0.5">
                Real-time sync of verified road hazards across Mangaluru and Karnataka corridors
              </div>
            </div>
          </div>

          <div className="flex items-start gap-3">
            <div className="w-9 h-9 rounded-xl bg-[#213A2C] flex items-center justify-center text-[#A4D1B6] shrink-0 mt-0.5">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <div className="text-sm font-bold text-[#CDE9D6]">
                Reputation & Sentinel Stats
              </div>
              <div className="text-xs text-[#D1C5AF] mt-0.5">
                Log civic reports with authenticated identity, telemetry tracking, and civic dispatch
              </div>
            </div>
          </div>
        </div>

        {errorMsg && (
          <div className="p-3 bg-red-950/80 border border-red-500/40 rounded-xl text-xs text-red-200 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        {/* Google Quick Sign-In */}
        <button
          onClick={handleGoogleSignIn}
          disabled={isLoading}
          type="button"
          className="w-full h-12 rounded-xl bg-[#FFD56D] text-[#3E2E00] font-black text-sm flex items-center justify-center gap-2 hover:bg-[#EEC14A] active:scale-[0.99] transition-all shadow-xl shadow-[#FFD56D]/20 disabled:opacity-60"
        >
          {isLoading ? (
            <span className="text-xs">Authenticating Sentinel...</span>
          ) : (
            <>
              <svg className="w-4 h-4" viewBox="0 0 24 24">
                <path
                  fill="currentColor"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="currentColor"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="currentColor"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="currentColor"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <span>Continue with Google Account</span>
            </>
          )}
        </button>

        <div className="flex items-center gap-3 my-2">
          <div className="flex-1 h-px bg-[#162F22]" />
          <span className="text-[10px] font-bold text-[#A4D1B6] uppercase tracking-wider">or sign in with sentinel call-sign</span>
          <div className="flex-1 h-px bg-[#162F22]" />
        </div>

        {/* Sign In Form */}
        <form onSubmit={handleSignIn} className="space-y-3">
          <div className="space-y-1">
            <label className="text-[10px] font-bold text-[#A4D1B6] uppercase tracking-wider">
              Sentinel Call-Sign / Name
            </label>
            <div className="relative">
              <input
                type="text"
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="e.g. Rider Sentinel"
                required
                className="w-full text-xs text-[#CDE9D6] bg-[#0B2418] border border-[#162F22] rounded-xl px-3 py-2.5 focus:outline-none focus:border-[#FFD56D]"
              />
              <User className="absolute right-3 top-2.5 w-4 h-4 text-[#D1C5AF]" />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-bold text-[#A4D1B6] uppercase tracking-wider">
              Email Address
            </label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="sentinel@ridevision.net"
              required
              className="w-full text-xs text-[#CDE9D6] bg-[#0B2418] border border-[#162F22] rounded-xl px-3 py-2.5 focus:outline-none focus:border-[#FFD56D]"
            />
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full h-11 rounded-xl bg-[#213A2C] border border-[#FFD56D]/30 text-[#CDE9D6] font-bold text-xs flex items-center justify-center gap-2 hover:bg-[#2C4A38] active:scale-[0.99] transition-all disabled:opacity-60"
          >
            <Lock className="w-3.5 h-3.5 text-[#FFD56D]" />
            <span>Launch Sentinel Terminal</span>
            <ArrowRight className="w-3.5 h-3.5 ml-1 text-[#FFD56D]" />
          </button>
        </form>

        <p className="text-[11px] text-[#D1C5AF]/80 text-center">
          Secured with Firebase Auth & Zero-Trust Cloud Firestore
        </p>
      </div>
    </div>
  );
};
