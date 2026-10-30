package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.BuildConfig
import java.util.UUID

/**
 * Logical categories for organizing PDFly's 16 local PDF tools on the Home Dashboard
 * and All Tools catalog screen without overcrowding.
 */
enum class PdfToolCategory(
    val id: String,
    val title: String,
    val subtitle: String
) {
    READ_AND_EDIT(
        id = "read_edit",
        title = "Read & Edit",
        subtitle = "View, search, annotate, highlight, and sign PDF documents"
    ),
    ORGANIZE_PDF(
        id = "organize",
        title = "Organize PDF",
        subtitle = "Merge, split, reorder, rotate, delete pages, and compress files"
    ),
    CONVERT_AND_EXTRACT(
        id = "convert_extract",
        title = "Convert & Extract",
        subtitle = "Convert between PDF and JPG/PNG, or extract text and images"
    ),
    SCAN_AND_CREATE(
        id = "scan_create",
        title = "Scan & Stamp",
        subtitle = "Scan paper documents, apply watermarks, and stamp page numbers"
    ),
    SECURITY_AND_UTILITIES(
        id = "security_utils",
        title = "Security & Inspect",
        subtitle = "Protect or unlock authorized PDFs and inspect technical metadata"
    )
}

/**
 * Complete catalog of 16 real, working, on-device PDF tools in PDFly.
 * Every tool has a full, non-truncated description that wraps cleanly across 2 lines.
 */
enum class PdfToolType(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val category: PdfToolCategory = PdfToolCategory.READ_AND_EDIT
) {
    // 1. Read & Edit
    READER(
        id = "reader",
        title = "PDF Reader",
        subtitle = "Read, zoom, search text, jump to pages, and manage bookmarks",
        badge = "Reader",
        category = PdfToolCategory.READ_AND_EDIT
    ),
    ANNOTATE(
        id = "annotate",
        title = "PDF Annotation",
        subtitle = "Draw freehand ink, highlight text, underline, and add page notes",
        badge = "Markup",
        category = PdfToolCategory.READ_AND_EDIT
    ),
    SIGNATURE(
        id = "signature",
        title = "Add Signature",
        subtitle = "Create handwritten or typed signatures and stamp onto any page",
        badge = "Sign",
        category = PdfToolCategory.READ_AND_EDIT
    ),

    // 2. Organize PDF
    MERGE(
        id = "merge",
        title = "Merge PDF",
        subtitle = "Combine multiple PDF documents in custom order into one file",
        badge = "Combine",
        category = PdfToolCategory.ORGANIZE_PDF
    ),
    SPLIT(
        id = "split",
        title = "Split PDF",
        subtitle = "Extract custom page ranges or split every page into separate PDFs",
        badge = "Extract",
        category = PdfToolCategory.ORGANIZE_PDF
    ),
    ORGANIZE_PAGES(
        id = "organize_pages",
        title = "Page Organizer",
        subtitle = "Reorder pages, rotate 90°/180°/270°, delete, or extract pages",
        badge = "Organize",
        category = PdfToolCategory.ORGANIZE_PDF
    ),
    COMPRESS(
        id = "compress",
        title = "Compress PDF",
        subtitle = "Reduce PDF file size locally with Low, Medium, or High presets",
        badge = "Optimize",
        category = PdfToolCategory.ORGANIZE_PDF
    ),

    // 3. Convert & Extract
    IMAGE_TO_PDF(
        id = "image_to_pdf",
        title = "JPG / PNG to PDF",
        subtitle = "Convert photos and graphics into formatted A4 or Letter PDFs",
        badge = "Convert",
        category = PdfToolCategory.CONVERT_AND_EXTRACT
    ),
    PDF_TO_IMAGE(
        id = "pdf_to_image",
        title = "PDF to JPG / PNG",
        subtitle = "Render and export PDF pages into high-resolution PNG or JPG images",
        badge = "Export",
        category = PdfToolCategory.CONVERT_AND_EXTRACT
    ),
    EXTRACT_TEXT(
        id = "extract_text",
        title = "Extract PDF Text",
        subtitle = "Parse and export selectable text streams into TXT or summary PDF",
        badge = "Text",
        category = PdfToolCategory.CONVERT_AND_EXTRACT
    ),
    EXTRACT_IMAGES(
        id = "extract_images",
        title = "Extract Images",
        subtitle = "Save page visuals and embedded graphics as standalone image files",
        badge = "Assets",
        category = PdfToolCategory.CONVERT_AND_EXTRACT
    ),

    // 4. Scan & Stamp
    SCANNER(
        id = "scanner",
        title = "Document Scanner",
        subtitle = "Capture pages with CameraX, auto-crop edges, and apply B&W filters",
        badge = "CameraX",
        category = PdfToolCategory.SCAN_AND_CREATE
    ),
    WATERMARK(
        id = "watermark",
        title = "PDF Watermark",
        subtitle = "Stamp custom text watermarks with adjustable opacity and position",
        badge = "Stamp",
        category = PdfToolCategory.SCAN_AND_CREATE
    ),
    PAGE_NUMBERS(
        id = "page_numbers",
        title = "Add Page Numbers",
        subtitle = "Insert formatted page numbering on headers or footers across pages",
        badge = "Numbering",
        category = PdfToolCategory.SCAN_AND_CREATE
    ),

    // 5. Security & Utilities
    SECURITY_LOCK_UNLOCK(
        id = "security_lock_unlock",
        title = "PDF Security & Unlock",
        subtitle = "Apply local access protection or unlock authorized PDF documents",
        badge = "Security",
        category = PdfToolCategory.SECURITY_AND_UTILITIES
    ),
    METADATA_VIEWER(
        id = "metadata_viewer",
        title = "PDF Metadata Inspector",
        subtitle = "Inspect PDF version, page dimensions, SHA-256 hash, and properties",
        badge = "Inspect",
        category = PdfToolCategory.SECURITY_AND_UTILITIES
    )
}

enum class CompressionPreset(
    val label: String,
    val description: String,
    val dpi: Int,
    val jpegQuality: Int,
    val estimatedSavingsNote: String
) {
    LOW(
        label = "Low Compression (High Quality)",
        description = "Preserves crisp typography and fine graphics at 150 DPI.",
        dpi = 150,
        jpegQuality = 84,
        estimatedSavingsNote = "~15–30% smaller for image-heavy PDFs"
    ),
    MEDIUM(
        label = "Medium Compression (Balanced)",
        description = "Balanced 115 DPI rendering & 68% JPEG encoding for email sharing.",
        dpi = 115,
        jpegQuality = 68,
        estimatedSavingsNote = "~35–55% smaller for scanned PDFs"
    ),
    HIGH(
        label = "High Compression (Smallest Size)",
        description = "Compact 85 DPI downsampling for strict attachment size limits.",
        dpi = 85,
        jpegQuality = 50,
        estimatedSavingsNote = "~55–75% smaller for photo-heavy PDFs"
    )
}

enum class SplitMode(val label: String, val helper: String) {
    CUSTOM_RANGE("Extract Page Range", "Enter ranges like 1-3, 5 to create a single extracted PDF"),
    SINGLE_PAGE("Extract Single Page", "Pick a single page number to export as its own PDF"),
    EVERY_PAGE("Split Every Page into Separate PDFs", "Generates one individual PDF file per page")
}

enum class PaperSizePreset(val label: String, val widthPt: Int, val heightPt: Int) {
    A4("A4 (210 × 297 mm)", 595, 842),
    LETTER("US Letter (8.5 × 11 in)", 612, 792),
    LEGAL("US Legal (8.5 × 14 in)", 612, 1008)
}

enum class PageOrientation(val label: String) {
    PORTRAIT("Portrait"),
    LANDSCAPE("Landscape")
}

enum class PageMarginPreset(val label: String, val marginPt: Int) {
    NONE("No Margin (0 pt)", 0),
    SMALL("Compact (18 pt)", 18),
    NORMAL("Standard (36 pt)", 36),
    WIDE("Wide (54 pt)", 54)
}

enum class ScanFilterPreset(val label: String, val description: String) {
    ORIGINAL("Original", "Natural camera colors"),
    MAGIC_ENHANCE("Magic Color", "High-contrast readable color"),
    GRAYSCALE("Grayscale", "Clean monochrome tones"),
    BW_SHARP("B&W Document", "Crisp black text on white paper")
}

data class ScannedPageDraft(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val sourceType: String,
    val rotationDegrees: Int = 0,
    val filter: ScanFilterPreset = ScanFilterPreset.BW_SHARP,
    val brightness: Float = 0f,
    val contrast: Float = 1.15f,
    val cropInsetPercent: Float = 0.04f,
    val bitmapPath: String? = null,
    val contentLines: List<String> = emptyList()
)

enum class AnnotationType(val label: String) {
    HIGHLIGHT("Highlight"),
    UNDERLINE("Underline"),
    FREEHAND("Freehand Ink"),
    TEXT_NOTE("Sticky Note"),
    SIGNATURE("Signature")
}

enum class AppThemeMode(val label: String) {
    SYSTEM("Follow System"),
    LIGHT("Light Theme"),
    DARK("Dark Theme")
}

/**
 * Page state item used in the PDF Page Organizer tool.
 */
data class PageOrganizeItem(
    val id: String = UUID.randomUUID().toString(),
    val originalPageIndex: Int,
    val rotationDegrees: Int = 0,
    val isDeleted: Boolean = false,
    val isSelectedForExtract: Boolean = false
)

enum class WatermarkPosition(val label: String) {
    DIAGONAL_CENTER("Diagonal Center"),
    TOP_HEADER("Top Header"),
    BOTTOM_FOOTER("Bottom Footer")
}

data class WatermarkConfig(
    val text: String = "CONFIDENTIAL • DO NOT DISTRIBUTE",
    val opacity: Float = 0.28f,
    val fontSizePt: Float = 32f,
    val colorArgb: Long = 0xFFDC2626,
    val position: WatermarkPosition = WatermarkPosition.DIAGONAL_CENTER
)

enum class PageNumberPosition(val label: String) {
    BOTTOM_CENTER("Bottom Center"),
    BOTTOM_RIGHT("Bottom Right"),
    TOP_RIGHT("Top Right")
}

enum class PageNumberFormat(val label: String, val example: String) {
    PAGE_X_OF_Y("Page X of Y", "Page 1 of 10"),
    X_SLASH_Y("X / Y", "1 / 10"),
    DASH_X_DASH("- X -", "- 1 -"),
    NUMBER_ONLY("X", "1")
}

data class PageNumberConfig(
    val position: PageNumberPosition = PageNumberPosition.BOTTOM_RIGHT,
    val format: PageNumberFormat = PageNumberFormat.PAGE_X_OF_Y,
    val startNumber: Int = 1,
    val fontSizePt: Float = 11f
)

enum class ImageExportFormat(val label: String, val extension: String, val mimeType: String) {
    PNG("PNG (Lossless Crisp Text)", "png", "image/png"),
    JPG("JPG (Compact Photo Size)", "jpg", "image/jpeg")
}

data class ExportedImageItem(
    val pageIndex: Int,
    val filePath: String,
    val fileName: String,
    val widthPx: Int,
    val heightPx: Int,
    val sizeBytes: Long
)

data class PdfMetadataReport(
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val pdfVersion: String,
    val pageCount: Int,
    val pageDimensionsSummary: String,
    val title: String,
    val author: String,
    val creator: String,
    val producer: String,
    val creationDate: String,
    val isEncrypted: Boolean,
    val sha256Checksum: String,
    val lastModifiedTimestamp: Long
)

data class ExtractedPdfTextResult(
    val fileName: String,
    val pageCount: Int,
    val pageTexts: List<String>,
    val combinedText: String,
    val exportedTxtFilePath: String? = null
)

enum class DocumentSortOption(val label: String) {
    RECENT("Recent"),
    NAME_ASC("Name (A–Z)"),
    SIZE_DESC("Largest Size"),
    PAGES_DESC("Most Pages")
}

/**
 * Internal compile-time build configuration resolver.
 * Never exposed to end users in the application UI.
 * Determines automatically at build time whether optional cryptocurrency donations
 * are enabled for the compiled binary.
 */
object InternalDistributionConfig {
    const val CHANNEL_DIRECT_APK = "DIRECT_APK"
    const val CHANNEL_GOOGLE_PLAY = "GOOGLE_PLAY"

    val compiledChannel: String
        get() = BuildConfig.DISTRIBUTION_CHANNEL

    val isCryptoDonationEnabled: Boolean
        get() = isCryptoDonationEnabledForBuild(
            channel = BuildConfig.DISTRIBUTION_CHANNEL,
            enableFlag = BuildConfig.ENABLE_CRYPTO_DONATIONS
        )

    fun isCryptoDonationEnabledForBuild(channel: String, enableFlag: Boolean): Boolean {
        return enableFlag && !channel.equals(CHANNEL_GOOGLE_PLAY, ignoreCase = true)
    }
}

/**
 * Verified configuration for voluntary cryptocurrency donations on BNB Smart Chain (BEP-20).
 */
data class CryptoTokenConfig(
    val symbol: String,
    val fullName: String,
    val networkName: String,
    val tokenStandard: String,
    val chainId: Int,
    val receivingWalletAddress: String,
    val contractAddress: String,
    val decimals: Int,
    val brandColor: Color = Color(0xFF0D9488),
    val explorerNote: String
) {
    /**
     * Standard EIP-681 token transfer URI on BNB Smart Chain (Chain ID 56) for compatible wallets,
     * while the primary QR code encodes the raw EVM receiving wallet address for universal scanner compatibility.
     */
    val paymentQrPayload: String
        get() = "ethereum:$contractAddress@$chainId/transfer?address=$receivingWalletAddress"
}

object OfficialDonationCatalog {
    const val DEVELOPER_WALLET_ADDRESS = BuildConfig.DONATION_WALLET_ADDRESS

    val supportedTokens: List<CryptoTokenConfig> = listOf(
        CryptoTokenConfig(
            symbol = "USDT",
            fullName = "Tether USD",
            networkName = "BNB Smart Chain (BSC)",
            tokenStandard = "BEP-20",
            chainId = 56,
            receivingWalletAddress = DEVELOPER_WALLET_ADDRESS,
            contractAddress = BuildConfig.BSC_USDT_CONTRACT,
            decimals = 18,
            brandColor = Color(0xFF0D9488),
            explorerNote = "Official Binance-Peg BSC-USD Token Contract"
        ),
        CryptoTokenConfig(
            symbol = "USDC",
            fullName = "USD Coin",
            networkName = "BNB Smart Chain (BSC)",
            tokenStandard = "BEP-20",
            chainId = 56,
            receivingWalletAddress = DEVELOPER_WALLET_ADDRESS,
            contractAddress = BuildConfig.BSC_USDC_CONTRACT,
            decimals = 18,
            brandColor = Color(0xFF0284C7),
            explorerNote = "Official Binance-Peg USD Coin Token Contract"
        )
    )

    fun isValidEvmAddress(address: String): Boolean {
        return Regex("^0x[a-fA-F0-9]{40}$").matches(address)
    }
}
