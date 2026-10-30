import React, { useState, useEffect } from 'react';
import { motion, useReducedMotion } from 'framer-motion';
import {
  FileText,
  Shield,
  Sparkles,
  Download,
  Github,
  Sun,
  Moon,
  Search,
  CheckCircle2,
  Lock,
  Smartphone,
  ChevronDown,
  ExternalLink,
  Menu,
  X,
  Terminal,
  Heart,
  Layers,
  PenTool,
  FolderGit2,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import {
  PDFLY_TOOLS,
  TOOL_CATEGORIES,
  APP_SCREENSHOTS,
  ARCHITECTURE_LAYERS,
  FAQ_ITEMS,
  GITHUB_REPO_URL,
  GITHUB_RELEASES_URL,
  DIRECT_APK_DOWNLOAD_URL,
  GITHUB_ISSUES_URL,
  DEVELOPER_GITHUB_URL
} from './data/pdflyData';
import { InteractiveToolSimulator } from './components/InteractiveToolSimulator';
import { SupportDonationSection } from './components/SupportDonationSection';

export function App() {
  const [isDark, setIsDark] = useState<boolean>(false);
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [activeScreenshotId, setActiveScreenshotId] = useState<string>(APP_SCREENSHOTS[0].id);
  const [heroScreenIndex, setHeroScreenIndex] = useState<number>(0);
  const [openFaqIndex, setOpenFaqIndex] = useState<number | null>(0);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);
  const shouldReduceMotion = useReducedMotion();

  useEffect(() => {
    const root = document.documentElement;
    if (isDark) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }, [isDark]);

  const filteredTools = PDFLY_TOOLS.filter((tool) => {
    const matchesCategory = selectedCategory === 'all' || tool.category === selectedCategory;
    const q = searchQuery.trim().toLowerCase();
    const matchesQuery =
      !q ||
      tool.title.toLowerCase().includes(q) ||
      tool.subtitle.toLowerCase().includes(q) ||
      tool.badge.toLowerCase().includes(q);
    return matchesCategory && matchesQuery;
  });

  const activeScreenshot =
    APP_SCREENSHOTS.find((s) => s.id === activeScreenshotId) || APP_SCREENSHOTS[0];

  const heroScreenshot = APP_SCREENSHOTS[heroScreenIndex] || APP_SCREENSHOTS[0];

  const stepActiveScreenshot = (dir: -1 | 1) => {
    const idx = APP_SCREENSHOTS.findIndex((s) => s.id === activeScreenshot.id);
    const nextIdx = (idx + dir + APP_SCREENSHOTS.length) % APP_SCREENSHOTS.length;
    setActiveScreenshotId(APP_SCREENSHOTS[nextIdx].id);
  };

  return (
    <div className="min-h-screen bg-[#F1F5F9] dark:bg-[#0B1120] text-slate-900 dark:text-slate-50 transition-colors">
      {/* Top Sticky Navigation */}
      <header className="sticky top-0 z-50 backdrop-blur-md bg-white/95 dark:bg-[#0B1120]/95 border-b border-slate-300 dark:border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Brand Logo */}
          <a href="#" className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-crimson-600 to-indigo-700 flex items-center justify-center text-white font-display font-bold text-sm shadow-sm group-hover:scale-105 transition-transform">
              PDF
            </div>
            <div>
              <div className="font-display font-bold text-lg leading-none text-slate-950 dark:text-white">
                PDFly
              </div>
              <div className="text-[11px] font-semibold text-slate-600 dark:text-slate-300">
                Android PDF Studio
              </div>
            </div>
          </a>

          {/* Desktop Navigation Links */}
          <nav className="hidden lg:flex items-center gap-6 text-sm font-semibold text-slate-700 dark:text-slate-200">
            <a href="#tools" className="hover:text-crimson-600 dark:hover:text-crimson-400 transition-colors">
              16 PDF Tools
            </a>
            <a href="#simulator" className="hover:text-crimson-600 dark:hover:text-crimson-400 transition-colors">
              Live Mode
            </a>
            <a href="#screenshots" className="hover:text-crimson-600 dark:hover:text-crimson-400 transition-colors">
              App Screenshots
            </a>
            <a href="#privacy" className="hover:text-crimson-600 dark:hover:text-crimson-400 transition-colors">
              Privacy &amp; Architecture
            </a>
            <a href="#support" className="hover:text-crimson-600 dark:hover:text-crimson-400 transition-colors">
              Support PDFly
            </a>
            <a href="#faq" className="hover:text-crimson-600 dark:hover:text-crimson-400 transition-colors">
              FAQ
            </a>
          </nav>

          {/* Right Actions */}
          <div className="hidden md:flex items-center gap-2.5">
            <button
              type="button"
              onClick={() => setIsDark(!isDark)}
              aria-label={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-bold border border-slate-300 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-100 hover:border-crimson-500 transition-colors"
            >
              {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4" />}
              <span>{isDark ? 'Light' : 'Dark'}</span>
            </button>

            <a
              href={GITHUB_REPO_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-bold border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 hover:border-crimson-500 transition-colors"
            >
              <Github className="w-4 h-4" />
              <span>GitHub</span>
            </a>

            <a
              href={DIRECT_APK_DOWNLOAD_URL}
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold bg-crimson-700 hover:bg-crimson-800 text-white shadow-sm transition-colors"
            >
              <Download className="w-4 h-4" />
              <span>Download APK</span>
            </a>
          </div>

          {/* Mobile Hamburger & Theme Button */}
          <div className="flex items-center gap-2 md:hidden">
            <button
              type="button"
              onClick={() => setIsDark(!isDark)}
              aria-label="Toggle theme"
              className="p-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-100"
            >
              {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4" />}
            </button>
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              aria-label="Toggle navigation menu"
              className="p-2 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-100"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Drawer */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-[#131C31] px-4 py-4 space-y-3">
            <div className="flex flex-col space-y-2 text-sm font-semibold text-slate-800 dark:text-slate-100">
              <a href="#tools" onClick={() => setMobileMenuOpen(false)} className="py-1.5">
                16 PDF Tools
              </a>
              <a href="#simulator" onClick={() => setMobileMenuOpen(false)} className="py-1.5">
                Live Mode
              </a>
              <a href="#screenshots" onClick={() => setMobileMenuOpen(false)} className="py-1.5">
                App Screenshots
              </a>
              <a href="#privacy" onClick={() => setMobileMenuOpen(false)} className="py-1.5">
                Privacy &amp; Architecture
              </a>
              <a href="#support" onClick={() => setMobileMenuOpen(false)} className="py-1.5">
                Support PDFly
              </a>
              <a href="#faq" onClick={() => setMobileMenuOpen(false)} className="py-1.5">
                FAQ
              </a>
            </div>
            <div className="pt-2 flex gap-2">
              <a
                href={GITHUB_REPO_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="flex-1 inline-flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold border border-slate-300 dark:border-slate-700 text-slate-900 dark:text-white"
              >
                <Github className="w-4 h-4" />
                <span>GitHub Repo</span>
              </a>
              <a
                href={DIRECT_APK_DOWNLOAD_URL}
                className="flex-1 inline-flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold bg-crimson-700 text-white"
              >
                <Download className="w-4 h-4" />
                <span>Download APK</span>
              </a>
            </div>
          </div>
        )}
      </header>

      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 sm:pt-16 sm:pb-24 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          {/* Hero Copy */}
          <motion.div
            initial={shouldReduceMotion ? false : { opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="lg:col-span-7 space-y-6"
          >
            <div className="inline-flex flex-wrap items-center gap-2 px-3.5 py-1.5 rounded-full text-xs font-bold bg-crimson-100 text-crimson-900 dark:bg-crimson-950/90 dark:text-crimson-200 border border-crimson-300 dark:border-crimson-800">
              <Sparkles className="w-3.5 h-3.5" />
              <span>OPEN-SOURCE ANDROID PDF STUDIO • v1.0.0 RELEASE</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-display font-bold tracking-tight text-slate-950 dark:text-white leading-[1.08]">
              Your Complete{' '}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-crimson-600 via-rose-500 to-indigo-600 dark:from-crimson-400 dark:via-rose-400 dark:to-indigo-400">
                PDF Studio
              </span>{' '}
              for Android.
            </h1>

            <p className="text-base sm:text-lg text-slate-700 dark:text-slate-200 leading-relaxed max-w-2xl">
              <strong>PDFly</strong> is a production-ready native Android PDF editor and toolkit built with{' '}
              <strong>Kotlin</strong>, <strong>Jetpack Compose</strong>, <strong>Material Design 3</strong>,{' '}
              <strong>CameraX</strong>, and Android’s native <strong>PDF 1.4 engine</strong>. Read, annotate, sign,
              merge, split, compress, organize, scan, and convert documents locally on your device with{' '}
              <strong>zero cloud uploads</strong>, <strong>zero subscriptions</strong>, and{' '}
              <strong>zero advertisements</strong>.
            </p>

            {/* Primary CTA Buttons */}
            <div className="flex flex-wrap items-center gap-3.5 pt-2">
              <a
                href={DIRECT_APK_DOWNLOAD_URL}
                className="inline-flex items-center gap-2.5 px-6 py-3.5 rounded-2xl font-bold text-sm sm:text-base bg-crimson-700 hover:bg-crimson-800 text-white shadow-lg shadow-crimson-700/20 transition-all"
              >
                <Download className="w-5 h-5" />
                <span>Download APK (v1.0.0)</span>
              </a>

              <a
                href={GITHUB_RELEASES_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2.5 px-5 py-3.5 rounded-2xl font-bold text-sm sm:text-base bg-white dark:bg-[#131C31] text-slate-900 dark:text-white border border-slate-300 dark:border-slate-700 hover:border-crimson-500 transition-all"
              >
                <Github className="w-5 h-5" />
                <span>GitHub Release Notes</span>
              </a>

              <a
                href="#simulator"
                className="inline-flex items-center gap-2 px-4 py-3.5 rounded-2xl font-bold text-sm text-crimson-700 dark:text-crimson-300 hover:underline transition-colors"
              >
                <span>Try Live Mode Simulator →</span>
              </a>
            </div>

            {/* Technical Trust Pillars */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-4">
              {[
                { label: '16 PDF Tools', sub: 'Unlocked forever' },
                { label: '0 Cloud Calls', sub: 'No INTERNET perm' },
                { label: 'Kotlin + M3', sub: 'Jetpack Compose' },
                { label: 'MIT Licensed', sub: 'By CodeByArnob' }
              ].map((stat) => (
                <div
                  key={stat.label}
                  className="rounded-2xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-800 p-3.5"
                >
                  <div className="font-display font-bold text-base sm:text-lg text-slate-950 dark:text-white">
                    {stat.label}
                  </div>
                  <div className="text-xs text-slate-600 dark:text-slate-300 font-medium">{stat.sub}</div>
                </div>
              ))}
            </div>
          </motion.div>

          {/* Hero Visual: Choreographed Android Device Frame + Quick Screen Switcher */}
          <motion.div
            initial={shouldReduceMotion ? false : { opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="lg:col-span-5 relative flex flex-col items-center"
          >
            <div className="relative w-full max-w-[330px]">
              {/* Glow Backdrop */}
              <div
                className="absolute -inset-4 bg-gradient-to-tr from-crimson-600/25 via-indigo-600/20 to-emerald-500/20 rounded-[48px] blur-2xl pointer-events-none"
                aria-hidden="true"
              />
              {/* Main Device Screenshot */}
              <div className="relative rounded-[36px] border-4 border-slate-900 dark:border-slate-700 bg-slate-900 shadow-2xl overflow-hidden">
                <img
                  src={heroScreenshot.imagePath}
                  alt={heroScreenshot.title}
                  className="w-full h-auto block"
                />
              </div>

              {/* Floating Pill 1: Zero Internet Permission */}
              <div className="hidden sm:flex items-center gap-2.5 absolute -left-10 top-14 rounded-2xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 px-3.5 py-2.5 shadow-xl">
                <div className="w-8 h-8 rounded-xl bg-emerald-100 dark:bg-emerald-950 flex items-center justify-center text-emerald-700 dark:text-emerald-400">
                  <Shield className="w-4 h-4" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-950 dark:text-white">100% Offline Engine</div>
                  <div className="text-[11px] text-slate-600 dark:text-slate-300">Zero cloud uploads</div>
                </div>
              </div>

              {/* Floating Pill 2: Native ISO PDF 1.4 */}
              <div className="hidden sm:flex items-center gap-2.5 absolute -right-8 bottom-20 rounded-2xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 px-3.5 py-2.5 shadow-xl">
                <div className="w-8 h-8 rounded-xl bg-crimson-100 dark:bg-crimson-950 flex items-center justify-center text-crimson-700 dark:text-crimson-400">
                  <FileText className="w-4 h-4" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-950 dark:text-white">ISO PDF 1.4 Codec</div>
                  <div className="text-[11px] text-slate-600 dark:text-slate-300">Vector ink &amp; signatures</div>
                </div>
              </div>
            </div>

            {/* Hero Screen Quick Switcher */}
            <div className="flex flex-wrap justify-center gap-1.5 mt-4">
              {[
                { idx: 0, label: 'Dashboard' },
                { idx: 2, label: 'Merge' },
                { idx: 5, label: 'Scanner' },
                { idx: 8, label: 'Dark Mode' }
              ].map((tab) => (
                <button
                  key={tab.idx}
                  type="button"
                  onClick={() => setHeroScreenIndex(tab.idx)}
                  className={`px-3 py-1 rounded-full text-xs font-bold transition-all ${
                    heroScreenIndex === tab.idx
                      ? 'bg-crimson-700 text-white shadow-sm'
                      : 'bg-white dark:bg-[#131C31] text-slate-700 dark:text-slate-200 border border-slate-300 dark:border-slate-700 hover:border-crimson-500'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          </motion.div>
        </div>
      </section>

      {/* Complete 16-Tool Catalog Section */}
      <section id="tools" className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-10 gap-6">
          <div className="space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold bg-indigo-100 text-indigo-900 dark:bg-indigo-950 dark:text-indigo-200 border border-indigo-300 dark:border-indigo-800">
              <Layers className="w-3.5 h-3.5" />
              <span>COMPLETE ON-DEVICE PDF TOOLKIT</span>
            </div>
            <h2 className="text-3xl sm:text-4xl font-display font-bold text-slate-950 dark:text-white">
              16 Real, Working Android PDF Tools
            </h2>
            <p className="text-slate-700 dark:text-slate-200 max-w-2xl text-sm sm:text-base">
              Every tool below is genuinely implemented in Kotlin inside{' '}
              <code className="font-mono text-xs bg-slate-200 dark:bg-slate-800 px-1.5 py-0.5 rounded font-bold">
                PdfProcessingEngine.kt
              </code>{' '}
              and verified with automated Robolectric test suites.
            </p>
          </div>

          {/* Search Input */}
          <div className="relative w-full md:w-80">
            <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="search"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search 16 tools (e.g. Merge, Scan)..."
              aria-label="Search PDF tools"
              className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 text-sm text-slate-900 dark:text-white placeholder:text-slate-500 focus:outline-none focus:border-crimson-600"
            />
          </div>
        </div>

        {/* Category Filter Pills */}
        <div className="flex flex-wrap gap-2 mb-8">
          {TOOL_CATEGORIES.map((cat) => {
            const active = selectedCategory === cat.id;
            return (
              <button
                key={cat.id}
                type="button"
                onClick={() => setSelectedCategory(cat.id)}
                className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition-all ${
                  active
                    ? 'bg-crimson-700 text-white shadow-sm'
                    : 'bg-white dark:bg-[#131C31] text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700 hover:border-crimson-500'
                }`}
              >
                {cat.label}
              </button>
            );
          })}
        </div>

        {/* Tools Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {filteredTools.map((tool) => (
            <div
              key={tool.id}
              className="rounded-2xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-800 p-5 flex flex-col justify-between hover:border-crimson-500 dark:hover:border-crimson-500 transition-all shadow-sm hover:shadow-md"
            >
              <div className="space-y-3">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-slate-600 dark:text-slate-300">
                    {tool.categoryLabel}
                  </span>
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-crimson-100 text-crimson-900 dark:bg-crimson-950 dark:text-crimson-200 border border-crimson-300 dark:border-crimson-800">
                    {tool.badge}
                  </span>
                </div>

                <h3 className="text-lg font-display font-bold text-slate-950 dark:text-white">
                  {tool.title}
                </h3>

                <p className="text-xs sm:text-sm text-slate-700 dark:text-slate-200 leading-relaxed">
                  {tool.subtitle}
                </p>
              </div>

              <ul className="mt-4 pt-3 border-t border-slate-200 dark:border-slate-800 space-y-1.5">
                {tool.highlights.map((item) => (
                  <li
                    key={item}
                    className="flex items-center gap-2 text-xs font-semibold text-slate-800 dark:text-slate-200"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400 shrink-0" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </section>

      {/* Interactive Live Mode Simulator Section */}
      <section id="simulator" className="py-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="space-y-3 mb-8 text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold bg-crimson-100 text-crimson-900 dark:bg-crimson-950 dark:text-crimson-200 border border-crimson-300 dark:border-crimson-800">
            <PenTool className="w-3.5 h-3.5" />
            <span>INTERACTIVE LIVE MODE &amp; ENGINE TELEMETRY</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-display font-bold text-slate-950 dark:text-white">
            Experience PDFly Live Mode in Your Browser
          </h2>
          <p className="text-slate-700 dark:text-slate-200 text-sm sm:text-base">
            Interact with the 7 core workflows below or enable Live Auto-Cycle to see how PDFly processes PDFs locally
            in both Light and Dark themes.
          </p>
        </div>

        <InteractiveToolSimulator />
      </section>

      {/* Genuine Android Application Screenshots Showcase (All 9 Screens) */}
      <section id="screenshots" className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="space-y-3 mb-10 text-center max-w-3xl mx-auto">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-900 dark:bg-emerald-950 dark:text-emerald-200 border border-emerald-300 dark:border-emerald-800">
            <Smartphone className="w-3.5 h-3.5" />
            <span>GENUINE ANDROID APPLICATION SCREENSHOTS (9 SCREENS)</span>
          </div>
          <h2 className="text-3xl sm:text-4xl font-display font-bold text-slate-950 dark:text-white">
            Captured Directly From the Working Android App
          </h2>
          <p className="text-slate-700 dark:text-slate-200 text-sm sm:text-base">
            Inspect real screenshots of the Home Dashboard, PDF Reader, Merge, Split, Compressor, CameraX Scanner,
            Signature Studio, 16-Tool Catalog, and Obsidian Dark Mode Settings.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* Screen Selector List (9 Screens) */}
          <div className="lg:col-span-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-1 gap-3">
            {APP_SCREENSHOTS.map((item, idx) => {
              const isSelected = item.id === activeScreenshot.id;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => setActiveScreenshotId(item.id)}
                  className={`w-full text-left p-4 rounded-2xl border transition-all ${
                    isSelected
                      ? 'bg-white dark:bg-[#131C31] border-crimson-600 dark:border-crimson-500 shadow-md ring-2 ring-crimson-600/25'
                      : 'bg-white/85 dark:bg-[#131C31]/70 border-slate-300 dark:border-slate-800 hover:border-crimson-400'
                  }`}
                >
                  <div className="flex items-center justify-between gap-2 mb-1">
                    <span className="font-display font-bold text-sm sm:text-base text-slate-950 dark:text-white">
                      {idx + 1}. {item.title}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200 border border-slate-300 dark:border-slate-700 shrink-0">
                      {item.badge}
                    </span>
                  </div>
                  <p className="text-xs text-slate-700 dark:text-slate-200 leading-relaxed">
                    {item.description}
                  </p>
                </button>
              );
            })}
          </div>

          {/* Active Genuine PNG Screenshot Preview */}
          <div className="lg:col-span-6 lg:sticky lg:top-24 flex flex-col items-center">
            <div className="flex items-center justify-between w-full max-w-[340px] mb-3">
              <button
                type="button"
                onClick={() => stepActiveScreenshot(-1)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-bold bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-slate-200 hover:border-crimson-500"
              >
                <ChevronLeft className="w-4 h-4" />
                <span>Prev</span>
              </button>
              <span className="text-xs font-mono font-bold text-slate-700 dark:text-slate-200">
                {activeScreenshot.title}
              </span>
              <button
                type="button"
                onClick={() => stepActiveScreenshot(1)}
                className="inline-flex items-center gap-1 px-3 py-1.5 rounded-xl text-xs font-bold bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-slate-200 hover:border-crimson-500"
              >
                <span>Next</span>
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>

            <div className="w-full max-w-[340px] rounded-[36px] border-4 border-slate-900 dark:border-slate-700 bg-slate-900 shadow-2xl overflow-hidden">
              <img
                src={activeScreenshot.imagePath}
                alt={activeScreenshot.title}
                className="w-full h-auto block"
              />
            </div>

            <div className="flex flex-wrap justify-center gap-2 mt-5">
              {activeScreenshot.highlights.map((h) => (
                <span
                  key={h}
                  className="px-3 py-1 rounded-full text-xs font-bold bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-700 text-slate-800 dark:text-slate-200"
                >
                  ✓ {h}
                </span>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Privacy-First & Architecture Deep Dive */}
      <section id="privacy" className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="rounded-3xl bg-slate-900 dark:bg-[#131C31] border border-slate-800 text-white p-6 sm:p-10 lg:p-12 space-y-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            <div className="lg:col-span-7 space-y-4">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold bg-emerald-950 text-emerald-300 border border-emerald-700">
                <Lock className="w-3.5 h-3.5" />
                <span>ZERO-CLOUD PRIVACY &amp; CLEAN ARCHITECTURE</span>
              </div>
              <h2 className="text-3xl sm:text-4xl font-display font-bold">
                Why Your Documents Never Leave Your Phone
              </h2>
              <p className="text-slate-200 text-sm sm:text-base leading-relaxed">
                Most mobile PDF utilities upload sensitive tax forms, legal contracts, and IDs to remote servers for
                processing. <strong>PDFly</strong> is engineered from the ground up to run 100% on-device without even
                requesting the <code className="text-emerald-300 font-mono">INTERNET</code> permission in{' '}
                <code className="text-emerald-300 font-mono">AndroidManifest.xml</code>.
              </p>
            </div>

            <div className="lg:col-span-5">
              <div className="rounded-2xl bg-[#0B1120] border border-slate-800 p-5 font-mono text-xs space-y-2.5">
                <div className="text-slate-300 font-bold">Manifest &amp; Storage Security Model</div>
                <div className="text-emerald-400">
                  Zero INTERNET permission: 100% offline processing
                </div>
                <div className="text-slate-200">
                  Permission: android.permission.CAMERA (Scanner only)
                </div>
                <div className="text-crimson-400">
                  SAF Import: ActivityResultContracts.OpenDocument()
                </div>
                <div className="text-indigo-400">
                  SAF Export: ActivityResultContracts.CreateDocument()
                </div>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {ARCHITECTURE_LAYERS.map((layer) => (
              <div
                key={layer.title}
                className="rounded-2xl bg-[#0B1120]/90 border border-slate-800 p-6 space-y-2"
              >
                <div className="text-xs font-mono font-bold text-crimson-400">{layer.tech}</div>
                <h3 className="text-lg font-display font-bold text-white">{layer.title}</h3>
                <p className="text-xs sm:text-sm text-slate-200 leading-relaxed">{layer.description}</p>
              </div>
            ))}
          </div>

          {/* Build from Source Quick Commands */}
          <div className="rounded-2xl bg-[#0B1120] border border-slate-800 p-6 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
            <div className="space-y-1">
              <div className="inline-flex items-center gap-2 text-xs font-bold text-emerald-400 uppercase tracking-wider">
                <Terminal className="w-4 h-4" />
                <span>Reproducible Open-Source Build</span>
              </div>
              <h3 className="text-lg font-display font-bold">Clone, Test &amp; Assemble the APK Yourself</h3>
              <p className="text-xs sm:text-sm text-slate-300">
                Verified with Android Gradle Plugin 9.1, Kotlin 2.2, and Robolectric unit and CUJ test suites.
              </p>
            </div>
            <div className="font-mono text-xs bg-slate-900 border border-slate-800 rounded-xl p-4 text-slate-200 overflow-x-auto space-y-1">
              <div>git clone https://github.com/CodeByArnob/PDFly.git</div>
              <div>cd PDFly</div>
              <div>./gradlew :app:testDebugUnitTest :app:assembleRelease</div>
            </div>
          </div>
        </div>
      </section>

      {/* Voluntary Support PDFly Section */}
      <SupportDonationSection />

      {/* FAQ Section */}
      <section id="faq" className="py-16 px-4 sm:px-6 lg:px-8 max-w-4xl mx-auto">
        <div className="text-center space-y-3 mb-10">
          <h2 className="text-3xl sm:text-4xl font-display font-bold text-slate-950 dark:text-white">
            Frequently Asked Questions
          </h2>
          <p className="text-slate-700 dark:text-slate-200 text-sm sm:text-base">
            Honest answers about PDFly’s offline architecture, permissions, licensing, and releases.
          </p>
        </div>

        <div className="space-y-3">
          {FAQ_ITEMS.map((item, idx) => {
            const isOpen = openFaqIndex === idx;
            return (
              <div
                key={item.question}
                className="rounded-2xl bg-white dark:bg-[#131C31] border border-slate-300 dark:border-slate-800 overflow-hidden"
              >
                <button
                  type="button"
                  onClick={() => setOpenFaqIndex(isOpen ? null : idx)}
                  aria-expanded={isOpen}
                  className="w-full px-6 py-4 text-left flex items-center justify-between gap-4 font-display font-bold text-sm sm:text-base text-slate-950 dark:text-white"
                >
                  <span>{item.question}</span>
                  <ChevronDown
                    className={`w-5 h-5 text-slate-500 shrink-0 transition-transform ${
                      isOpen ? 'rotate-180 text-crimson-600' : ''
                    }`}
                  />
                </button>
                {isOpen && (
                  <div className="px-6 pb-5 text-xs sm:text-sm text-slate-700 dark:text-slate-200 leading-relaxed border-t border-slate-100 dark:border-slate-800/80 pt-3">
                    {item.answer}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-slate-300 dark:border-slate-800 bg-white dark:bg-[#0B1120] py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-start md:items-center justify-between gap-8">
          <div className="space-y-2 max-w-md">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-crimson-700 text-white font-display font-bold text-xs flex items-center justify-center">
                PDF
              </div>
              <span className="font-display font-bold text-lg text-slate-950 dark:text-white">
                PDFly: PDF Editor &amp; Toolkit
              </span>
            </div>
            <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-300">
              100% Free, Open-Source, Offline-First Android PDF Studio built with Kotlin &amp; Jetpack Compose.
              Maintained by{' '}
              <a
                href={DEVELOPER_GITHUB_URL}
                target="_blank"
                rel="noopener noreferrer"
                className="font-bold text-crimson-700 dark:text-crimson-400 hover:underline"
              >
                CodeByArnob
              </a>
              .
            </p>
          </div>

          <div className="flex flex-wrap gap-6 text-xs sm:text-sm font-semibold text-slate-700 dark:text-slate-200">
            <a
              href={GITHUB_REPO_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 hover:text-crimson-600"
            >
              <FolderGit2 className="w-4 h-4" />
              <span>Repository</span>
            </a>
            <a
              href={DIRECT_APK_DOWNLOAD_URL}
              className="inline-flex items-center gap-1.5 hover:text-crimson-600"
            >
              <Download className="w-4 h-4" />
              <span>Download APK</span>
            </a>
            <a
              href={GITHUB_RELEASES_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 hover:text-crimson-600"
            >
              <span>Release v1.0.0</span>
            </a>
            <a
              href={GITHUB_ISSUES_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 hover:text-crimson-600"
            >
              <span>Report Issue</span>
              <ExternalLink className="w-3.5 h-3.5" />
            </a>
            <a
              href={`${GITHUB_REPO_URL}/blob/main/LICENSE`}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 hover:text-crimson-600"
            >
              <span>MIT License</span>
            </a>
            <a
              href={DEVELOPER_GITHUB_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 hover:text-crimson-600"
            >
              <Heart className="w-4 h-4 text-crimson-600" />
              <span>@CodeByArnob</span>
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
