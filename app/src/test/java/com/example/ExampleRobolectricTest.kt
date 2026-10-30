package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.IsoPdf14Codec
import com.example.core.processing.PdfInspectionResult
import com.example.core.processing.PdfProcessingEngine
import com.example.data.local.PdfAnnotationEntity
import com.example.domain.model.AnnotationType
import com.example.domain.model.AppThemeMode
import com.example.domain.model.CompressionPreset
import com.example.domain.model.ImageExportFormat
import com.example.domain.model.PageMarginPreset
import com.example.domain.model.PageNumberConfig
import com.example.domain.model.PageNumberFormat
import com.example.domain.model.PageNumberPosition
import com.example.domain.model.PageOrganizeItem
import com.example.domain.model.PageOrientation
import com.example.domain.model.PaperSizePreset
import com.example.domain.model.ScannedPageDraft
import com.example.domain.model.SplitMode
import com.example.domain.model.WatermarkConfig
import com.example.domain.model.WatermarkPosition
import com.example.feature.TopLevelDestination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w412dp-h915dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun verifyAppNameAndInitialSamplePdfSeedingAndInspection(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("PDFly", context.getString(R.string.app_name))

        val (seededDocs, signature) = LocalFileManager.seedInitialSampleDocumentsIfNeeded(context)
        assertEquals(3, seededDocs.size)
        assertTrue(signature.strokesSerialized.isNotBlank())

        val firstFile = File(seededDocs.first().filePath)
        val validCheck = PdfProcessingEngine.inspectPdfFile(firstFile)
        assertTrue(validCheck is PdfInspectionResult.Valid)
        assertEquals(4, (validCheck as PdfInspectionResult.Valid).pageCount)

        val corruptedFile = File(LocalFileManager.getTempDir(context), "corrupted.pdf").apply {
            writeText("NOT_A_VALID_PDF_HEADER")
        }
        val corruptedCheck = PdfProcessingEngine.inspectPdfFile(corruptedFile)
        assertTrue(corruptedCheck is PdfInspectionResult.Error)
        corruptedFile.delete()
    }

    @Test
    fun verifyPdfMergeSplitCompressScannerExportAndAnnotationFlattening(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (seededDocs, _) = LocalFileManager.seedInitialSampleDocumentsIfNeeded(context)
        val file1 = File(seededDocs[0].filePath)
        val file2 = File(seededDocs[1].filePath)

        // 1. Test Merge PDFs (4 pages + 3 pages = 7 pages)
        val mergeRes = PdfProcessingEngine.mergePdfs(
            context = context,
            inputFiles = listOf(file1, file2),
            outputFileName = "Test_Merged_Output"
        )
        assertTrue(mergeRes.success)
        assertEquals(7, mergeRes.totalPageCount)
        assertNotNull(mergeRes.primaryOutputFile)

        // 2. Test Split PDF (Custom range 1-2 from 4-page file)
        val splitRes = PdfProcessingEngine.splitPdf(
            context = context,
            sourceFile = file1,
            splitMode = SplitMode.CUSTOM_RANGE,
            rangeInput = "1-2",
            outputBaseName = "Test_Split_Range"
        )
        assertTrue(splitRes.success)
        assertEquals(2, splitRes.totalPageCount)

        // 3. Test Compress PDF
        val compressRes = PdfProcessingEngine.compressPdf(
            context = context,
            sourceFile = file1,
            preset = CompressionPreset.MEDIUM,
            outputFileName = "Test_Compressed_Output"
        )
        assertTrue(compressRes.success)
        assertEquals(4, compressRes.totalPageCount)

        // 4. Test Scanner / Image-to-PDF generation
        val scanRes = PdfProcessingEngine.createPdfFromScannedPages(
            context = context,
            pages = listOf(
                ScannedPageDraft(id = "test-1", title = "Scan 1", sourceType = "SCANNER"),
                ScannedPageDraft(id = "test-2", title = "Scan 2", sourceType = "SCANNER")
            ),
            paperSize = PaperSizePreset.A4,
            orientation = PageOrientation.PORTRAIT,
            marginPreset = PageMarginPreset.NORMAL,
            imageQualityPercent = 85,
            outputFileName = "Test_Scanned_Batch"
        )
        assertTrue(scanRes.success)
        assertEquals(2, scanRes.totalPageCount)

        // 5. Test Flatten Annotations & Signatures to PDF
        val flattenRes = PdfProcessingEngine.flattenAnnotationsToNewPdf(
            context = context,
            sourceFile = file2,
            annotations = listOf(
                PdfAnnotationEntity(
                    documentId = 1L,
                    pageIndex = 0,
                    annotationType = AnnotationType.HIGHLIGHT.name,
                    textContent = "Test Highlight"
                )
            ),
            outputFileName = "Test_Flattened_Signed"
        )
        assertTrue(flattenRes.success)
        assertEquals(3, flattenRes.totalPageCount)
    }

    @Test
    fun verifyExpandedToolkitOrganizeWatermarkPageNumbersConvertTextMetadataAndSecurity(): Unit = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val (seededDocs, _) = LocalFileManager.seedInitialSampleDocumentsIfNeeded(context)
        val sourceFile = File(seededDocs[0].filePath)

        // 1. Page Organizer (reorder & rotate 3 active pages, delete 1 page)
        val orgItems = listOf(
            PageOrganizeItem(originalPageIndex = 1, rotationDegrees = 90, isDeleted = false),
            PageOrganizeItem(originalPageIndex = 0, rotationDegrees = 0, isDeleted = false),
            PageOrganizeItem(originalPageIndex = 2, rotationDegrees = 180, isDeleted = false),
            PageOrganizeItem(originalPageIndex = 3, rotationDegrees = 0, isDeleted = true)
        )
        val orgRes = PdfProcessingEngine.organizePdfPages(
            context = context,
            sourceFile = sourceFile,
            organizedItems = orgItems,
            extractSelectedOnly = false,
            outputFileName = "Test_Organized_Output"
        )
        assertTrue(orgRes.success)
        assertEquals(3, orgRes.totalPageCount)

        // 2. Watermark PDF
        val wmRes = PdfProcessingEngine.applyWatermarkToPdf(
            context = context,
            sourceFile = sourceFile,
            config = WatermarkConfig(
                text = "CONFIDENTIAL TEST",
                opacity = 0.35f,
                fontSizePt = 36f,
                position = WatermarkPosition.DIAGONAL_CENTER
            ),
            outputFileName = "Test_Watermarked_Output"
        )
        assertTrue(wmRes.success)
        assertEquals(4, wmRes.totalPageCount)

        // 3. Add Page Numbers
        val numRes = PdfProcessingEngine.addPageNumbersToPdf(
            context = context,
            sourceFile = sourceFile,
            config = PageNumberConfig(
                position = PageNumberPosition.BOTTOM_RIGHT,
                format = PageNumberFormat.PAGE_X_OF_Y,
                startNumber = 1
            ),
            outputFileName = "Test_Numbered_Output"
        )
        assertTrue(numRes.success)
        assertEquals(4, numRes.totalPageCount)

        // 4. Convert PDF to Images
        val (imgOpRes, exportedImages) = PdfProcessingEngine.convertPdfToImages(
            context = context,
            sourceFile = sourceFile,
            format = ImageExportFormat.PNG,
            qualityPercent = 90,
            baseFileName = "Test_Page_Img"
        )
        assertTrue(imgOpRes.success)
        assertEquals(4, exportedImages.size)

        // 5. Extract Text from PDF
        val textRes = PdfProcessingEngine.extractTextFromPdf(context, sourceFile)
        assertEquals(4, textRes.pageCount)
        assertTrue(textRes.combinedText.isNotBlank())

        // 6. Inspect Detailed Metadata
        val meta = PdfProcessingEngine.inspectDetailedMetadata(sourceFile)
        assertEquals(4, meta.pageCount)
        assertTrue(meta.sha256Checksum.length == 64)

        // 7. Protect PDF with Password & Unlock with Correct/Incorrect Password
        val protectRes = PdfProcessingEngine.protectOrUnlockPdf(
            context = context,
            sourceFile = sourceFile,
            password = "Secret2026",
            unlockMode = false,
            outputFileName = "Test_Protected_Output"
        )
        assertTrue(protectRes.success)
        val protectedFile = protectRes.primaryOutputFile!!

        val wrongUnlock = PdfProcessingEngine.protectOrUnlockPdf(
            context = context,
            sourceFile = protectedFile,
            password = "WrongPassword",
            unlockMode = true,
            outputFileName = "Test_Wrong_Unlock"
        )
        assertFalse(wrongUnlock.success)

        val validUnlock = PdfProcessingEngine.protectOrUnlockPdf(
            context = context,
            sourceFile = protectedFile,
            password = "Secret2026",
            unlockMode = true,
            outputFileName = "Test_Valid_Unlock"
        )
        assertTrue(validUnlock.success)
        assertEquals(4, validUnlock.totalPageCount)
    }

    @Test
    fun verifyDashboardFunctionalMotionVisualizersAndVoluntaryCryptoDonationUiFlow() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("PDFly").assertIsDisplayed()

        composeTestRule.onNodeWithTag("dashboard_lazy_column")
            .performScrollToNode(hasTestTag("tool_card_reader"))
        composeTestRule.onNodeWithTag("tool_card_reader").assertIsDisplayed()

        composeTestRule.onNodeWithTag("dashboard_lazy_column")
            .performScrollToNode(hasTestTag("tool_card_merge"))
        composeTestRule.onNodeWithTag("tool_card_merge").assertIsDisplayed()

        // Open Merge PDF Tool and verify Merge Convergence Functional Motion Visualizer
        composeTestRule.onNodeWithTag("tool_card_merge").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("merge_functional_visualizer").assertIsDisplayed()

        // Navigate back to Dashboard and open Settings to verify Theme chips & Reduced Motion toggle
        composeTestRule.onNodeWithTag("nav_back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("nav_tab_settings").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_chip_LIGHT").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_chip_DARK").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_chip_LIGHT").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settings_lazy_column")
            .performScrollToNode(hasTestTag("toggle_reduced_motion"))
        composeTestRule.onNodeWithTag("toggle_reduced_motion").assertIsDisplayed()

        // Navigate to Support screen via bottom nav tab
        composeTestRule.onNodeWithTag("nav_tab_support").performClick()
        composeTestRule.waitForIdle()

        // Verify Distribution Build Policy Configuration and build-selection buttons are completely gone
        composeTestRule.onNodeWithText("Distribution Build Policy Configuration").assertDoesNotExist()
        composeTestRule.onNodeWithText("Direct APK / Open Web Build").assertDoesNotExist()
        composeTestRule.onNodeWithText("Google Play Store Build").assertDoesNotExist()

        // Verify redesigned Support PDFly header and Free Forever info card
        composeTestRule.onNodeWithText("Support PDFly").assertIsDisplayed()
        composeTestRule.onNodeWithText("PDFly is Free Forever").assertIsDisplayed()

        // Scroll to token selector inside LazyColumn
        composeTestRule.onNodeWithTag("support_donation_lazy_column")
            .performScrollToNode(hasTestTag("token_selector_USDT"))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("token_selector_USDT").assertIsDisplayed()
        composeTestRule.onNodeWithTag("token_selector_USDC").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("support_donation_lazy_column")
            .performScrollToNode(hasTestTag("wallet_and_contract_card"))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("0x21b8f3cea6901820adfca5deb3be37e567ab0f97").assertIsDisplayed()
        composeTestRule.onNodeWithTag("copy_wallet_address_button").assertIsDisplayed()
    }

    @Test
    fun verifyCinematicSplashLaunchExperienceColdStartAndWarmResumeLifecycle() {
        composeTestRule.waitForIdle()
        val viewModel = composeTestRule.activity.viewModel

        // Verify initial cold start completed cleanly and revealed the Home Dashboard
        assertFalse(viewModel.showCinematicIntro.value)
        composeTestRule.onNodeWithTag("dashboard_lazy_column").assertIsDisplayed()

        // Trigger the cinematic splash screen in inspection mode to verify all choreographed visual elements
        composeTestRule.runOnUiThread {
            viewModel.replayCinematicIntro(holdForInspection = true)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cinematic_splash_root").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_logo_emblem").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_document_assembly_canvas").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_brand_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_brand_tagline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Complete PDF Studio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_progress_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("splash_skip_button").assertIsDisplayed()

        // Complete the splash intro via the Skip/Complete action and verify seamless reveal of Home Dashboard
        composeTestRule.onNodeWithTag("splash_skip_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cinematic_splash_root").assertDoesNotExist()
        composeTestRule.onNodeWithTag("dashboard_lazy_column").assertIsDisplayed()
        assertFalse(viewModel.showCinematicIntro.value)

        // Verify warm session resume (background -> foreground) does NOT replay the full cold-start splash
        val initialWarmCount = viewModel.warmResumeCount.value
        composeTestRule.runOnUiThread {
            viewModel.onAppBackgrounded()
            viewModel.onAppResumed()
        }
        composeTestRule.waitForIdle()

        assertFalse(viewModel.showCinematicIntro.value)
        assertEquals(initialWarmCount + 1, viewModel.warmResumeCount.value)
        composeTestRule.onNodeWithTag("dashboard_lazy_column").assertIsDisplayed()
    }

    @Test
    fun verifyRealWorldPdfEdgeCasesCompressionAndStorageHygiene() = runBlocking {
        val context = composeTestRule.activity.applicationContext
        val tempDir = LocalFileManager.getTempDir(context)
        val docsDir = LocalFileManager.getDocumentsDir(context)

        // 1. Mixed-orientation & different paper sizes (A4, US Letter, US Legal, Landscape)
        val mixedSizeFile = File(tempDir, "Mixed_Paper_Sizes_Test.pdf")
        IsoPdf14Codec.writeValidPdf14(
            file = mixedSizeFile,
            pages = listOf(
                IsoPdf14Codec.PageSpec(widthPt = 595, heightPt = 842, heading = "A4 Portrait", lines = listOf("ISO A4")),
                IsoPdf14Codec.PageSpec(widthPt = 612, heightPt = 792, heading = "US Letter", lines = listOf("ANSI Letter")),
                IsoPdf14Codec.PageSpec(widthPt = 612, heightPt = 1008, heading = "US Legal", lines = listOf("Legal Size")),
                IsoPdf14Codec.PageSpec(widthPt = 842, heightPt = 595, heading = "A4 Landscape", lines = listOf("Landscape"))
            ),
            documentTitle = "Mixed Paper Sizes"
        )
        val parsedDims = IsoPdf14Codec.parsePageDimensionsPerPage(mixedSizeFile)
        assertEquals(4, parsedDims.size)
        assertEquals(595 to 842, parsedDims[0])
        assertEquals(612 to 792, parsedDims[1])
        assertEquals(612 to 1008, parsedDims[2])
        assertEquals(842 to 595, parsedDims[3])

        // Split pages 2-4 from mixed-size PDF and verify per-page dimensions are preserved
        val splitRes = PdfProcessingEngine.splitPdf(
            context = context,
            sourceFile = mixedSizeFile,
            splitMode = SplitMode.CUSTOM_RANGE,
            rangeInput = "2-4",
            outputBaseName = "Mixed_Split_Pages_2_to_4.pdf"
        )
        assertTrue(splitRes.success)
        val splitOut = splitRes.primaryOutputFile!!
        val splitDims = IsoPdf14Codec.parsePageDimensionsPerPage(splitOut)
        assertEquals(3, splitDims.size)
        assertEquals(612 to 792, splitDims[0])
        assertEquals(612 to 1008, splitDims[1])
        assertEquals(842 to 595, splitDims[2])

        // 2. Image-heavy PDF compression across LOW, MEDIUM, and HIGH presets
        val imageHeavyFile = File(tempDir, "Image_Heavy_Scan_Test.pdf")
        val heavyPayload = ByteArray(12_000) { idx -> (idx % 251).toByte() }
        IsoPdf14Codec.writeValidPdf14(
            file = imageHeavyFile,
            pages = listOf(
                IsoPdf14Codec.PageSpec(
                    heading = "High-Res Photo Scan Page 1",
                    lines = listOf("Embedded Raster Stream Page 1"),
                    binaryPayloadPadding = heavyPayload
                ),
                IsoPdf14Codec.PageSpec(
                    heading = "High-Res Photo Scan Page 2",
                    lines = listOf("Embedded Raster Stream Page 2"),
                    binaryPayloadPadding = heavyPayload
                )
            )
        )
        val origHeavySize = imageHeavyFile.length()
        val lowComp = PdfProcessingEngine.compressPdf(context, imageHeavyFile, CompressionPreset.LOW, "Heavy_Low.pdf")
        val medComp = PdfProcessingEngine.compressPdf(context, imageHeavyFile, CompressionPreset.MEDIUM, "Heavy_Med.pdf")
        val highComp = PdfProcessingEngine.compressPdf(context, imageHeavyFile, CompressionPreset.HIGH, "Heavy_High.pdf")

        assertTrue(lowComp.success && medComp.success && highComp.success)
        val lowLen = lowComp.primaryOutputFile!!.length()
        val medLen = medComp.primaryOutputFile!!.length()
        val highLen = highComp.primaryOutputFile!!.length()
        assertTrue("LOW ($lowLen) should be smaller than original ($origHeavySize)", lowLen < origHeavySize)
        assertTrue("MEDIUM ($medLen) should be smaller than LOW ($lowLen)", medLen < lowLen)
        assertTrue("HIGH ($highLen) should be smaller than MEDIUM ($medLen)", highLen < medLen)

        // 3. External /Encrypt password-protected PDF detection
        val externalEncryptedFile = File(tempDir, "External_Encrypted.pdf")
        externalEncryptedFile.writeText(
            "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Filter /Standard /V 2 /R 3 /Length 128 >>\nendobj\n" +
                "trailer\n<< /Root 1 0 R /Encrypt 3 0 R >>\n%%EOF"
        )
        val encInspection = PdfProcessingEngine.inspectPdfFile(externalEncryptedFile)
        assertTrue(encInspection is PdfInspectionResult.Error)
        assertTrue((encInspection as PdfInspectionResult.Error).reason.contains("password-protected", ignoreCase = true))

        // 4. Filename sanitization, path traversal blocking, and MIME type resolution
        val traversalSanitized = LocalFileManager.sanitizeFileName("../../etc/passwd")
        assertFalse(traversalSanitized.contains(".."))
        assertFalse(traversalSanitized.contains("/"))
        assertTrue(traversalSanitized.endsWith(".pdf"))

        val longNameSanitized = LocalFileManager.sanitizeFileName("A".repeat(220) + ".pdf")
        assertTrue(longNameSanitized.length <= 104)

        assertEquals("application/pdf", LocalFileManager.resolveMimeType(File("doc.pdf")))
        assertEquals("image/png", LocalFileManager.resolveMimeType(File("page_1.png")))
        assertEquals("image/jpeg", LocalFileManager.resolveMimeType(File("page_1.jpg")))
        assertEquals("text/plain", LocalFileManager.resolveMimeType(File("extracted.txt")))

        // 5. Temporary file cleanup hygiene (removes temp files while preserving workspace documents)
        val docCountBefore = docsDir.listFiles()?.size ?: 0
        val tempTrash = File(tempDir, "abandoned_temp_chunk.tmp").apply { writeText("temp data") }
        assertTrue(tempTrash.exists())
        val removedCount = LocalFileManager.cleanTempFiles(context)
        assertTrue(removedCount >= 1)
        assertFalse(tempTrash.exists())
        val docCountAfter = docsDir.listFiles()?.size ?: 0
        assertEquals(docCountBefore, docCountAfter)
    }

    @Test
    fun captureGenuineApplicationScreenshots() {
        composeTestRule.waitForIdle()
        val viewModel = composeTestRule.activity.viewModel

        // Ensure Light Mode and Reduced Motion for crisp deterministic frame capture
        composeTestRule.runOnUiThread {
            viewModel.completeCinematicIntro()
            viewModel.setThemeMode(AppThemeMode.LIGHT)
            viewModel.setReducedMotionEnabled(true)
            viewModel.navigateTo(TopLevelDestination.DASHBOARD)
        }
        composeTestRule.waitForIdle()
        captureScreenPng("01-home-dashboard.png")

        // 2. PDF Reader
        composeTestRule.runOnUiThread {
            viewModel.navigateTo(TopLevelDestination.TOOL_READER)
        }
        Thread.sleep(250)
        composeTestRule.waitForIdle()
        captureScreenPng("02-pdf-reader.png")

        // 3. PDF Merge
        composeTestRule.runOnUiThread {
            viewModel.allDocuments.value.forEach { doc ->
                viewModel.addDocumentToMergeQueue(doc)
            }
            viewModel.navigateTo(TopLevelDestination.TOOL_MERGE)
        }
        composeTestRule.waitForIdle()
        captureScreenPng("03-pdf-merge.png")

        // 4. PDF Split
        composeTestRule.runOnUiThread {
            viewModel.navigateTo(TopLevelDestination.TOOL_SPLIT)
        }
        composeTestRule.waitForIdle()
        captureScreenPng("04-pdf-split.png")

        // 5. PDF Compressor
        composeTestRule.runOnUiThread {
            viewModel.navigateTo(TopLevelDestination.TOOL_COMPRESS)
        }
        composeTestRule.waitForIdle()
        captureScreenPng("05-pdf-compressor.png")

        // 6. PDF Scanner
        composeTestRule.runOnUiThread {
            viewModel.navigateTo(TopLevelDestination.TOOL_SCANNER)
        }
        Thread.sleep(250)
        composeTestRule.waitForIdle()
        captureScreenPng("06-pdf-scanner.png")

        // 7. PDF Signature Studio
        composeTestRule.runOnUiThread {
            viewModel.navigateTo(TopLevelDestination.TOOL_SIGNATURE)
        }
        Thread.sleep(250)
        composeTestRule.waitForIdle()
        captureScreenPng("07-pdf-signature.png")

        // 8. Other Available Tools (All 16 Tools Catalog & Page Organizer)
        composeTestRule.runOnUiThread {
            viewModel.navigateTo(TopLevelDestination.ALL_TOOLS)
        }
        composeTestRule.waitForIdle()
        captureScreenPng("08-other-tools.png")

        // 9. Settings & Theme Options (in Obsidian Dark Mode)
        composeTestRule.runOnUiThread {
            viewModel.setThemeMode(AppThemeMode.DARK)
            viewModel.navigateTo(TopLevelDestination.SETTINGS)
        }
        composeTestRule.waitForIdle()
        captureScreenPng("09-settings-theme.png")

        // Restore Light mode after capture
        composeTestRule.runOnUiThread {
            viewModel.setThemeMode(AppThemeMode.LIGHT)
        }
        composeTestRule.waitForIdle()
    }

    private fun captureScreenPng(fileName: String) {
        composeTestRule.waitForIdle()
        val bitmap: Bitmap = runCatching {
            composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        }.getOrElse {
            val view = composeTestRule.activity.window.decorView
            val w = view.width.coerceAtLeast(1080)
            val h = view.height.coerceAtLeast(2200)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            view.draw(canvas)
            bmp
        }
        val docsDir = File("../docs/screenshots").apply { mkdirs() }
        val webDir = File("../website/public/screenshots").apply { mkdirs() }
        val docsFile = File(docsDir, fileName)
        val webFile = File(webDir, fileName)
        FileOutputStream(docsFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 92, out)
        }
        FileOutputStream(webFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 92, out)
        }
        assertTrue("Screenshot $fileName must exist and be non-empty", docsFile.exists() && docsFile.length() > 1000L)
        assertTrue("Website screenshot $fileName must exist and be non-empty", webFile.exists() && webFile.length() > 1000L)
    }
}


