import React, { useState } from 'react';
import { Copy, Check, Heart, ShieldAlert, ExternalLink, Wallet } from 'lucide-react';
import { SUPPORTED_CRYPTO_TOKENS, DEVELOPER_WALLET_ADDRESS } from '../data/pdflyData';

export const SupportDonationSection: React.FC = () => {
  const [selectedSymbol, setSelectedSymbol] = useState<'USDT' | 'USDC'>('USDT');
  const [copiedField, setCopiedField] = useState<'wallet' | 'contract' | null>(null);

  const activeToken =
    SUPPORTED_CRYPTO_TOKENS.find((t) => t.symbol === selectedSymbol) || SUPPORTED_CRYPTO_TOKENS[0];

  const handleCopy = async (value: string, field: 'wallet' | 'contract') => {
    try {
      await navigator.clipboard.writeText(value);
      setCopiedField(field);
      setTimeout(() => setCopiedField(null), 2200);
    } catch {
      // Fallback for restricted clipboard environments
      const textarea = document.createElement('textarea');
      textarea.value = value;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      setCopiedField(field);
      setTimeout(() => setCopiedField(null), 2200);
    }
  };

  return (
    <section id="support" className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div className="rounded-3xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 shadow-xl overflow-hidden">
        <div className="p-6 sm:p-10 lg:p-12 grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
          {/* Left Column: Voluntary Open-Source Philosophy */}
          <div className="lg:col-span-7 space-y-6">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
              <Heart className="w-3.5 h-3.5 fill-current" />
              <span>100% VOLUNTARY OPEN-SOURCE SUPPORT • ZERO PAYWALLS</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-display font-bold text-slate-900 dark:text-white tracking-tight">
              Support PDFly Development
            </h2>

            <p className="text-slate-700 dark:text-slate-200 text-base leading-relaxed">
              Every single PDF tool in PDFly is <strong>100% free forever</strong> with no subscriptions, no locked
              features, no advertisements, and no tracking. If PDFly saves you time or helps your daily workflow, you
              can optionally support ongoing open-source maintenance via{' '}
              <strong>USDT (BEP-20)</strong> or <strong>USDC (BEP-20)</strong> on{' '}
              <strong>BNB Smart Chain (Chain ID 56)</strong>.
            </p>

            {/* Important Network Notice */}
            <div className="rounded-2xl bg-amber-50 dark:bg-amber-950/50 border border-amber-300 dark:border-amber-700/80 p-4 flex items-start gap-3.5">
              <ShieldAlert className="w-5 h-5 text-amber-700 dark:text-amber-400 shrink-0 mt-0.5" />
              <div className="text-xs sm:text-sm text-amber-950 dark:text-amber-100 space-y-1">
                <div className="font-bold">Network Verification: BNB Smart Chain (BEP20 • Chain ID 56)</div>
                <p className="text-amber-800 dark:text-amber-200/90">
                  Please ensure your wallet or exchange is set to <strong>BNB Smart Chain (BEP20)</strong> before
                  sending USDT or USDC.
                </p>
              </div>
            </div>

            {/* Token Selector Tabs */}
            <div className="space-y-3">
              <div className="text-xs font-bold uppercase tracking-wider text-slate-600 dark:text-slate-300">
                Select Supported BEP-20 Token
              </div>
              <div className="grid grid-cols-2 gap-3 max-w-md">
                {SUPPORTED_CRYPTO_TOKENS.map((token) => {
                  const isSelected = token.symbol === selectedSymbol;
                  return (
                    <button
                      key={token.symbol}
                      type="button"
                      onClick={() => setSelectedSymbol(token.symbol)}
                      className={`flex items-center justify-between px-4 py-3 rounded-xl border text-left transition-all ${
                        isSelected
                          ? 'bg-slate-900 dark:bg-crimson-700 text-white border-slate-900 dark:border-crimson-500 shadow-md'
                          : 'bg-slate-50 dark:bg-slate-800/80 text-slate-800 dark:text-slate-200 border-slate-300 dark:border-slate-700 hover:border-slate-400'
                      }`}
                    >
                      <div>
                        <div className="text-sm font-bold">{token.symbol}</div>
                        <div className={`text-[11px] ${isSelected ? 'text-slate-200' : 'text-slate-500 dark:text-slate-400'}`}>
                          {token.tokenStandard} • BSC
                        </div>
                      </div>
                      <span
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: token.badgeColor }}
                        aria-hidden="true"
                      />
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Wallet Address & Copy Control */}
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs font-bold text-slate-700 dark:text-slate-200">
                <span className="inline-flex items-center gap-1.5">
                  <Wallet className="w-3.5 h-3.5 text-crimson-600 dark:text-crimson-400" />
                  Receiving Wallet Address ({activeToken.symbol} • {activeToken.networkName})
                </span>
                <span className="font-mono text-emerald-700 dark:text-emerald-400">Chain ID: {activeToken.chainId}</span>
              </div>
              <div className="flex flex-col sm:flex-row gap-2.5">
                <div className="flex-1 rounded-xl bg-slate-100 dark:bg-[#0B1120] border border-slate-300 dark:border-slate-700 px-3.5 py-3 font-mono text-xs sm:text-sm text-slate-900 dark:text-emerald-300 break-all select-all">
                  {DEVELOPER_WALLET_ADDRESS}
                </div>
                <button
                  type="button"
                  onClick={() => handleCopy(DEVELOPER_WALLET_ADDRESS, 'wallet')}
                  className="inline-flex items-center justify-center gap-2 px-5 py-3 rounded-xl font-bold text-xs sm:text-sm bg-crimson-700 hover:bg-crimson-800 text-white transition-colors shrink-0 shadow-sm"
                >
                  {copiedField === 'wallet' ? (
                    <>
                      <Check className="w-4 h-4" />
                      <span>Address Copied</span>
                    </>
                  ) : (
                    <>
                      <Copy className="w-4 h-4" />
                      <span>Copy Wallet Address</span>
                    </>
                  )}
                </button>
              </div>
            </div>

            {/* Verified Token Contract Reference */}
            <div className="rounded-xl bg-slate-50 dark:bg-slate-900/70 border border-slate-200 dark:border-slate-800 p-3.5 flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-xs">
              <div className="space-y-0.5">
                <div className="font-bold text-slate-700 dark:text-slate-300">
                  Official {activeToken.symbol} ({activeToken.tokenStandard}) Contract on BSC:
                </div>
                <div className="font-mono text-slate-600 dark:text-slate-400 break-all">
                  {activeToken.contractAddress}
                </div>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <button
                  type="button"
                  onClick={() => handleCopy(activeToken.contractAddress, 'contract')}
                  className="px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-200 font-semibold hover:border-crimson-500 transition-colors"
                >
                  {copiedField === 'contract' ? 'Contract Copied' : 'Copy Contract'}
                </button>
                <a
                  href={activeToken.explorerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-200 font-semibold hover:border-crimson-500 transition-colors"
                >
                  <span>BscScan</span>
                  <ExternalLink className="w-3.5 h-3.5" />
                </a>
              </div>
            </div>
          </div>

          {/* Right Column: Scannable Wallet QR Card */}
          <div className="lg:col-span-5 flex flex-col items-center">
            <div className="w-full max-w-sm rounded-3xl bg-slate-900 dark:bg-[#0B1120] border border-slate-700 p-6 text-center text-white shadow-lg space-y-4">
              <div className="flex items-center justify-between text-xs">
                <span className="font-bold text-slate-300">{activeToken.fullName}</span>
                <span className="px-2.5 py-0.5 rounded-full font-mono font-bold bg-emerald-950 text-emerald-300 border border-emerald-700">
                  {activeToken.tokenStandard}
                </span>
              </div>

              {/* Stylized High-Contrast Wallet QR Code Preview */}
              <div className="bg-white p-5 rounded-2xl inline-block mx-auto shadow-inner">
                <svg
                  viewBox="0 0 200 200"
                  className="w-44 h-44"
                  role="img"
                  aria-label={`BNB Smart Chain wallet address ${DEVELOPER_WALLET_ADDRESS}`}
                >
                  <rect width="200" height="200" fill="#FFFFFF" />
                  {/* Top-Left Finder */}
                  <rect x="12" y="12" width="52" height="52" fill="#0F172A" />
                  <rect x="20" y="20" width="36" height="36" fill="#FFFFFF" />
                  <rect x="28" y="28" width="20" height="20" fill="#BE123C" />
                  {/* Top-Right Finder */}
                  <rect x="136" y="12" width="52" height="52" fill="#0F172A" />
                  <rect x="144" y="20" width="36" height="36" fill="#FFFFFF" />
                  <rect x="152" y="28" width="20" height="20" fill="#0F172A" />
                  {/* Bottom-Left Finder */}
                  <rect x="12" y="136" width="52" height="52" fill="#0F172A" />
                  <rect x="20" y="144" width="36" height="36" fill="#FFFFFF" />
                  <rect x="28" y="152" width="20" height="20" fill="#0F172A" />
                  {/* Data Modules */}
                  <rect x="76" y="16" width="12" height="12" fill="#0F172A" />
                  <rect x="100" y="16" width="20" height="12" fill="#0F172A" />
                  <rect x="76" y="40" width="24" height="12" fill="#0F172A" />
                  <rect x="112" y="40" width="12" height="24" fill="#0F172A" />
                  <rect x="16" y="76" width="24" height="12" fill="#0F172A" />
                  <rect x="52" y="76" width="24" height="16" fill="#0F172A" />
                  <rect x="88" y="88" width="24" height="24" rx="4" fill="#BE123C" />
                  <rect x="124" y="76" width="20" height="16" fill="#0F172A" />
                  <rect x="156" y="76" width="28" height="12" fill="#0F172A" />
                  <rect x="20" y="104" width="36" height="16" fill="#0F172A" />
                  <rect x="68" y="120" width="28" height="12" fill="#0F172A" />
                  <rect x="116" y="112" width="24" height="24" fill="#0F172A" />
                  <rect x="152" y="104" width="32" height="20" fill="#0F172A" />
                  <rect x="76" y="148" width="20" height="36" fill="#0F172A" />
                  <rect x="108" y="148" width="36" height="16" fill="#0F172A" />
                  <rect x="156" y="144" width="28" height="40" fill="#0F172A" />
                  <rect x="112" y="172" width="28" height="16" fill="#0F172A" />
                </svg>
              </div>

              <div className="space-y-1">
                <div className="text-xs font-semibold text-slate-300">
                  BNB Smart Chain (BEP20) Receiving Address
                </div>
                <div className="font-mono text-[11px] text-emerald-400 break-all px-2">
                  {DEVELOPER_WALLET_ADDRESS}
                </div>
              </div>

              <div className="pt-2 border-t border-slate-800 text-[11px] text-slate-400">
                Inside the Android app, PDFly renders an exact ISO/IEC 18004 QR matrix locally via{' '}
                <code className="text-slate-200">QrCodeGenerator.kt</code>.
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};
