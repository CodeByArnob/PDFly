import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence, useReducedMotion } from 'framer-motion';
import {
  BookOpen,
  Layers,
  Scissors,
  Minimize2,
  Camera,
  PenTool,
  LayoutGrid,
  CheckCircle2,
  ArrowUp,
  ArrowDown,
  FileText,
  Sparkles,
  ShieldCheck,
  Play,
  Pause,
  RotateCw,
  Bookmark,
  ZoomIn
} from 'lucide-react';

type SimMode = 'reader' | 'merge' | 'split' | 'compress' | 'scanner' | 'annotate' | 'organize';

interface ModeInfo {
  id: SimMode;
  label: string;
  badge: string;
  headline: string;
  description: string;
  engineMethod: string;
  metrics: { label: string; value: string }[];
}

const SIM_MODES: ModeInfo[] = [
  {
    id: 'reader',
    label: 'PDF Reader',
    badge: 'Native Page Renderer',
    headline: 'Read, zoom, search text, and bookmark pages with zero-latency native rendering',
    description:
      'Android PdfRenderer renders each PDF page into a hardware-backed bitmap with pinch-to-zoom, 90-degree page rotation, keyword search, and persistent bookmarks.',
    engineMethod: 'PdfProcessingEngine.renderPageToBitmap(file, pageIndex, targetWidthPx, rotationDegrees)',
    metrics: [
      { label: 'Rendering Engine', value: 'Android Native PdfRenderer' },
      { label: 'Zoom & Rotation', value: '1.0x to 3.5x • 0/90/180/270 deg' },
      { label: 'Search & Bookmarks', value: 'Instant Local Index' }
    ]
  },
  {
    id: 'merge',
    label: 'Merge PDF',
    badge: 'Multi-Document Combine',
    headline: 'Combine multiple PDFs in custom order while preserving per-page dimensions',
    description:
      'Select 2 or more PDF files via Android SAF, reorder them with instant visual feedback, and merge into a single ISO PDF 1.4 file on-device.',
    engineMethod: 'PdfProcessingEngine.mergePdfs(context, inputFiles, outputFileName)',
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
      'Tap any page sheet to toggle extraction or enter flexible range syntax like 1-3, 5 to create standalone PDF documents in milliseconds.',
    engineMethod: 'PdfProcessingEngine.splitPdf(context, sourceFile, SplitMode.CUSTOM_RANGE, rangeInput)',
    metrics: [
      { label: 'Range Syntax', value: 'Interactive Deck & Custom Range' },
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
    engineMethod: 'PdfProcessingEngine.compressPdf(context, sourceFile, CompressionPreset.MEDIUM)',
    metrics: [
      { label: 'Original Size', value: '692 KB (Scanned PDF)' },
      { label: 'Compressed Size', value: '194 KB to 498 KB (Calibrated)' },
      { label: 'Memory Safety', value: 'Immediate Bitmap.recycle()' }
    ]
  },
  {
    id: 'scanner',
    label: 'CameraX Scanner',
    badge: 'Hardware Document Capture',
    headline: 'Capture physical pages with CameraX, auto-crop edges, and apply B&W filters',
    description:
      'Turn paper receipts, whiteboards, and signed contracts into crisp A4, US Letter, or Legal PDFs with B&W Sharp, Magic Enhance, or Grayscale filters.',
    engineMethod: 'PdfProcessingEngine.createPdfFromScannedPages(context, pages, PaperSizePreset.A4)',
    metrics: [
      { label: 'Color Filters', value: 'B&W Sharp • Magic Enhance • Grayscale' },
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
      'Normalized [0..1] page coordinates map accurately across 0, 90, 180, and 270 degree rotated pages and flatten directly into the exported PDF.',
    engineMethod: 'PdfProcessingEngine.flattenAnnotationsToNewPdf(context, sourceFile, annotations, outputName)',
    metrics: [
      { label: 'Markup Tools', value: 'Ink • Highlight • Underline • Note' },
      { label: 'Signature Vault', value: 'Reusable vector strokes in Room DB' },
      { label: 'Rotation Mapping', value: '0 / 90 / 180 / 270 deg verified' }
    ]
  },
  {
    id: 'organize',
    label: 'Page Organizer',
    badge: 'Reorder, Rotate & Stamp',
    headline: 'Reorder pages, rotate individual sheets, delete pages, or stamp watermarks',
    description:
      'Inspect every page in your document, rotate individual pages in 90-degree increments, remove unwanted sheets, and export a clean PDF.',
    engineMethod: 'PdfProcessingEngine.organizePdfPages(context, sourceFile, organizedItems)',
    metrics: [
      { label: 'Page Operations', value: 'Reorder • Rotate • Delete • Extract' },
      { label: 'Stamping Tools', value: 'Diagonal Watermark & Page X of Y' },
      { label: 'Integrity', value: 'SHA-256 checksum inspector' }
    ]
  }
];

export const InteractiveToolSimulator: React.FC = () => {
  const [activeMode, setActiveMode] = useState<SimMode>('merge');
  const [isLiveAutoPlay, setIsLiveAutoPlay] = useState<boolean>(false);

  // Interactive states for each simulated workflow
  const [readerPage, setReaderPage] = useState<number>(1);
  const [readerZoom, setReaderZoom] = useState<number>(100);
  const [readerBookmarked, setReaderBookmarked] = useState<boolean>(true);

  const [mergeDocs, setMergeDocs] = useState([
    { id: 'doc1', name: 'Architecture_Spec.pdf', pages: 4, sizeKb: 245, accent: 'crimson' },
    { id: 'doc2', name: 'Q4_Financial_Audit.pdf', pages: 3, sizeKb: 198, accent: 'indigo' },
    { id: 'doc3', name: 'Vendor_Agreement.pdf', pages: 3, sizeKb: 164, accent: 'emerald' }
  ]);

  const [splitSelectedPages, setSplitSelectedPages] = useState<number[]>([1, 2, 3, 5]);
  const [compressPreset, setCompressPreset] = useState<'low' | 'medium' | 'high'>('medium');
  const [scanFilter, setScanFilter] = useState<'magic' | 'bw' | 'gray'>('bw');
  const [signatureInkColor, setSignatureInkColor] = useState<'indigo' | 'crimson' | 'emerald'>('indigo');
  const [orgRotations, setOrgRotations] = useState<number[]>([0, 90, 0]);

  const shouldReduceMotion = useReducedMotion();

  useEffect(() => {
    if (!isLiveAutoPlay) return;
    const timer = setInterval(() => {
      setActiveMode((prev) => {
        const idx = SIM_MODES.findIndex((m) => m.id === prev);
        return SIM_MODES[(idx + 1) % SIM_MODES.length].id;
      });
    }, 4200);
    return () => clearInterval(timer);
  }, [isLiveAutoPlay]);

  const currentInfo = SIM_MODES.find((m) => m.id === activeMode) || SIM_MODES[0];

  const modeIcons: Record<SimMode, React.ReactNode> = {
    reader: <BookOpen className="w-4 h-4" />,
    merge: <Layers className="w-4 h-4" />,
    split: <Scissors className="w-4 h-4" />,
    compress: <Minimize2 className="w-4 h-4" />,
    scanner: <Camera className="w-4 h-4" />,
    annotate: <PenTool className="w-4 h-4" />,
    organize: <LayoutGrid className="w-4 h-4" />
  };

  const moveMergeDoc = (index: number, direction: -1 | 1) => {
    const target = index + direction;
    if (target < 0 || target >= mergeDocs.length) return;
    const copy = [...mergeDocs];
    const [item] = copy.splice(index, 1);
    copy.splice(target, 0, item);
    setMergeDocs(copy);
  };

  const toggleSplitPage = (page: number) => {
    setSplitSelectedPages((prev) =>
      prev.includes(page)
        ? prev.length > 1
          ? prev.filter((p) => p !== page)
          : prev
        : [...prev, page].sort((a, b) => a - b)
    );
  };

  const rotateOrgPage = (idx: number) => {
    setOrgRotations((prev) => prev.map((rot, i) => (i === idx ? (rot + 90) % 360 : rot)));
  };

  return (
    <div className="rounded-3xl bg-white dark:bg-[#131C31] border-2 border-slate-300 dark:border-slate-700 shadow-xl overflow-hidden">
      {/* Top Mode Selector & Live Mode Control Bar */}
      <div className="border-b border-slate-200 dark:border-slate-800 bg-slate-100/90 dark:bg-[#0B1120]/80 px-4 sm:px-6 py-3.5 flex flex-col xl:flex-row gap-3 xl:items-center xl:justify-between">
        <div className="flex flex-wrap gap-2" role="tablist" aria-label="Interactive PDF workflow simulator">
          {SIM_MODES.map((mode) => {
            const isActive = mode.id === activeMode;
            return (
              <button
                key={mode.id}
                type="button"
                role="tab"
                aria-selected={isActive}
                onClick={() => {
                  setActiveMode(mode.id);
                  setIsLiveAutoPlay(false);
                }}
                className={`inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all ${
                  isActive
                    ? 'bg-crimson-700 text-white shadow-sm ring-2 ring-crimson-500/30'
                    : 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700 hover:border-crimson-600 dark:hover:border-crimson-400'
                }`}
              >
                {modeIcons[mode.id]}
                <span>{mode.label}</span>
              </button>
            );
          })}
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={() => setIsLiveAutoPlay(!isLiveAutoPlay)}
            className={`inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold border transition-all ${
              isLiveAutoPlay
                ? 'bg-amber-100 text-amber-900 border-amber-400 dark:bg-amber-950 dark:text-amber-200 dark:border-amber-700'
                : 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 border-slate-300 dark:border-slate-700 hover:border-crimson-500'
            }`}
          >
            {isLiveAutoPlay ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
            <span>{isLiveAutoPlay ? 'Live Auto-Cycle: Active' : 'Start Live Auto-Cycle'}</span>
          </button>

          <span className="inline-flex items-center gap-1.5 text-xs font-mono font-bold text-emerald-800 dark:text-emerald-300 bg-emerald-100 dark:bg-emerald-950/90 border border-emerald-300 dark:border-emerald-700 px-3 py-1.5 rounded-full">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span>100% On-Device Execution</span>
          </span>
        </div>
      </div>

      {/* Main Content Split */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 p-6 sm:p-8 items-start">
        {/* Left: High-Contrast Description & Engine Telemetry */}
        <div className="lg:col-span-6 space-y-5">
          <div className="flex flex-wrap items-center gap-2">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-crimson-100 text-crimson-900 dark:bg-crimson-950 dark:text-crimson-200 border border-crimson-300 dark:border-crimson-800">
              <Sparkles className="w-3.5 h-3.5" />
              <span>{currentInfo.badge}</span>
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-mono font-bold bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700">
              Interactive Live Preview
            </span>
          </div>

          <h3 className="text-2xl sm:text-3xl font-display font-bold text-slate-950 dark:text-white leading-tight">
            {currentInfo.headline}
          </h3>

          <p className="text-slate-700 dark:text-slate-200 text-sm sm:text-base leading-relaxed">
            {currentInfo.description}
          </p>

          {/* Clean Engine Method Box */}
          <div className="rounded-2xl bg-slate-900 dark:bg-[#0B1120] border border-slate-700 p-4 space-y-1.5">
            <div className="flex items-center justify-between text-[11px] font-bold uppercase tracking-wider text-slate-300">
              <span>Kotlin Native Engine Call</span>
              <span className="text-emerald-400 font-mono">Dispatchers.IO</span>
            </div>
            <div className="font-mono text-xs sm:text-sm text-emerald-300 break-all">
              {currentInfo.engineMethod}
            </div>
          </div>

          {/* Metrics Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1">
            {currentInfo.metrics.map((metric) => (
              <div
                key={metric.label}
                className="rounded-xl bg-slate-100 dark:bg-slate-800/90 border border-slate-300 dark:border-slate-700 p-3.5"
              >
                <div className="text-[11px] font-bold uppercase tracking-wider text-slate-600 dark:text-slate-300">
                  {metric.label}
                </div>
                <div className="text-xs sm:text-sm font-bold text-slate-950 dark:text-white mt-1">
                  {metric.value}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right: Non-Overlapping Interactive Live Mode Workspace */}
        <div className="lg:col-span-6 w-full">
          <div className="rounded-2xl bg-slate-100 dark:bg-[#0B1120] border-2 border-slate-300 dark:border-slate-800 p-5 sm:p-6 min-h-[360px] flex flex-col justify-between">
            <AnimatePresence mode="wait">
              {activeMode === 'reader' && (
                <motion.div
                  key="reader"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-300 dark:border-slate-800 pb-3">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Live PDF Reader Canvas (Page {readerPage} of 4)
                    </span>
                    <div className="flex items-center gap-1.5">
                      {[1, 2, 3, 4].map((p) => (
                        <button
                          key={p}
                          type="button"
                          onClick={() => setReaderPage(p)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-mono font-bold transition-colors ${
                            readerPage === p
                              ? 'bg-crimson-700 text-white'
                              : 'bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          P.{p}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="rounded-xl bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 p-4 space-y-3 shadow-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-mono font-bold text-crimson-700 dark:text-crimson-300">
                        PDFly_Architecture_Guide.pdf • {readerZoom}% Zoom
                      </span>
                      <div className="flex items-center gap-1.5">
                        <button
                          type="button"
                          onClick={() => setReaderZoom((z) => (z === 100 ? 150 : z === 150 ? 200 : 100))}
                          className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700"
                        >
                          <ZoomIn className="w-3.5 h-3.5" />
                          <span>{readerZoom}%</span>
                        </button>
                        <button
                          type="button"
                          onClick={() => setReaderBookmarked(!readerBookmarked)}
                          className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold border ${
                            readerBookmarked
                              ? 'bg-amber-100 text-amber-900 border-amber-400 dark:bg-amber-950 dark:text-amber-200 dark:border-amber-700'
                              : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          <Bookmark className="w-3.5 h-3.5" />
                          <span>{readerBookmarked ? 'Bookmarked' : 'Bookmark'}</span>
                        </button>
                      </div>
                    </div>

                    <div className="rounded-lg bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 p-3.5 space-y-2">
                      <div className="text-sm font-bold text-slate-950 dark:text-white">
                        Section {readerPage}.0: On-Device ISO PDF 1.4 Processing
                      </div>
                      <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-200 leading-relaxed">
                        Page {readerPage} renders directly from local storage using Android PdfRenderer with zero
                        network buffers and full pinch-to-zoom clarity.
                      </p>
                    </div>
                  </div>
                </motion.div>
              )}

              {activeMode === 'merge' && (
                <motion.div
                  key="merge"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex items-center justify-between border-b border-slate-300 dark:border-slate-800 pb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Interactive Merge Queue (Click Arrows to Reorder)
                    </span>
                    <span className="text-xs font-mono font-bold text-indigo-700 dark:text-indigo-300">
                      3 Files → 10 Pages
                    </span>
                  </div>

                  <div className="space-y-2">
                    {mergeDocs.map((doc, idx) => (
                      <div
                        key={doc.id}
                        className="rounded-xl bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 px-3.5 py-2.5 flex items-center justify-between gap-3"
                      >
                        <div className="flex items-center gap-3 min-w-0">
                          <span className="w-6 h-6 rounded-lg bg-indigo-100 dark:bg-indigo-950 text-indigo-800 dark:text-indigo-200 font-mono text-xs font-bold flex items-center justify-center shrink-0">
                            #{idx + 1}
                          </span>
                          <FileText className="w-4 h-4 text-crimson-600 dark:text-crimson-400 shrink-0" />
                          <div className="min-w-0">
                            <div className="text-xs sm:text-sm font-bold text-slate-950 dark:text-white truncate">
                              {doc.name}
                            </div>
                            <div className="text-[11px] font-medium text-slate-600 dark:text-slate-300">
                              {doc.pages} pages • {doc.sizeKb} KB
                            </div>
                          </div>
                        </div>

                        <div className="flex items-center gap-1 shrink-0">
                          <button
                            type="button"
                            disabled={idx === 0}
                            onClick={() => moveMergeDoc(idx, -1)}
                            aria-label={`Move ${doc.name} up`}
                            className="p-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200 disabled:opacity-40"
                          >
                            <ArrowUp className="w-3.5 h-3.5" />
                          </button>
                          <button
                            type="button"
                            disabled={idx === mergeDocs.length - 1}
                            onClick={() => moveMergeDoc(idx, 1)}
                            aria-label={`Move ${doc.name} down`}
                            className="p-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200 disabled:opacity-40"
                          >
                            <ArrowDown className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="rounded-xl bg-crimson-700 text-white p-3.5 flex flex-wrap items-center justify-between gap-2 shadow-md">
                    <div className="flex items-center gap-2.5">
                      <CheckCircle2 className="w-5 h-5 text-emerald-300 shrink-0" />
                      <div>
                        <div className="text-xs sm:text-sm font-bold">
                          Merged_Output.pdf ({mergeDocs.map((d) => d.name.split('_')[0]).join(' + ')})
                        </div>
                        <div className="text-[11px] text-crimson-100">
                          10 Pages • 607 KB • Per-Page Dimensions Preserved
                        </div>
                      </div>
                    </div>
                    <span className="text-xs font-mono bg-white/20 px-2.5 py-1 rounded-lg font-bold">
                      ISO PDF 1.4
                    </span>
                  </div>
                </motion.div>
              )}

              {activeMode === 'split' && (
                <motion.div
                  key="split"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex items-center justify-between border-b border-slate-300 dark:border-slate-800 pb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Tap Any Page Sheet to Toggle Extraction
                    </span>
                    <span className="text-xs font-mono font-bold text-crimson-700 dark:text-crimson-300">
                      {splitSelectedPages.length} of 6 Selected
                    </span>
                  </div>

                  <div className="grid grid-cols-3 sm:grid-cols-6 gap-2.5 pt-1">
                    {[1, 2, 3, 4, 5, 6].map((page) => {
                      const selected = splitSelectedPages.includes(page);
                      return (
                        <button
                          key={page}
                          type="button"
                          onClick={() => toggleSplitPage(page)}
                          className={`rounded-xl p-3 border-2 text-center transition-all ${
                            selected
                              ? 'bg-crimson-700 text-white border-crimson-500 shadow-sm -translate-y-1'
                              : 'bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          <div className="text-xs font-mono font-bold">P.{page}</div>
                          <div className="text-[11px] font-semibold mt-1">{selected ? 'Extract' : 'Skip'}</div>
                        </button>
                      );
                    })}
                  </div>

                  <div className="rounded-xl bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 p-4 flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <div className="text-xs font-bold text-slate-600 dark:text-slate-300">
                        Extracted Output Document
                      </div>
                      <div className="text-sm font-bold text-slate-950 dark:text-white mt-0.5">
                        Pages [{splitSelectedPages.join(', ')}] → {splitSelectedPages.length}-Page PDF
                      </div>
                    </div>
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-900 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-700">
                      Bounds Verified
                    </span>
                  </div>
                </motion.div>
              )}

              {activeMode === 'compress' && (
                <motion.div
                  key="compress"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-300 dark:border-slate-800 pb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Select Compression Preset
                    </span>
                    <div className="flex gap-1.5">
                      {(['low', 'medium', 'high'] as const).map((preset) => (
                        <button
                          key={preset}
                          type="button"
                          onClick={() => setCompressPreset(preset)}
                          className={`px-3 py-1 rounded-lg text-xs font-bold capitalize transition-colors ${
                            compressPreset === preset
                              ? 'bg-crimson-700 text-white'
                              : 'bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          {preset}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="space-y-4 bg-white dark:bg-slate-900 rounded-xl p-4 border border-slate-300 dark:border-slate-700">
                    <div>
                      <div className="flex justify-between text-xs font-bold mb-1.5">
                        <span className="text-slate-700 dark:text-slate-300">Original Source File</span>
                        <span className="font-mono text-slate-950 dark:text-white">692 KB (100%)</span>
                      </div>
                      <div className="w-full h-3 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
                        <div className="h-full bg-slate-500 w-full rounded-full" />
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between text-xs font-bold mb-1.5">
                        <span className="text-emerald-800 dark:text-emerald-300">
                          Optimized ({compressPreset === 'low' ? '150 DPI • 84% Q' : compressPreset === 'medium' ? '115 DPI • 68% Q' : '85 DPI • 50% Q'})
                        </span>
                        <span className="font-mono text-emerald-800 dark:text-emerald-300">
                          {compressPreset === 'low' ? '498 KB (-28%)' : compressPreset === 'medium' ? '318 KB (-54%)' : '194 KB (-72%)'}
                        </span>
                      </div>
                      <div className="w-full h-3 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden">
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
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-300 dark:border-slate-800 pb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      CameraX Filter Preset
                    </span>
                    <div className="flex gap-1.5">
                      {[
                        { id: 'bw', label: 'B&W Sharp' },
                        { id: 'magic', label: 'Magic Enhance' },
                        { id: 'gray', label: 'Grayscale' }
                      ].map((f) => (
                        <button
                          key={f.id}
                          type="button"
                          onClick={() => setScanFilter(f.id as 'bw' | 'magic' | 'gray')}
                          className={`px-2.5 py-1 rounded-lg text-xs font-bold ${
                            scanFilter === f.id
                              ? 'bg-crimson-700 text-white'
                              : 'bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          {f.label}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="rounded-xl bg-slate-900 border border-slate-700 p-4 text-white">
                    <div className="flex items-center justify-between text-xs mb-3">
                      <span className="font-mono text-emerald-400 font-bold">CAMERAX HARDWARE VIEWFINDER</span>
                      <span className="bg-emerald-950 text-emerald-300 border border-emerald-700 px-2.5 py-0.5 rounded text-[11px] font-bold">
                        {scanFilter === 'bw' ? 'High-Contrast B&W' : scanFilter === 'magic' ? 'Magic Color Boost' : '256-Level Grayscale'}
                      </span>
                    </div>
                    <div
                      className={`border-2 border-emerald-400 rounded-lg p-4 mx-auto max-w-xs transition-colors ${
                        scanFilter === 'bw'
                          ? 'bg-white text-slate-950'
                          : scanFilter === 'magic'
                          ? 'bg-amber-50 text-slate-950'
                          : 'bg-slate-200 text-slate-900'
                      }`}
                    >
                      <div className="text-[11px] font-bold text-crimson-700">SCANNED CONTRACT • A4 PORTRAIT</div>
                      <div className="h-2 bg-slate-900 rounded w-3/4 mt-2" />
                      <div className="h-2 bg-slate-700 rounded w-full mt-1.5" />
                      <div className="h-2 bg-slate-700 rounded w-5/6 mt-1.5" />
                      <div className="mt-3 pt-2 border-t border-slate-300 flex justify-between text-[11px] font-mono font-bold text-slate-700">
                        <span>Perspective Crop: 4%</span>
                        <span>300 DPI Output</span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}

              {activeMode === 'annotate' && (
                <motion.div
                  key="annotate"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-300 dark:border-slate-800 pb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Vector Signature Ink Color
                    </span>
                    <div className="flex gap-1.5">
                      {(['indigo', 'crimson', 'emerald'] as const).map((color) => (
                        <button
                          key={color}
                          type="button"
                          onClick={() => setSignatureInkColor(color)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-bold capitalize ${
                            signatureInkColor === color
                              ? 'bg-crimson-700 text-white'
                              : 'bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700'
                          }`}
                        >
                          {color} Ink
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="rounded-xl bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 p-4 space-y-3">
                    <div className="text-xs font-bold text-crimson-700 dark:text-crimson-300">
                      CLAUSE 4.2 • LOCAL DATA SOVEREIGNTY
                    </div>
                    <p className="text-xs sm:text-sm text-slate-800 dark:text-slate-200 leading-relaxed">
                      All PDF rendering and vector ink serialization{' '}
                      <span className="bg-amber-200 dark:bg-amber-500/40 text-slate-950 dark:text-white px-1.5 py-0.5 rounded font-bold">
                        executes strictly on the user’s Android device
                      </span>{' '}
                      without network calls.
                    </p>
                    <div className="rounded-lg border-2 border-dashed border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/70 p-3 flex items-center justify-between gap-3">
                      <div>
                        <div className="text-[11px] font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                          Flattened Vector Signature ({signatureInkColor.toUpperCase()})
                        </div>
                        <svg
                          viewBox="0 0 180 36"
                          className={`w-36 h-8 fill-none ${
                            signatureInkColor === 'indigo'
                              ? 'stroke-indigo-600 dark:stroke-indigo-400'
                              : signatureInkColor === 'crimson'
                              ? 'stroke-crimson-600 dark:stroke-crimson-400'
                              : 'stroke-emerald-600 dark:stroke-emerald-400'
                          }`}
                        >
                          <path
                            d="M 8 26 C 28 6, 44 30, 68 14 C 88 4, 110 28, 168 12"
                            strokeWidth="3"
                            strokeLinecap="round"
                          />
                        </svg>
                      </div>
                      <span className="text-xs font-mono font-bold text-emerald-800 dark:text-emerald-300 bg-emerald-100 dark:bg-emerald-950 px-2.5 py-1 rounded-lg border border-emerald-300 dark:border-emerald-800">
                        Burned to PDF
                      </span>
                    </div>
                  </div>
                </motion.div>
              )}

              {activeMode === 'organize' && (
                <motion.div
                  key="organize"
                  initial={shouldReduceMotion ? false : { opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={shouldReduceMotion ? undefined : { opacity: 0, y: -10 }}
                  className="space-y-4"
                >
                  <div className="flex items-center justify-between border-b border-slate-300 dark:border-slate-800 pb-2.5">
                    <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Click Any Page to Rotate 90 Degrees
                    </span>
                    <span className="text-xs font-mono font-bold text-emerald-800 dark:text-emerald-300">
                      3 Pages Active
                    </span>
                  </div>

                  <div className="grid grid-cols-3 gap-3">
                    {[0, 1, 2].map((idx) => (
                      <button
                        key={idx}
                        type="button"
                        onClick={() => rotateOrgPage(idx)}
                        className="rounded-xl bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 p-3 text-center hover:border-crimson-500 transition-all"
                      >
                        <div className="flex items-center justify-center gap-1 text-xs font-bold text-slate-950 dark:text-white">
                          <span>Page {idx + 1}</span>
                          <RotateCw className="w-3.5 h-3.5 text-crimson-600 dark:text-crimson-400" />
                        </div>
                        <span className="inline-block mt-2 px-2.5 py-0.5 rounded text-[11px] font-mono font-bold bg-indigo-100 text-indigo-900 dark:bg-indigo-950 dark:text-indigo-200">
                          {orgRotations[idx]} deg
                        </span>
                      </button>
                    ))}
                  </div>

                  <div className="rounded-xl bg-slate-900 text-slate-100 p-3.5 font-mono text-xs flex flex-wrap items-center justify-between gap-2">
                    <span>SHA-256 Integrity Hash Verified</span>
                    <span className="text-emerald-400 font-bold">e3b0c442...98fc1c14</span>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>

            {/* Bottom Live Telemetry Footer */}
            <div className="mt-4 pt-3 border-t border-slate-300/80 dark:border-slate-800 flex flex-wrap items-center justify-between gap-2 text-xs text-slate-700 dark:text-slate-300">
              <span className="font-semibold">
                Mode: <strong className="text-slate-950 dark:text-white">{currentInfo.label}</strong>
              </span>
              <span className="font-mono text-[11px] text-emerald-800 dark:text-emerald-300 font-bold">
                Zero Network Calls • Android SAF Verified
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
