export interface PdfToolItem {
  id: string;
  title: string;
  subtitle: string;
  badge: string;
  category: 'read_edit' | 'organize' | 'convert_extract' | 'scan_create' | 'security_utils';
  categoryLabel: string;
  highlights: string[];
  accentColor: string;
}

export interface AppScreenshotItem {
  id: string;
  title: string;
  badge: string;
  description: string;
  imagePath: string;
  highlights: string[];
}

export interface CryptoTokenItem {
  symbol: 'USDT' | 'USDC';
  fullName: string;
  networkName: string;
  tokenStandard: string;
  chainId: number;
  receivingWalletAddress: string;
  contractAddress: string;
  decimals: number;
  explorerUrl: string;
  badgeColor: string;
}

export const GITHUB_REPO_URL = 'https://github.com/CodeByArnob/PDFly';
export const GITHUB_RELEASES_URL = 'https://github.com/CodeByArnob/PDFly/releases/tag/v1.0.0';
export const DIRECT_APK_DOWNLOAD_URL =
  'https://github.com/CodeByArnob/PDFly/releases/download/v1.0.0/PDFly-v1.0.0-release.apk';
export const GITHUB_ISSUES_URL = 'https://github.com/CodeByArnob/PDFly/issues';
export const DEVELOPER_GITHUB_URL = 'https://github.com/CodeByArnob';
export const DEVELOPER_WALLET_ADDRESS = '0x21b8f3cea6901820adfca5deb3be37e567ab0f97';

export const TOOL_CATEGORIES = [
  { id: 'all', label: 'All 16 Tools' },
  { id: 'read_edit', label: 'Read & Edit' },
  { id: 'organize', label: 'Organize PDF' },
  { id: 'convert_extract', label: 'Convert & Extract' },
  { id: 'scan_create', label: 'Scan & Stamp' },
  { id: 'security_utils', label: 'Security & Inspect' }
] as const;

export const PDFLY_TOOLS: PdfToolItem[] = [
  {
    id: 'reader',
    title: 'PDF Reader',
    subtitle: 'Read, zoom, search text, jump to pages, and manage bookmarks with crisp native rendering.',
    badge: 'Reader',
    category: 'read_edit',
    categoryLabel: 'Read & Edit',
    highlights: ['Pinch-to-zoom & page jump', 'Full-text keyword search', 'Persistent page bookmarks'],
    accentColor: 'crimson'
  },
  {
    id: 'annotate',
    title: 'PDF Annotation',
    subtitle: 'Draw freehand vector ink, highlight text, underline key clauses, and attach sticky notes.',
    badge: 'Markup',
    category: 'read_edit',
    categoryLabel: 'Read & Edit',
    highlights: ['Rotation-aware vector ink', 'Highlight & underline markup', 'Flattens directly into PDF 1.4'],
    accentColor: 'amber'
  },
  {
    id: 'signature',
    title: 'Add Signature',
    subtitle: 'Create handwritten or typed vector signatures and stamp them onto any PDF page offline.',
    badge: 'Sign',
    category: 'read_edit',
    categoryLabel: 'Read & Edit',
    highlights: ['Reusable signature vault', 'Custom ink colors & stroke width', 'Burned into exported PDF'],
    accentColor: 'indigo'
  },
  {
    id: 'merge',
    title: 'Merge PDF',
    subtitle: 'Combine multiple PDF documents in custom order into one cohesive file while preserving page dimensions.',
    badge: 'Combine',
    category: 'organize',
    categoryLabel: 'Organize PDF',
    highlights: ['Custom document ordering', 'Preserves mixed page sizes', 'Instant page count preview'],
    accentColor: 'indigo'
  },
  {
    id: 'split',
    title: 'Split PDF',
    subtitle: 'Extract custom page ranges (e.g., 1-3, 5), single pages, or burst every page into separate PDFs.',
    badge: 'Extract',
    category: 'organize',
    categoryLabel: 'Organize PDF',
    highlights: ['Custom range syntax (1-3, 5)', 'Single-page quick extraction', 'Every-page batch burst'],
    accentColor: 'sky'
  },
  {
    id: 'organize_pages',
    title: 'Page Organizer',
    subtitle: 'Reorder pages, rotate individual pages 90, 180, or 270 degrees, delete unwanted sheets, or extract selections.',
    badge: 'Organize',
    category: 'organize',
    categoryLabel: 'Organize PDF',
    highlights: ['Per-page 90/180/270 degree rotation', 'Visual page deletion & restore', 'Non-destructive export'],
    accentColor: 'violet'
  },
  {
    id: 'compress',
    title: 'Compress PDF',
    subtitle: 'Reduce PDF file size locally with Low (150 DPI), Medium (115 DPI), or High (85 DPI) presets.',
    badge: 'Optimize',
    category: 'organize',
    categoryLabel: 'Organize PDF',
    highlights: ['3 calibrated DPI & JPEG presets', 'Downsamples image streams safely', 'Transparent before/after size report'],
    accentColor: 'emerald'
  },
  {
    id: 'image_to_pdf',
    title: 'JPG / PNG to PDF',
    subtitle: 'Convert photos and graphics into formatted A4, US Letter, or Legal PDFs with custom margins.',
    badge: 'Convert',
    category: 'convert_extract',
    categoryLabel: 'Convert & Extract',
    highlights: ['A4, US Letter & Legal presets', 'Portrait & Landscape orientation', 'OOM-safe sampled bitmap decoding'],
    accentColor: 'teal'
  },
  {
    id: 'pdf_to_image',
    title: 'PDF to JPG / PNG',
    subtitle: 'Render and export PDF pages into high-resolution lossless PNG or compact JPG image files.',
    badge: 'Export',
    category: 'convert_extract',
    categoryLabel: 'Convert & Extract',
    highlights: ['Lossless PNG or compact JPG', 'High-DPI page rendering', 'Exports individual page images'],
    accentColor: 'sky'
  },
  {
    id: 'extract_text',
    title: 'Extract PDF Text',
    subtitle: 'Parse and export selectable PDF text streams into clean TXT files or structured summaries.',
    badge: 'Text',
    category: 'convert_extract',
    categoryLabel: 'Convert & Extract',
    highlights: ['Per-page text stream parser', 'Instant copy or TXT export', '100% offline text extraction'],
    accentColor: 'amber'
  },
  {
    id: 'extract_images',
    title: 'Extract Images',
    subtitle: 'Save rendered page visuals and embedded document graphics as standalone image files.',
    badge: 'Assets',
    category: 'convert_extract',
    categoryLabel: 'Convert & Extract',
    highlights: ['High-resolution asset capture', 'Batch page image export', 'Zero quality degradation'],
    accentColor: 'violet'
  },
  {
    id: 'scanner',
    title: 'Document Scanner',
    subtitle: 'Capture paper documents with CameraX, auto-crop edges, and apply crisp B&W or Magic Color filters.',
    badge: 'CameraX',
    category: 'scan_create',
    categoryLabel: 'Scan & Stamp',
    highlights: ['CameraX hardware viewfinder', 'B&W, Magic Color & Grayscale', 'Multi-page batch scan to PDF'],
    accentColor: 'crimson'
  },
  {
    id: 'watermark',
    title: 'PDF Watermark',
    subtitle: 'Stamp custom text watermarks across pages with adjustable opacity, font size, and diagonal or header placement.',
    badge: 'Stamp',
    category: 'scan_create',
    categoryLabel: 'Scan & Stamp',
    highlights: ['Diagonal, Header & Footer modes', 'Custom opacity & point size', 'Applied across all pages'],
    accentColor: 'rose'
  },
  {
    id: 'page_numbers',
    title: 'Add Page Numbers',
    subtitle: 'Insert formatted page numbering (Page X of Y, X / Y) on headers or footers across pages.',
    badge: 'Numbering',
    category: 'scan_create',
    categoryLabel: 'Scan & Stamp',
    highlights: ['4 numbering format styles', 'Configurable starting number', 'Header or footer alignment'],
    accentColor: 'indigo'
  },
  {
    id: 'security_lock_unlock',
    title: 'PDF Security & Unlock',
    subtitle: 'Apply local access protection metadata with SHA-256 verification or unlock authorized PDF documents.',
    badge: 'Security',
    category: 'security_utils',
    categoryLabel: 'Security & Inspect',
    highlights: ['SHA-256 password verification', 'Unlock authorized protected PDFs', 'Strict local key handling'],
    accentColor: 'emerald'
  },
  {
    id: 'metadata_viewer',
    title: 'PDF Metadata Inspector',
    subtitle: 'Inspect PDF version, exact page dimensions in points, SHA-256 checksum, encryption status, and properties.',
    badge: 'Inspect',
    category: 'security_utils',
    categoryLabel: 'Security & Inspect',
    highlights: ['SHA-256 file integrity hash', 'PDF version & page box dimensions', 'Author, Producer & timestamp audit'],
    accentColor: 'teal'
  }
];

export const APP_SCREENSHOTS: AppScreenshotItem[] = [
  {
    id: 'home',
    title: 'Home Dashboard',
    badge: 'Light Theme • WCAG AA+',
    description:
      'High-contrast Editorial Studio dashboard featuring instant access to all 16 offline tools, workspace storage telemetry, and zero-permission SAF file import.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/01-home-dashboard.png`,
    highlights: ['WCAG 2.2 AA+ verified contrast', 'Quick-launch 16 local PDF tools', 'Recent & Favorite workspace files']
  },
  {
    id: 'reader',
    title: 'PDF Reader & Markup Studio',
    badge: 'Native PdfRenderer',
    description:
      'Read multi-page PDFs with pinch-to-zoom, keyword search, page rotation, bookmarks, and real-time annotation overlays.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/02-pdf-reader.png`,
    highlights: ['Pinch-to-zoom & page rotation', 'Full-text keyword search bar', 'Persistent page bookmarks']
  },
  {
    id: 'merge',
    title: 'PDF Merge & Live Pipeline',
    badge: 'Multi-PDF Combine',
    description:
      'Combine 2 or more PDF files in custom order with a non-overlapping Live Merge Pipeline visualizer showing source page counts and combined output size.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/03-pdf-merge.png`,
    highlights: ['Live convergence stream preview', 'Custom document queue ordering', 'Preserves mixed page sizes']
  },
  {
    id: 'split',
    title: 'PDF Split & Range Extractor',
    badge: 'Interactive Page Deck',
    description:
      'Tap individual pages in the interactive separation deck or enter custom ranges (1-3, 5) to extract pages into standalone PDF documents.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/04-pdf-split.png`,
    highlights: ['Interactive page toggle cards', 'Range, Single Page & Burst modes', 'Bounds-checked page validation']
  },
  {
    id: 'compressor',
    title: 'PDF Compressor & Stream Profile',
    badge: 'Smart DPI Optimization',
    description:
      'Reduce PDF file size locally using calibrated Low (150 DPI), Medium (115 DPI), or High (85 DPI) raster optimization presets.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/05-pdf-compressor.png`,
    highlights: ['Live bitrate density visualizer', '150 / 115 / 85 DPI presets', 'OOM-safe bitmap downsampling']
  },
  {
    id: 'scanner',
    title: 'CameraX Document Scanner',
    badge: 'Hardware CameraX',
    description:
      'Scan physical documents with CameraX, adjust perspective crop brackets, and apply B&W Document, Magic Enhance, or Grayscale filters.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/06-pdf-scanner.png`,
    highlights: ['Animated crop brackets & scan beam', 'B&W, Magic Enhance & Grayscale', 'Multi-page batch scan to PDF']
  },
  {
    id: 'signature',
    title: 'PDF Signature Studio',
    badge: 'Vector Ink Vault',
    description:
      'Draw handwritten vector signatures on the responsive ink pad, save reusable signatures locally in Room DB, and flatten them onto any PDF page.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/07-pdf-signature.png`,
    highlights: ['Handwritten vector signature pad', 'Reusable local signature vault', 'Flattens directly into PDF 1.4']
  },
  {
    id: 'other_tools',
    title: 'Complete 16-Tool Catalog',
    badge: '5 Logical Categories',
    description:
      'Browse all 16 PDF tools including Page Organizer, Watermark, Page Numbers, PDF to Image, Extract Text, Security Lock/Unlock, and Metadata Inspector.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/08-other-tools.png`,
    highlights: ['All 16 tools unlocked forever', 'Category filter chips & search', 'Zero subscriptions or paywalls']
  },
  {
    id: 'settings',
    title: 'Settings & Theme Options',
    badge: 'Obsidian Dark Mode',
    description:
      'Customize Light, Dark, or System themes, toggle Reduced Motion accessibility mode, set default compression/paper presets, and manage local cache.',
    imagePath: `${import.meta.env.BASE_URL}screenshots/09-settings-theme.png`,
    highlights: ['Light, Dark & System themes', 'Reduced Motion accessibility toggle', 'One-tap temporary cache cleanup']
  }
];

export const SUPPORTED_CRYPTO_TOKENS: CryptoTokenItem[] = [
  {
    symbol: 'USDT',
    fullName: 'Tether USD (BSC-USD)',
    networkName: 'BNB Smart Chain (BEP20)',
    tokenStandard: 'BEP-20',
    chainId: 56,
    receivingWalletAddress: DEVELOPER_WALLET_ADDRESS,
    contractAddress: '0x55d398326f99059fF775485246999027B3197955',
    decimals: 18,
    explorerUrl: 'https://bscscan.com/token/0x55d398326f99059fF775485246999027B3197955',
    badgeColor: '#0D9488'
  },
  {
    symbol: 'USDC',
    fullName: 'USD Coin (Binance-Peg USDC)',
    networkName: 'BNB Smart Chain (BEP20)',
    tokenStandard: 'BEP-20',
    chainId: 56,
    receivingWalletAddress: DEVELOPER_WALLET_ADDRESS,
    contractAddress: '0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d',
    decimals: 18,
    explorerUrl: 'https://bscscan.com/token/0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d',
    badgeColor: '#1D4ED8'
  }
];

export const ARCHITECTURE_LAYERS = [
  {
    title: 'Jetpack Compose + Material 3 UI Layer',
    tech: 'Kotlin 2.2 • Compose BOM • WCAG 2.2 AA+ Light & Obsidian Dark Themes',
    description:
      'Choreographed entrance animations, functional motion workspaces, adaptive layouts, and full Reduced Motion accessibility support.'
  },
  {
    title: 'Native On-Device PDF Processing Engine',
    tech: 'Android PdfRenderer • PdfDocument • Custom IsoPdf14Codec',
    description:
      'Renders, merges, splits, compresses, watermarks, numbers, and flattens vector ink and signatures locally in native memory with OOM-safe bitmap recycling.'
  },
  {
    title: 'Zero-Permission Storage & CameraX Pipeline',
    tech: 'Android SAF (OpenDocument / CreateDocument) • Photo Picker • CameraX 1.5',
    description:
      'Uses system document pickers instead of broad storage permissions, and requests Camera permission strictly when launching the Document Scanner.'
  },
  {
    title: 'Reactive Local Persistence',
    tech: 'Room Database 2.7 • Jetpack DataStore Preferences • Kotlin Coroutines & StateFlow',
    description:
      'Persists document metadata, processing history, page annotations, saved vector signatures, and user preferences strictly on-device.'
  }
];

export const FAQ_ITEMS = [
  {
    question: 'Is PDFly really 100% free with no subscriptions or locked tools?',
    answer:
      'Yes. Every single one of PDFly’s 16 PDF tools (including PDF Reader, Merge, Split, Compress, Page Organizer, CameraX Document Scanner, Annotate, Signature, Watermark, Page Numbers, Text/Image Extraction, Security, and Metadata Inspector) is unlocked for all users forever. There are no trials, subscriptions, ads, or paywalls.'
  },
  {
    question: 'Does PDFly upload my PDF documents to any server or cloud API?',
    answer:
      'Never. PDFly does not even request the android.permission.INTERNET permission in its AndroidManifest.xml. All PDF rendering, merging, splitting, compression, scanning, and signature flattening happen 100% locally on your Android device.'
  },
  {
    question: 'What Android permissions does PDFly require?',
    answer:
      'PDFly follows a strict least-privilege security model. Opening and saving PDFs or images uses the zero-permission Android Storage Access Framework (SAF) and Android Photo Picker. The only runtime permission in the entire app is CAMERA, which is requested only when you open the Document Scanner.'
  },
  {
    question: 'How can I download and install PDFly on my Android device?',
    answer:
      'You can download the signed production APK directly from the official GitHub Releases page of CodeByArnob/PDFly using the Download APK button on this site, or clone the repository and build the APK yourself in Android Studio.'
  },
  {
    question: 'How does voluntary developer support work?',
    answer:
      'Supporting PDFly is 100% optional and never gates or unlocks any app feature. Users who wish to support open-source development can contribute USDT (BEP20) or USDC (BEP20) on BNB Smart Chain (Chain ID 56) to the verified developer wallet address.'
  }
];
