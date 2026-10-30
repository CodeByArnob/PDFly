import React, { useState } from 'react';
import { motion, AnimatePresence, useReducedMotion } from 'framer-motion';
import {
  Layers,
  Scissors,
  Minimize2,
  Camera,
  PenTool,
  LayoutGrid,
  CheckCircle2,
  ArrowRight,
  FileText,
  Sparkles,
  ShieldCheck
} from 'lucide-react';

type SimMode = 'merge' | 'split' | 'compress' | 'scanner' | 'annotate' | 'organize';

interface ModeInfo {
  id: SimMode;
  label: string;
  badge: string;
  headline: string;
  description: string;
  engineCall: string;
  metrics: { label: string; value: string }[];
}

const SIM_MODES: ModeInfo[] = [
  {
    id: 'merge',
    label: 'Merge PDF',
    badge: 'Multi-Document Combine',
    headline: 'Combine multiple PDFs in custom order while preserving per-page dimensions',
    description:
      'Select 2 or more PDF files via Android SAF, reorder them with instant visual feedback, and merge into a single ISO PDF 1.4 file on-device.',
    engineCall: 'PdfProcessingEngine.mergePdfs(context, inputFiles, outputFileName)',
    metrics: [
      { label: 'Input Documents', value: '3 PDFs (4 + 3 + 3 pages)' },
      { label: 'Page Dimensions', value: 'Preserved per page (A4 & Letter)' },
      { label: 'Merged Output', value: '10 pages • 100% Local' }
    ]
  },
  {
    id: 'split',
    label: 'Split PDF',
    badge: 'Range & Page Extraction',
    headline: 'Extract custom page ranges, single sheets, or burst every page into separate PDFs',
    description:
      'Enter flexible range syntax like "1-3, 5" or burst an entire multi-page contract into individual PDF documents in milliseconds.',
    engineCall: 'PdfProcessingEngine.splitPdf(context, sourceFile, SplitMode.CUSTOM_RANGE, "1-3, 5")',
    metrics: [
      { label: 'Range Syntax', value: '1-3, 5 (Custom Range)' },
      { label: 'Split Modes', value: 'Range • Single Page • Burst All' },
      { label: 'Validation', value: 'Bounds-checked against pageCount' }
    ]
  },
  {
    id: 'compress',
    label: 'Compress PDF',
    badge: 'Smart Raster Optimization',
    headline: 'Downsample embedded raster streams with Low, Medium, or High presets',
    description:
      'Choose 150 DPI (84% quality), 115 DPI (68% quality), or 85 DPI (50% quality) to shrink large scanned PDFs for email attachments.',
    engineCall: 'PdfProcessingEngine.compressPdf(context, sourceFile, CompressionPreset.MEDIUM)',
    metrics: [
      { label: 'Original Size', value: '692 KB (Scanned PDF)' },
      { label: 'Compressed Size', value: '318 KB (-54% Reduction)' },
      { label: 'Memory Safety', value: 'Immediate Bitmap.recycle()' }
    ]
  },
  {
    id: 'scanner',
    label: 'CameraX Scanner',
    badge: 'Hardware Document Capture',
    headline: 'Capture physical pages with CameraX, auto-crop edges, and apply B&W filters',
    description:
      'Turn paper receipts, whiteboards, and signed contracts into crisp A4, US Letter, or Legal PDFs with B&W Sharp, Magic Color, or Grayscale enhancement.',
    engineCall: 'PdfProcessingEngine.createPdfFromScannedPages(context, pages, PaperSizePreset.A4)',
    metrics: [
      { label: 'Color Filters', value: 'B&W Sharp • Magic Color • Grayscale' },
      { label: 'Paper Presets', value: 'A4 • US Letter • US Legal' },
      { label: 'Permission Scope', value: 'CAMERA only when scanning' }
    ]
  },
  {
    id: 'annotate',
    label: 'Annotate & Sign',
    badge: 'Vector Ink & Signature Pad',
    headline: 'Draw freehand ink, highlight clauses, and burn vector signatures into PDF pages',
    description:
      'Normalized [0..1] page coordinates map accurately across 0°, 90°, 180°, and 270° rotated pages and flatten directly into the exported PDF.',
    engineCall: 'PdfProcessingEngine.exportAnnotatedPdf(context, sourceFile, annotations, signature)',
    metrics: [
      { label: 'Markup Tools', value: 'Ink • Highlight • Underline • Note' },
      { label: 'Signature Vault', value: 'Reusable vector strokes in Room DB' },
      { label: 'Rotation Mapping', value: '0° / 90° / 180° / 270° verified' }
    ]
  },
  {
    id: 'organize',
    label: 'Page Organizer',
    badge: 'Reorder, Rotate & Stamp',
    headline: 'Reorder pages, rotate individual sheets, delete pages, or stamp watermarks & page numbers',
    description:
      'Inspect every page in your document, rotate individual pages in 90° increments, remove unwanted sheets, and export a clean PDF.',
    engineCall: 'PdfProcessingEngine.organizePdfPages(context, sourceFile, pageOperations)',
    metrics: [
      { label: 'Page Operations', value: 'Reorder • Rotate • Delete • Extract' },
      { label: 'Stamping Tools', value: 'Diagonal Watermark & Page X of Y' },
      { label: 'Integrity', value: 'SHA-256 checksum inspector' }
    ]
  }
];

export const InteractiveToolSimulator: React.FC = () => {
  const [activeMode, setActiveMode] = useState<SimMode>('merge');
  const [compressPreset, setCompressPreset] = useState<'low' | 'medium' | 'high'>('medium');
  const shouldReduceMotion = useReducedMotion();

  const currentInfo = SIM_MODES.find((m) => m.id === activeMode) || SIM_MODES[0];

  const modeIcons: Record<SimMode, React.ReactNode> = {
    merge: <Layers className="w-4 h-4" />,
    split: <Scissors className="w-4 h-4" />,
    compress: <Minimize2 className="w-4 h-4" />,
    scanner: <Camera className="w-4 h-4" />,
    annotate: <PenTool className="w-4 h-4" />,
    organize: <LayoutGrid className="w-4 h-4" />
  };

  return (
    <div className="rounded-3xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 shadow-xl overflow-hidden">
      {/* Top Mode Selector Bar */}
      <div className="border-b border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-[#0B1120]/60 px-4 py-3 flex flex-wrap gap-2 items-center justify-between">
        <div className="flex flex-wrap gap-2" role="tablist" aria-label="Interactive PDF workflow simulator">
          {SIM_MODES.map((mode) => {
            const isActive = mode.id === activeMode;
            return (
              <button
                key={mode.id}
                role="tab"
                aria-selected={isActive}
                onClick={() => setActiveMode(mode.id)}
                className={`inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all ${
                  isActive
                    ? 'bg-crimson-700 text-white shadow-sm'
                    : 'bg-white dark:bg-slate-800/90 text-slate-700 dark:text-slate-300 border border-slate-300 dark:border-slate-700 hover:border-crimson-500'
                }`}
              >
                {modeIcons[mode.id]}
                <span>{mode.label}</span>
              </button>
            );
          })}
        </div>
        <span className="hidden lg:inline-flex items-center gap-1.5 text-xs font-mono font-semibold text-emerald-700 dark:text-emerald-400 bg-emerald-100 dark:bg-emerald-950/70 border border-emerald-300 dark:border-emerald-800 px-3 py-1 rounded-full">
          <ShieldCheck className="w-3.5 h-3.5" />
          100% On-Device Execution
        </span>
      </div>

      {/* Main Content Split */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 p-6 sm:p-8 items-center">
        {/* Left: Description & Engine Telemetry */}
        <div className="lg:col-span-6 space-y-5">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold bg-crimson-100 text-crimson-800 dark:bg-crimson-950 dark:text-crimson-200 border border-crimson-200 dark:border-crimson-800">
            <Sparkles className="w-3.5 h-3.5" />
            <span>{currentInfo.badge}</span>
          </div>

          <h3 className="text-2xl sm:text-3xl font-display font-bold text-slate-900 dark:text-white leading-tight">
            {currentInfo.headline}
          </h3>

          <p className="text-slate-600 dark:text-slate-300 text-sm sm:text-base leading-relaxed">
            {currentInfo.description}
          </p>

          {/* Code / Engine Call Box */}
          <div className="rounded-xl bg-slate-900 dark:bg-[#0B1120] border border-slate-700 p-3.5 font-mono text-xs text-emerald-300 overflow-x-auto">
            <div className="text-slate-400 text-[11px] mb-1">// Kotlin Native Engine Invocation</div>
            <code>{currentInfo.engineCall}</code>
          </div>

          {/* Metrics Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1">
            {currentInfo.metrics.map((metric) => (
              <div
                key={metric.label}
                className="rounded-xl bg-slate-100 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700/80 p-3"
              >
                <div className="text-[11px] font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                  {metric.label}
                </div>
                <div className="text-xs sm:text-sm font-bold text-slate-900 dark:text-white mt-1">
                  {metric.value}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right: Choreographed Functional Motion Canvas */}
        <div className="lg:col-span-6">
          <div className="rounded-2xl bg-slate-100 dark:bg-[#0B1120] border border-slate-300 dark:border-slate-800 p-6 min-h-[320px] flex flex-col justify-center relative overflow-hidden">
            <AnimatePresence mode="wait">
              {activeMode === 'merge' && (
                <motion.div
                  key="merge"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -12 }}
                  className="space-y-4"
                >
                  <div className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 text-center">
                    Functional Motion • Multi-PDF Merge Pipeline
                  </div>
                  <div className="grid grid-cols-3 gap-3">
                    {[
                      { name: 'Architecture.pdf', pages: '4 pages', color: 'border-crimson-500 bg-crimson-50 dark:bg-crimson-950/40' },
                      { name: 'Audit_Q4.pdf', pages: '3 pages', color: 'border-indigo-500 bg-indigo-50 dark:bg-indigo-950/40' },
                      { name: 'Contract.pdf', pages: '3 pages', color: 'border-emerald-500 bg-emerald-50 dark:bg-emerald-950/40' }
                    ].map((doc, idx) => (
                      <motion.div
                        key={doc.name}
                        initial={shouldReduceMotion ? false : { y: 16, opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        transition={{ delay: idx * 0.1 }}
                        className={`rounded-xl border-2 ${doc.color} p-3 text-center`}
                      >
                        <FileText className="w-6 h-6 mx-auto mb-1.5 text-slate-800 dark:text-slate-100" />
                        <div className="text-xs font-bold text-slate-900 dark:text-white truncate">{doc.name}</div>
                        <div className="text-[11px] text-slate-600 dark:text-slate-400">{doc.pages}</div>
                      </motion.div>
                    ))}
                  </div>
                  <div className="flex justify-center">
                    <ArrowRight className="w-5 h-5 text-crimson-600 rotate-90" />
                  </div>
                  <motion.div
                    initial={shouldReduceMotion ? false : { scale: 0.95, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    transition={{ delay: 0.25 }}
                    className="rounded-xl bg-crimson-700 text-white p-4 flex items-center justify-between shadow-lg"
                  >
                    <div className="flex items-center gap-3">
                      <CheckCircle2 className="w-6 h-6 text-emerald-300 shrink-0" />
                      <div>
                        <div className="text-sm font-bold">Merged_Release_Bundle.pdf</div>
                        <div className="text-xs text-crimson-100">10 Pages • Mixed A4 &amp; Letter Dimensions Preserved</div>
                      </div>
                    </div>
                    <span className="text-xs font-mono bg-white/20 px-2.5 py-1 rounded-lg font-bold">PDF 1.4</span>
                  </motion.div>
                </motion.div>
              )}

              {activeMode === 'split' && (
                <motion.div
                  key="split"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -12 }}
                  className="space-y-4"
                >
                  <div className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 text-center">
                    Functional Motion • Custom Page Range Extractor (1-3, 5)
                  </div>
                  <div className="grid grid-cols-5 gap-2">
                    {[1, 2, 3, 4, 5].map((page) => {
                      const selected = page !== 4;
                      return (
                        <motion.div
                          key={page}
                          animate={
                            shouldReduceMotion
                              ? {}
                              : { y: selected ? -6 : 4, scale: selected ? 1.03 : 0.95 }
                          }
                          className={`rounded-xl p-3 border text-center transition-all ${
                            selected
                              ? 'bg-crimson-600 text-white border-crimson-500 shadow-md'
                              : 'bg-slate-200 dark:bg-slate-800 text-slate-500 border-slate-300 dark:border-slate-700 opacity-60'
                          }`}
                        >
                          <div className="text-xs font-mono font-bold">P.{page}</div>
                          <div className="text-[10px] mt-1">{selected ? 'Extract' : 'Skip'}</div>
                        </motion.div>
                      );
                    })}
                  </div>
                  <div className="rounded-xl bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 p-4 flex items-center justify-between">
                    <div>
                      <div className="text-xs font-semibold text-slate-500 dark:text-slate-400">Extracted Output PDF</div>
                      <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                        Pages 1, 2, 3, 5 → 4-Page Standalone Document
                      </div>
                    </div>
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
                      Verified
                    </span>
                  </div>
                </motion.div>
              )}

              {activeMode === 'compress' && (
                <motion.div
                  key="compress"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -12 }}
                  className="space-y-4"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
                      Interactive Preset Preview
                    </span>
                    <div className="flex gap-1.5">
                      {(['low', 'medium', 'high'] as const).map((preset) => (
                        <button
                          key={preset}
                          onClick={() => setCompressPreset(preset)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-bold capitalize ${
                            compressPreset === preset
                              ? 'bg-crimson-700 text-white'
                              : 'bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          {preset}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="space-y-3 bg-white dark:bg-slate-800/90 rounded-xl p-4 border border-slate-300 dark:border-slate-700">
                    <div>
                      <div className="flex justify-between text-xs font-bold mb-1">
                        <span className="text-slate-600 dark:text-slate-400">Original Source File</span>
                        <span className="font-mono text-slate-900 dark:text-white">692 KB (100%)</span>
                      </div>
                      <div className="w-full h-3 rounded-full bg-slate-200 dark:bg-slate-700 overflow-hidden">
                        <div className="h-full bg-slate-500 w-full rounded-full" />
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between text-xs font-bold mb-1">
                        <span className="text-emerald-700 dark:text-emerald-400">
                          Compressed ({compressPreset === 'low' ? '150 DPI • 84% Q' : compressPreset === 'medium' ? '115 DPI • 68% Q' : '85 DPI • 50% Q'})
                        </span>
                        <span className="font-mono text-emerald-700 dark:text-emerald-400">
                          {compressPreset === 'low' ? '498 KB (-28%)' : compressPreset === 'medium' ? '318 KB (-54%)' : '194 KB (-72%)'}
                        </span>
                      </div>
                      <div className="w-full h-3 rounded-full bg-slate-200 dark:bg-slate-700 overflow-hidden">
                        <motion.div
                          initial={false}
                          animate={{
                            width: compressPreset === 'low' ? '72%' : compressPreset === 'medium' ? '46%' : '28%'
                          }}
                          className="h-full bg-emerald-600 rounded-full"
                        />
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}

              {activeMode === 'scanner' && (
                <motion.div
                  key="scanner"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -12 }}
                  className="space-y-3"
                >
                  <div className="rounded-xl bg-slate-900 border border-slate-700 p-4 text-white relative overflow-hidden">
                    <div className="flex items-center justify-between text-xs mb-3">
                      <span className="font-mono text-emerald-400 font-bold">● CAMERAX VIEWFINDER</span>
                      <span className="bg-emerald-950 text-emerald-300 border border-emerald-700 px-2 py-0.5 rounded text-[11px] font-bold">
                        B&amp;W Document Filter
                      </span>
                    </div>
                    <div className="border-2 border-emerald-400 rounded-lg bg-white text-slate-900 p-4 mx-auto max-w-xs shadow-inner">
                      <div className="text-[11px] font-bold text-crimson-700">SCANNED CONTRACT • A4 PORTRAIT</div>
                      <div className="h-2 bg-slate-900 rounded w-3/4 mt-2" />
                      <div className="h-2 bg-slate-600 rounded w-full mt-1.5" />
                      <div className="h-2 bg-slate-600 rounded w-5/6 mt-1.5" />
                      <div className="mt-3 pt-2 border-t border-slate-300 flex justify-between text-[10px] font-mono text-slate-600">
                        <span>Auto-Crop Locked</span>
                        <span>300 DPI Crisp Text</span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}

              {activeMode === 'annotate' && (
                <motion.div
                  key="annotate"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -12 }}
                  className="space-y-3"
                >
                  <div className="rounded-xl bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 p-4 space-y-3">
                    <div className="text-xs font-bold text-crimson-700 dark:text-crimson-300">
                      CLAUSE 4.2 • LOCAL DATA SOVEREIGNTY
                    </div>
                    <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-200">
                      All PDF rendering and vector ink serialization{' '}
                      <span className="bg-yellow-300/80 dark:bg-yellow-500/40 text-slate-950 dark:text-white px-1.5 py-0.5 rounded font-bold">
                        executes strictly on the user’s Android device
                      </span>{' '}
                      without network calls.
                    </p>
                    <div className="rounded-lg border border-dashed border-indigo-500 bg-indigo-50/60 dark:bg-indigo-950/40 p-3 flex items-center justify-between">
                      <div>
                        <div className="text-[10px] font-bold uppercase tracking-wider text-indigo-700 dark:text-indigo-300">
                          Flattened Vector Signature
                        </div>
                        <svg viewBox="0 0 180 36" className="w-36 h-8 stroke-indigo-700 dark:stroke-indigo-300 fill-none">
                          <path
                            d="M 8 26 C 28 6, 44 30, 68 14 C 88 4, 110 28, 168 12"
                            strokeWidth="2.8"
                            strokeLinecap="round"
                          />
                        </svg>
                      </div>
                      <span className="text-[11px] font-mono font-bold text-emerald-700 dark:text-emerald-400">
                        Burned to PDF
                      </span>
                    </div>
                  </div>
                </motion.div>
              )}

              {activeMode === 'organize' && (
                <motion.div
                  key="organize"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -12 }}
                  className="space-y-3"
                >
                  <div className="grid grid-cols-3 gap-3">
                    {[
                      { page: 'Page 1', status: 'Rotated 90°', badge: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-950 dark:text-indigo-300' },
                      { page: 'Page 2', status: 'Watermarked', badge: 'bg-crimson-100 text-crimson-800 dark:bg-crimson-950 dark:text-crimson-300' },
                      { page: 'Page 3', status: 'Numbered 3/3', badge: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300' }
                    ].map((item) => (
                      <div
                        key={item.page}
                        className="rounded-xl bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-700 p-3 text-center"
                      >
                        <div className="text-xs font-bold text-slate-900 dark:text-white">{item.page}</div>
                        <span className={`inline-block mt-2 px-2 py-0.5 rounded text-[10px] font-bold ${item.badge}`}>
                          {item.status}
                        </span>
                      </div>
                    ))}
                  </div>
                  <div className="rounded-xl bg-slate-900 text-slate-100 p-3 font-mono text-xs flex items-center justify-between">
                    <span>SHA-256 Checksum Verified</span>
                    <span className="text-emerald-400 font-bold">e3b0c442...98fc1c14</span>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </div>
  );
};
