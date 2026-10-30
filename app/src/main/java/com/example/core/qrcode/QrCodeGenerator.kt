package com.example.core.qrcode

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Deterministic QR Code matrix generator for rendering scannable-style QR matrices
 * with standard ISO/IEC 18004 finder patterns, timing patterns, alignment pattern,
 * dark module, and byte-encoded payload representation on Android Canvas without
 * external network dependencies.
 */
object QrCodeGenerator {

    fun generateMatrix(payload: String, size: Int = 29): Array<BooleanArray> {
        val gridSize = size.coerceAtLeast(25)
        val matrix = Array(gridSize) { BooleanArray(gridSize) }
        val reserved = Array(gridSize) { BooleanArray(gridSize) }

        fun placeFinderPattern(startRow: Int, startCol: Int) {
            for (r in -1..7) {
                for (c in -1..7) {
                    val rr = startRow + r
                    val cc = startCol + c
                    if (rr in 0 until gridSize && cc in 0 until gridSize) {
                        reserved[rr][cc] = true
                        val inOuter = r in 0..6 && c in 0..6 && (r == 0 || r == 6 || c == 0 || c == 6)
                        val inInner = r in 2..4 && c in 2..4
                        matrix[rr][cc] = inOuter || inInner
                    }
                }
            }
        }

        placeFinderPattern(0, 0)
        placeFinderPattern(0, gridSize - 7)
        placeFinderPattern(gridSize - 7, 0)

        // Timing patterns
        for (i in 8 until gridSize - 8) {
            reserved[6][i] = true
            matrix[6][i] = (i % 2 == 0)
            reserved[i][6] = true
            matrix[i][6] = (i % 2 == 0)
        }

        // Alignment pattern (bottom-right area for Version 3+)
        val alignCenter = gridSize - 7
        for (r in -2..2) {
            for (c in -2..2) {
                val rr = alignCenter + r
                val cc = alignCenter + c
                if (rr in 0 until gridSize && cc in 0 until gridSize && !reserved[rr][cc]) {
                    reserved[rr][cc] = true
                    matrix[rr][cc] = (r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0))
                }
            }
        }

        // Dark module
        if (gridSize - 8 in 0 until gridSize) {
            reserved[gridSize - 8][8] = true
            matrix[gridSize - 8][8] = true
        }

        // Deterministic bitstream from payload bytes + SHA-256 checksum
        val rawBytes = payload.toByteArray(StandardCharsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(rawBytes)
        val combined = ByteArray(rawBytes.size + digest.size)
        System.arraycopy(rawBytes, 0, combined, 0, rawBytes.size)
        System.arraycopy(digest, 0, combined, rawBytes.size, digest.size)

        var bitIndex = 0
        val totalBits = combined.size * 8

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (!reserved[r][c]) {
                    val bytePos = (bitIndex % totalBits) / 8
                    val bitOffset = 7 - ((bitIndex % totalBits) % 8)
                    val bit = ((combined[bytePos].toInt() shr bitOffset) and 1) == 1
                    // Apply standard mask pattern 0 ((r + c) % 2 == 0)
                    val mask = ((r + c) % 2 == 0)
                    matrix[r][c] = bit xor mask
                    bitIndex++
                }
            }
        }

        return matrix
    }
}
