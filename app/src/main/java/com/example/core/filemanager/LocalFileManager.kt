package com.example.core.filemanager

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.data.local.PdfDocumentEntity
import com.example.data.local.SavedSignatureEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object LocalFileManager {

    fun getDocumentsDir(context: Context): File {
        return File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }
    }

    fun getExportsDir(context: Context): File {
        return File(context.filesDir, "exports").apply { if (!exists()) mkdirs() }
    }

    fun getTempDir(context: Context): File {
        return File(context.cacheDir, "temp_pdf").apply { if (!exists()) mkdirs() }
    }

    fun cleanTempFiles(context: Context): Int {
        val dir = getTempDir(context)
        val files = dir.listFiles() ?: return 0
        var removed = 0
        for (f in files) {
            if (f.isFile && f.delete()) removed++
        }
        return removed
    }

    fun getTempCacheSizeBytes(context: Context): Long {
        val dir = getTempDir(context)
        return dir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    fun getWorkspaceStorageSizeBytes(context: Context): Long {
        val docs = getDocumentsDir(context).listFiles()?.sumOf { it.length() } ?: 0L
        val exports = getExportsDir(context).listFiles()?.sumOf { it.length() } ?: 0L
        return docs + exports
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val kb = bytes / 1024.0
        if (kb < 1024.0) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.2f MB", mb)
    }

    fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.US)
        return sdf.format(Date(millis))
    }

    fun sanitizeFileName(rawName: String, defaultBase: String = "PDFly_Document"): String {
        val withoutExt = rawName.trim()
            .removeSuffix(".pdf")
            .removeSuffix(".PDF")
            .replace("..", "_")
            .replace("/", "_")
            .replace("\\", "_")
        val cleaned = withoutExt
            .replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_', ' ', '.')
            .take(100)
            .trim('_', ' ', '.')
            .ifBlank { defaultBase }
        return "$cleaned.pdf"
    }

    suspend fun importUriToLocalDocuments(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        runCatching {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val displayName = queryDisplayName(context, uri) ?: "Imported_${System.currentTimeMillis()}.pdf"
            val safeName = sanitizeFileName(displayName)
            val targetFile = createUniqueFile(getDocumentsDir(context), safeName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 0L) targetFile else null
        }.getOrNull()
    }

    suspend fun exportLocalFileToSafUri(context: Context, sourceFile: File, targetUri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(targetUri)?.use { output ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                } != null
            }.getOrDefault(false)
        }

    fun resolveMimeType(file: File): String {
        return when (file.extension.lowercase(Locale.US)) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "txt" -> "text/plain"
            else -> "*/*"
        }
    }

    fun sharePdfFile(context: Context, file: File) {
        runCatching {
            if (!file.exists()) return
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = resolveMimeType(file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooserTitle = if (mimeType == "application/pdf") "Share PDF via" else "Share File via"
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun createUniqueFile(directory: File, requestedName: String): File {
        val safeName = sanitizeFileName(requestedName)
        val base = safeName.removeSuffix(".pdf")
        var candidate = File(directory, safeName)
        var counter = 1
        while (candidate.exists()) {
            candidate = File(directory, "${base}_$counter.pdf")
            counter++
        }
        return candidate
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        }.getOrNull()
    }

    /**
     * Generates realistic multi-page sample PDF documents locally on the device
     * on first launch so all PDF tools (Reader, Merge, Split, Compress, Annotate, Signature)
     * have genuine local PDF files ready to inspect and test immediately.
     */
    suspend fun seedInitialSampleDocumentsIfNeeded(
        context: Context
    ): Pair<List<PdfDocumentEntity>, SavedSignatureEntity> = withContext(Dispatchers.IO) {
        val docsDir = getDocumentsDir(context)

        val specFile = File(docsDir, "PDFly_Architecture_Handbook.pdf")
        if (!specFile.exists()) {
            generateRichPdfFile(
                file = specFile,
                docTitle = "PDFly Architecture & Security Handbook",
                subtitle = "100% Local On-Device PDF Processing Specification • Rev 2.4",
                accentColor = Color.rgb(217, 27, 66),
                pages = listOf(
                    SamplePageContent(
                        heading = "1. Executive Summary & Privacy Guarantee",
                        bullets = listOf(
                            "Zero Cloud Uploads: All PDF rendering, merging, splitting, and compression execute locally.",
                            "Storage Access Framework (SAF): Fine-grained document access without broad storage permissions.",
                            "Multi-Page Vector & Bitmap Pipeline: Supports A4, US Letter, Legal, and custom aspect ratios.",
                            "Memory-Aware Streaming: Processes documents sequentially on background IO coroutines."
                        ),
                        footerNote = "Section 1 • Confidential Technical Architecture"
                    ),
                    SamplePageContent(
                        heading = "2. PDF Compression & Image Optimization",
                        bullets = listOf(
                            "Low Compression (150 DPI / 85% Quality): Ideal for high-clarity archival and desktop printing.",
                            "Medium Compression (110 DPI / 68% Quality): Balanced quality and size for fast email attachments.",
                            "High Compression (78 DPI / 48% Quality): Aggressive bitmap downsampling for bandwidth-constrained sharing.",
                            "Honest Metrics: Compression savings depend on embedded raster images vs vector text content."
                        ),
                        footerNote = "Section 2 • Compression Engine Benchmarks"
                    ),
                    SamplePageContent(
                        heading = "3. Annotation, Signatures & Document Scanner",
                        bullets = listOf(
                            "Interactive Annotations: Highlight, underline, freehand ink, and callout notes per page.",
                            "Vector Signature Pad: Smooth multi-stroke ink capture burned directly into exported PDFs.",
                            "CameraX Document Scanner: Perspective crop, rotation, B&W high-contrast & Magic filters.",
                            "Voluntary Support: Optional USDT/USDC (BEP-20) donations never lock or restrict any feature."
                        ),
                        footerNote = "Section 3 • End-User Capabilities"
                    ),
                    SamplePageContent(
                        heading = "4. Release Verification & Sign-Off Sheet",
                        bullets = listOf(
                            "QA Checklist: Verified PDF Reader zoom, page thumbnails, rotation, bookmarks, and search.",
                            "Verified Merge reordering, Split range validation, and SAF export safety.",
                            "Use the 'Add Signature' or 'Annotate' tool on this page to test live PDF stamping!",
                            "Authorized Signatory Block below reserved for digital ink signature placement."
                        ),
                        footerNote = "Section 4 • Sign-Off & Approval Block"
                    )
                )
            )
        }

        val contractFile = File(docsDir, "Master_Service_Agreement_2026.pdf")
        if (!contractFile.exists()) {
            generateRichPdfFile(
                file = contractFile,
                docTitle = "Master Software Design Agreement",
                subtitle = "Standard Commercial License & Deliverable Schedule • Contract #MSA-2026-089",
                accentColor = Color.rgb(37, 99, 235),
                pages = listOf(
                    SamplePageContent(
                        heading = "Article I — Scope of Professional Services",
                        bullets = listOf(
                            "1.1 Deliverable Milestones: Mobile UI Architecture, PDF Engine Integration, and QA Suite.",
                            "1.2 Intellectual Property: All custom source code and design tokens remain client-owned.",
                            "1.3 Offline Compliance: Software shall operate 100% offline with zero telemetry collection.",
                            "1.4 Acceptance Criteria: Full compliance with Material Design 3 and accessibility standards."
                        ),
                        footerNote = "Contract #MSA-2026-089 • Page 1 of 3"
                    ),
                    SamplePageContent(
                        heading = "Article II — Schedule & Quality Assurance",
                        bullets = listOf(
                            "2.1 Performance Targets: Smooth 60fps scrolling and sub-second page thumbnail rendering.",
                            "2.2 Error Handling: Graceful recovery for corrupted, encrypted, or interrupted PDF streams.",
                            "2.3 Security Audit: Verified zero external network transmission of user document contents.",
                            "2.4 Warranty Period: 90 days of post-release maintenance and regression verification."
                        ),
                        footerNote = "Contract #MSA-2026-089 • Page 2 of 3"
                    ),
                    SamplePageContent(
                        heading = "Article III — Execution & Digital Signatures",
                        bullets = listOf(
                            "IN WITNESS WHEREOF, the parties hereto have executed this Master Service Agreement.",
                            "Client Representative: ___________________________   Date: Oct 30, 2026",
                            "Lead Architect:        ___________________________   Date: Oct 30, 2026",
                            "Tip: Open this PDF in PDF Reader or Signature tool to stamp your signature here."
                        ),
                        footerNote = "Contract #MSA-2026-089 • Page 3 of 3"
                    )
                )
            )
        }

        val invoiceFile = File(docsDir, "Q4_Equipment_Invoice_Bundle.pdf")
        if (!invoiceFile.exists()) {
            generateRichPdfFile(
                file = invoiceFile,
                docTitle = "Q4 Studio Hardware & Receipt Bundle",
                subtitle = "Consolidated Expense Vouchers & Tax Summary • FY 2026",
                accentColor = Color.rgb(5, 150, 105),
                pages = listOf(
                    SamplePageContent(
                        heading = "Voucher #101 — Mobile Test Device Lab",
                        bullets = listOf(
                            "Item 01: Flagship Android 16 Reference Handset (256GB) .......... $899.00",
                            "Item 02: Foldable Large-Screen Tablet Reference Unit ............ $1,249.00",
                            "Item 03: Calibrated Colorimetric Document Lighting Rig .......... $340.00",
                            "Subtotal Verified: $2,488.00 • Paid in Full • Tax Deductible R&D"
                        ),
                        footerNote = "Expense Bundle • Voucher 1 of 2"
                    ),
                    SamplePageContent(
                        heading = "Voucher #102 — Optical Calibration Targets",
                        bullets = listOf(
                            "Item 04: ISO-12233 CameraX Resolution Test Chart ................ $145.00",
                            "Item 05: Matte Anti-Glare Document Scan Platen .................. $89.00",
                            "Total Consolidated Q4 Equipment Expense: $2,722.00",
                            "Ready for PDF Merge, Split, or Compression testing."
                        ),
                        footerNote = "Expense Bundle • Voucher 2 of 2"
                    )
                )
            )
        }

        val now = System.currentTimeMillis()
        val entities = listOf(
            PdfDocumentEntity(
                title = "PDFly_Architecture_Handbook.pdf",
                filePath = specFile.absolutePath,
                sizeBytes = specFile.length(),
                pageCount = 4,
                isFavorite = true,
                lastOpenedAt = now - 60_000L,
                createdAt = now - 3_600_000L,
                bookmarkedPagesCsv = "0,3",
                searchableSummary = "Executive Summary Privacy Guarantee Zero Cloud Uploads SAF Compression Low Medium High CameraX Scanner Signature Release Verification",
                categoryTag = "Handbook"
            ),
            PdfDocumentEntity(
                title = "Master_Service_Agreement_2026.pdf",
                filePath = contractFile.absolutePath,
                sizeBytes = contractFile.length(),
                pageCount = 3,
                isFavorite = true,
                lastOpenedAt = now - 180_000L,
                createdAt = now - 7_200_000L,
                bookmarkedPagesCsv = "2",
                searchableSummary = "Master Software Design Agreement Scope Services Intellectual Property Schedule Execution Digital Signatures",
                categoryTag = "Contract"
            ),
            PdfDocumentEntity(
                title = "Q4_Equipment_Invoice_Bundle.pdf",
                filePath = invoiceFile.absolutePath,
                sizeBytes = invoiceFile.length(),
                pageCount = 2,
                isFavorite = false,
                lastOpenedAt = now - 420_000L,
                createdAt = now - 14_400_000L,
                bookmarkedPagesCsv = "",
                searchableSummary = "Q4 Studio Hardware Receipt Bundle Voucher Mobile Test Device Lab Optical Calibration Targets $2,722.00",
                categoryTag = "Finance"
            )
        )

        val defaultSignature = SavedSignatureEntity(
            label = "Executive Ink Signature",
            inkColorHex = 0xFF1E3A8A,
            strokesSerialized = "0.10,0.65;0.18,0.28;0.26,0.58;0.35,0.35;0.46,0.52;0.58,0.40;0.74,0.48;0.88,0.42|0.14,0.72;0.52,0.68;0.86,0.64"
        )

        Pair(entities, defaultSignature)
    }

    private data class SamplePageContent(
        val heading: String,
        val bullets: List<String>,
        val footerNote: String
    )

    private val seedLock = Any()

    private fun generateRichPdfFile(
        file: File,
        docTitle: String,
        subtitle: String,
        accentColor: Int,
        pages: List<SamplePageContent>
    ) = synchronized(seedLock) {
        if (file.exists() && file.length() > 64L) return@synchronized
        var pdf: PdfDocument? = null
        try {
            pdf = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(15, 23, 42) }
            val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(248, 250, 252) }
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(203, 213, 225)
                textSize = 11f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(15, 23, 42)
                textSize = 16f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(51, 65, 85)
                textSize = 12.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(100, 116, 139)
                textSize = 10f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }

            pages.forEachIndexed { index, pageData ->
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = pdf.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), 112f, bannerPaint)
                canvas.drawRect(0f, 108f, pageWidth.toFloat(), 114f, accentPaint)

                canvas.drawText(docTitle, 40f, 52f, titlePaint)
                canvas.drawText(subtitle, 40f, 78f, subPaint)

                val badgeRect = RectF(pageWidth - 130f, 34f, pageWidth - 36f, 66f)
                canvas.drawRoundRect(badgeRect, 16f, 16f, accentPaint)
                canvas.drawText("PAGE ${index + 1}/${pages.size}", pageWidth - 115f, 54f, subPaint.apply { color = Color.WHITE })

                canvas.drawText(pageData.heading, 40f, 164f, headingPaint)
                canvas.drawLine(40f, 176f, pageWidth - 40f, 176f, borderPaint)

                val contentRect = RectF(40f, 198f, pageWidth - 40f, 520f)
                canvas.drawRoundRect(contentRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(contentRect, 12f, 12f, borderPaint)

                var yPos = 236f
                pageData.bullets.forEach { bullet ->
                    canvas.drawCircle(64f, yPos - 4f, 4.5f, accentPaint)
                    val words = bullet.split(" ")
                    var currentLine = StringBuilder()
                    for (w in words) {
                        if (currentLine.length + w.length + 1 > 64) {
                            canvas.drawText(currentLine.toString(), 80f, yPos, bodyPaint)
                            yPos += 20f
                            currentLine = StringBuilder(w)
                        } else {
                            if (currentLine.isNotEmpty()) currentLine.append(" ")
                            currentLine.append(w)
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine.toString(), 80f, yPos, bodyPaint)
                        yPos += 32f
                    }
                }

                val boxRect = RectF(40f, 550f, pageWidth - 40f, 740f)
                canvas.drawRoundRect(boxRect, 12f, 12f, borderPaint)
                canvas.drawText("LOCAL VERIFICATION & ANNOTATION ZONE", 58f, 582f, footerPaint)

                for (bar in 0..3) {
                    val top = 604f + bar * 28f
                    val fillW = 140f + ((index + 1) * 45f) + (bar * 52f)
                    val barBg = RectF(58f, top, pageWidth - 58f, top + 14f)
                    val barFg = RectF(58f, top, (58f + fillW).coerceAtMost(pageWidth - 58f), top + 14f)
                    canvas.drawRoundRect(barBg, 7f, 7f, cardPaint)
                    canvas.drawRoundRect(barFg, 7f, 7f, accentPaint)
                }

                canvas.drawLine(40f, pageHeight - 56f, pageWidth - 40f, pageHeight - 56f, borderPaint)
                canvas.drawText(pageData.footerNote, 40f, pageHeight - 34f, footerPaint)
                canvas.drawText("Generated Offline by PDFly Engine", pageWidth - 235f, pageHeight - 34f, footerPaint)

                pdf.finishPage(page)
            }

            FileOutputStream(file).use { out ->
                pdf.writeTo(out)
            }
        } catch (_: Throwable) {
            // Headless JVM / Robolectric fallback: write spec-compliant %PDF-1.4
            com.example.core.processing.IsoPdf14Codec.writeValidPdf14(
                file = file,
                pages = pages.map { p ->
                    com.example.core.processing.IsoPdf14Codec.PageSpec(
                        heading = "$docTitle - ${p.heading}",
                        lines = listOf(subtitle) + p.bullets,
                        footer = p.footerNote
                    )
                }
            )
        } finally {
            runCatching { pdf?.close() }
        }
    }
}
