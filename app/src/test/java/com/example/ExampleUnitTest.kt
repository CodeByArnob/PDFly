package com.example

import com.example.core.filemanager.LocalFileManager
import com.example.core.processing.PdfProcessingEngine
import com.example.core.qrcode.QrCodeGenerator
import com.example.domain.model.CompressionPreset
import com.example.domain.model.InternalDistributionConfig
import com.example.domain.model.OfficialDonationCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify developer receiving wallet and official BSC token contracts`() {
        val expectedWallet = "0x21b8f3cea6901820adfca5deb3be37e567ab0f97"
        assertEquals(expectedWallet, OfficialDonationCatalog.DEVELOPER_WALLET_ADDRESS)
        assertTrue(OfficialDonationCatalog.isValidEvmAddress(expectedWallet))

        val tokens = OfficialDonationCatalog.supportedTokens
        assertEquals(2, tokens.size)

        val usdt = tokens.first { it.symbol == "USDT" }
        assertEquals(expectedWallet, usdt.receivingWalletAddress)
        assertEquals("BEP-20", usdt.tokenStandard)
        assertEquals(56, usdt.chainId)
        assertEquals("0x55d398326f99059fF775485246999027B3197955", usdt.contractAddress)
        assertTrue(OfficialDonationCatalog.isValidEvmAddress(usdt.contractAddress))

        val usdc = tokens.first { it.symbol == "USDC" }
        assertEquals(expectedWallet, usdc.receivingWalletAddress)
        assertEquals("BEP-20", usdc.tokenStandard)
        assertEquals(56, usdc.chainId)
        assertEquals("0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d", usdc.contractAddress)
        assertTrue(OfficialDonationCatalog.isValidEvmAddress(usdc.contractAddress))
    }

    @Test
    fun `verify internal build configuration for Direct APK and Google Play`() {
        assertTrue(
            InternalDistributionConfig.isCryptoDonationEnabledForBuild(
                channel = InternalDistributionConfig.CHANNEL_DIRECT_APK,
                enableFlag = true
            )
        )
        assertFalse(
            InternalDistributionConfig.isCryptoDonationEnabledForBuild(
                channel = InternalDistributionConfig.CHANNEL_GOOGLE_PLAY,
                enableFlag = true
            )
        )
        assertFalse(
            InternalDistributionConfig.isCryptoDonationEnabledForBuild(
                channel = InternalDistributionConfig.CHANNEL_DIRECT_APK,
                enableFlag = false
            )
        )
    }

    @Test
    fun `verify page range parser handles valid and invalid inputs`() {
        val valid = PdfProcessingEngine.parsePageSelection("1-2, 4", pageCount = 5)
        assertTrue(valid.isSuccess)
        assertEquals(listOf(0, 1, 3), valid.getOrThrow())

        val outOfBounds = PdfProcessingEngine.parsePageSelection("1-6", pageCount = 4)
        assertTrue(outOfBounds.isFailure)

        val inverted = PdfProcessingEngine.parsePageSelection("3-1", pageCount = 4)
        assertTrue(inverted.isFailure)

        val empty = PdfProcessingEngine.parsePageSelection("   ", pageCount = 4)
        assertTrue(empty.isFailure)
    }

    @Test
    fun `verify qr code generator produces valid finder patterns`() {
        val matrix = QrCodeGenerator.generateMatrix(
            OfficialDonationCatalog.DEVELOPER_WALLET_ADDRESS,
            size = 29
        )
        assertEquals(29, matrix.size)
        assertEquals(29, matrix[0].size)
        // Top-left finder corner (0,0) and center (3,3) must be dark (true)
        assertTrue(matrix[0][0])
        assertTrue(matrix[3][3])
        // Top-right finder corner (0, 28) must be dark
        assertTrue(matrix[0][28])
        // Bottom-left finder corner (28, 0) must be dark
        assertTrue(matrix[28][0])
    }

    @Test
    fun `verify filename sanitization and compression presets`() {
        assertEquals("Report_2026.pdf", LocalFileManager.sanitizeFileName("Report 2026.pdf"))
        assertEquals("Custom_File.pdf", LocalFileManager.sanitizeFileName("Custom/File?.pdf"))
        assertTrue(CompressionPreset.LOW.dpi > CompressionPreset.MEDIUM.dpi)
        assertTrue(CompressionPreset.MEDIUM.dpi > CompressionPreset.HIGH.dpi)
    }
}
