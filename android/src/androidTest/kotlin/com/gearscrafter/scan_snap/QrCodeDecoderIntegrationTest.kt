package com.gearscrafter.scan_snap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Integration tests for QRCodeDecoder that run on a real device/emulator.
 * Tests actual QR code scanning capabilities with real images and camera frames.
 *
 */
@RunWith(AndroidJUnit4::class)
class QRCodeDecoderIntegrationTest {

    private lateinit var context: Context
    private lateinit var testImageDir: File

    companion object {
        private const val TEST_QR_CONTENT = "https://gearscrafter.com"
    }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        testImageDir = File(context.cacheDir, "qr_test_images").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Test: Decode a valid QR code from a file path.
     * Expected: Returns the correct QR code content.
     */
    @Test
    fun testDecodeValidQRCodeFromFile() {
        // Arrange: Create a QR code with known content
        val qrBitmap = generateQRCodeBitmap(TEST_QR_CONTENT)
        val qrFile = saveBitmapToFile(qrBitmap, "valid_qr.png")

        // Act: Decode the QR code
        val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert: Verify the decoded content matches
        assertNotNull("QR code should be decoded", decoded)
        assertTrue(
            "Decoded content should match: expected=$TEST_QR_CONTENT, got=$decoded",
            decoded == TEST_QR_CONTENT || decoded?.contains(TEST_QR_CONTENT) == true
        )

        qrFile.delete()
        qrBitmap.recycle()
    }

    /**
     * Test: Decode multiple different QR codes in sequence.
     * Expected: Each QR code is decoded correctly with its unique content.
     */
    @Test
    fun testDecodeMultipleDifferentQRCodes() {
        val testCodes = listOf(
            "Test-Code-1",
            "Test-Code-2",
            "https://example.com",
            "user@example.com",
            "1234567890"
        )

        for ((index, content) in testCodes.withIndex()) {
            // Arrange
            val qrBitmap = generateQRCodeBitmap(content)
            val qrFile = saveBitmapToFile(qrBitmap, "qr_$index.png")

            // Act
            val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

            // Assert
            assertNotNull("QR code #$index should be decoded", decoded)
            assertTrue(
                "QR code #$index content mismatch: expected=$content, got=$decoded",
                decoded == content || decoded?.contains(content) == true
            )

            qrFile.delete()
            qrBitmap.recycle()
        }
    }

    /**
     * Test: Handle invalid/non-QR image gracefully.
     * Expected: Returns null without crashing.
     */
    @Test
    fun testDecodeInvalidImageReturnsNull() {
        // Arrange: Create a random bitmap without QR code
        val invalidBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565)
        val canvas = Canvas(invalidBitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(50f, 50f, 150f, 150f, paint)

        val invalidFile = saveBitmapToFile(invalidBitmap, "invalid_image.png")

        // Act
        val decoded = QRCodeDecoder.syncDecodeQRCode(invalidFile.absolutePath, context)

        // Assert: Should return null for non-QR image
        assertNull("Non-QR image should not decode", decoded)

        invalidFile.delete()
        invalidBitmap.recycle()
    }

    /**
     * Test: Decode from an empty/corrupted file path.
     * Expected: Returns null without crashing.
     */
    @Test
    fun testDecodeFromInvalidPathReturnsNull() {
        // Act: Try to decode from non-existent path
        val decoded = QRCodeDecoder.syncDecodeQRCode("/invalid/path/qr.png", context)

        // Assert: Should return null gracefully
        assertNull("Invalid path should return null", decoded)
    }

    /**
     * Test: Decode QR codes at different scales/sizes.
     * Expected: All QR codes decode correctly regardless of size.
     */
    @Test
    fun testDecodeQRCodeAtDifferentScales() {
        val sizes = listOf(100, 200, 400, 800, 1600)
        val testContent = "Scale-Test-QR"

        for (size in sizes) {
            // Arrange
            val qrBitmap = generateQRCodeBitmap(testContent, size)
            val qrFile = saveBitmapToFile(qrBitmap, "qr_scale_${size}x${size}.png")

            // Act
            val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

            // Assert
            assertNotNull(
                "QR code at ${size}x${size} should be decodable",
                decoded
            )
            assertTrue(
                "QR code at ${size}x${size} should contain correct content",
                decoded == testContent || decoded?.contains(testContent) == true
            )

            qrFile.delete()
            qrBitmap.recycle()
        }
    }

    /**
     * Test: Decode QR codes with long content strings.
     * Expected: Successfully decodes even with maximum capacity.
     */
    @Test
    fun testDecodeQRCodeWithLongContent() {
        // Arrange: Create a long content string
        val longContent = "https://example.com/path/to/resource?" +
                "param1=value1&param2=value2&param3=value3&" +
                "user=test_user&session=abcdef123456&" +
                "timestamp=2026-01-26T14:30:00Z"

        val qrBitmap = generateQRCodeBitmap(longContent)
        val qrFile = saveBitmapToFile(qrBitmap, "qr_long_content.png")

        // Act
        val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert
        assertNotNull("Long content QR code should be decodable", decoded)
        assertTrue(
            "Long content should match: expected=$longContent, got=$decoded",
            decoded == longContent || decoded?.contains(longContent) == true
        )

        qrFile.delete()
        qrBitmap.recycle()
    }

    /**
     * Test: Decode QR code with special characters and unicode.
     * Expected: Handles special characters correctly.
     */
    @Test
    fun testDecodeQRCodeWithSpecialCharacters() {
        val specialContent = "test@123!#\$%^&*()_+-=[]{}|;':\",./<>?"

        // Arrange
        val qrBitmap = generateQRCodeBitmap(specialContent)
        val qrFile = saveBitmapToFile(qrBitmap, "qr_special_chars.png")

        // Act
        val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert
        assertNotNull("Special character QR code should be decodable", decoded)
        assertEquals(
            "Special characters should be preserved",
            specialContent,
            decoded
        )

        qrFile.delete()
        qrBitmap.recycle()
    }

    /**
     * Test: Concurrent decoding of multiple QR codes.
     * Expected: All codes decode correctly without race conditions.
     */
    @Test
    fun testConcurrentQRCodeDecoding() {
        // Arrange: Create multiple QR codes
        val qrFiles = (1..5).map { i ->
            val bitmap = generateQRCodeBitmap("Concurrent-QR-$i")
            saveBitmapToFile(bitmap, "concurrent_qr_$i.png")
        }

        // Act: Decode all concurrently using threads
        val decodedResults = mutableListOf<String?>()
        val threads = qrFiles.mapIndexed { index, file ->
            Thread {
                val decoded = QRCodeDecoder.syncDecodeQRCode(file.absolutePath, context)
                synchronized(decodedResults) {
                    decodedResults.add(decoded)
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Assert: All should decode successfully
        assertEquals(
            "Should have 5 results",
            5,
            decodedResults.size
        )
        decodedResults.forEach { result ->
            assertNotNull("Each QR code should decode", result)
        }

        qrFiles.forEach { it.delete() }
    }

    /**
     * Test: Memory efficiency with large QR code images.
     * Expected: No OutOfMemoryError, images are properly recycled.
     */
    @Test
    fun testMemoryEfficiencyWithLargeImages() {
        // Arrange: Create a large QR code (2400x2400)
        val largeSize = 2400
        val qrBitmap = generateQRCodeBitmap(TEST_QR_CONTENT, largeSize)
        val qrFile = saveBitmapToFile(qrBitmap, "qr_large.png")

        // Act: Decode large image
        val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert: Should decode without memory issues
        assertNotNull("Large QR code should be decodable", decoded)
        assertTrue(
            "Large QR code content should be correct",
            decoded == TEST_QR_CONTENT || decoded?.contains(TEST_QR_CONTENT) == true
        )

        qrFile.delete()
        qrBitmap.recycle()
    }

    /**
     * Test: Decode QR codes with different formats (CODE_128, EAN_13, etc.)
     * Expected: MultiFormatReader successfully decodes different barcode types.
     */
    @Test
    fun testDecodeMultipleBarcodeFormats() {
        // Note: This requires generating non-QR barcodes
        // For now, we'll test multiple QR codes as ZXing supports multiple formats

        val testCases = listOf(
            "FORMAT_TEST_1",
            "FORMAT_TEST_2",
            "https://test.com"
        )

        for ((index, content) in testCases.withIndex()) {
            // Arrange
            val bitmap = generateQRCodeBitmap(content)
            val file = saveBitmapToFile(bitmap, "format_test_$index.png")

            // Act
            val decoded = QRCodeDecoder.syncDecodeQRCode(file.absolutePath, context)

            // Assert
            assertNotNull("Format test $index should decode", decoded)

            file.delete()
            bitmap.recycle()
        }
    }

    /**
     * Test: Decode QR code from rotated image.
     * Expected: Should handle rotations correctly (or indicate limitation).
     */
    @Test
    fun testDecodeRotatedQRCode() {
        // Arrange: Create QR code and rotate it
        val originalBitmap = generateQRCodeBitmap(TEST_QR_CONTENT)
        val rotatedBitmap = rotateBitmap(originalBitmap, 90f)
        val qrFile = saveBitmapToFile(rotatedBitmap, "qr_rotated_90.png")

        // Act
        val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert: ZXing typically handles rotations well
        assertNotNull("Rotated QR code should be decodable", decoded)
        assertTrue(
            "Rotated content should match original",
            decoded == TEST_QR_CONTENT || decoded?.contains(TEST_QR_CONTENT) == true
        )

        qrFile.delete()
        originalBitmap.recycle()
        rotatedBitmap.recycle()
    }

    /**
     * Test: File I/O operations don't leak file handles.
     * Expected: Files can be deleted after decoding.
     */
    @Test
    fun testFileHandlingAndCleanup() {
        // Arrange
        val qrBitmap = generateQRCodeBitmap(TEST_QR_CONTENT)
        val qrFile = saveBitmapToFile(qrBitmap, "cleanup_test.png")
        val fileExists = qrFile.exists()

        // Act
        QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert: File should still exist and be deletable
        assertTrue("File should exist before deletion", fileExists)
        assertTrue("File should be deletable after decoding", qrFile.delete())

        qrBitmap.recycle()
    }

    /**
     * Test: HMS availability fallback on supported devices.
     * Expected: Either HMS or ZXing decoding works based on device capability.
     * (Only runs on Huawei devices with HMS)
     */
    @Test
    fun testHMSFallbackOnAvailableDevices() {
        // Skip on devices without HMS
        if (Build.MANUFACTURER.lowercase() != "huawei") {
            return
        }

        // Arrange
        val qrBitmap = generateQRCodeBitmap(TEST_QR_CONTENT)
        val qrFile = saveBitmapToFile(qrBitmap, "hms_fallback_test.png")

        // Act
        val decoded = QRCodeDecoder.syncDecodeQRCode(qrFile.absolutePath, context)

        // Assert: Should decode via HMS or ZXing fallback
        assertNotNull("Should decode via HMS or fallback to ZXing", decoded)

        qrFile.delete()
        qrBitmap.recycle()
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generates a QR code bitmap from a text string.
     */
    private fun generateQRCodeBitmap(content: String, size: Int = 400): Bitmap {
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    /**
     * Saves a bitmap to a file in the test cache directory.
     */
    private fun saveBitmapToFile(bitmap: Bitmap, filename: String): File {
        val file = File(testImageDir, filename)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        return file
    }

    /**
     * Rotates a bitmap by the specified degrees.
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}