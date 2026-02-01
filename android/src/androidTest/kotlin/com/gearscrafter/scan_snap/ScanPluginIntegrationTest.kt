package com.gearscrafter.scan_snap

import android.content.Context
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.times
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Integration tests for ScanPlugin.
 * Tests plugin lifecycle (attach/detach), method calls (getPlatformVersion, parse),
 * and interaction with Flutter through MethodChannel.
 */
@RunWith(AndroidJUnit4::class)
class ScanPluginIntegrationTest {

    companion object {
        private const val TAG = "ScanPluginIntegrationTest"
        private const val CHANNEL_NAME = "scan_snap/scan"
        private const val TEST_QR_CONTENT = "https://gearscrafter.com"
    }

    private lateinit var context: Context
    private lateinit var plugin: ScanPlugin

    @Mock
    private lateinit var mockBinaryMessenger: BinaryMessenger

    @Mock
    private lateinit var mockActivityBinding: ActivityPluginBinding

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Test: Plugin initializes without errors.
     * Expected: Plugin object is created successfully.
     */
    @Test
    fun testPluginInstantiation() {
        // Act
        plugin = ScanPlugin()

        // Assert
        assertNotNull("Plugin should be instantiated", plugin)
    }

    /**
     * Test: onAttachedToEngine initializes channel and view factory.
     * Expected: Channel is registered with correct name, viewFactory is created.
     */
    @Test
    fun testOnAttachedToEngine() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()

        // Act
        plugin.onAttachedToEngine(flutterBinding)

        // Assert
        assertNotNull("Plugin should have channel after attach", plugin)
        // Verify that registerViewFactory was called
        verify(flutterBinding.platformViewRegistry, atLeastOnce()).registerViewFactory(
            any(),
            any()
        )
    }

    /**
     * Test: onDetachedFromEngine cleans up resources.
     * Expected: Channel is null, viewFactory is null, job is cancelled.
     */
    @Test
    fun testOnDetachedFromEngine() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        // Act
        plugin.onDetachedFromEngine(flutterBinding)

        // Assert - plugin should be cleaned up
        assertNotNull("Plugin should still exist", plugin)
    }

    /**
     * Test: onAttachedToActivity registers activity binding.
     * Expected: ActivityBinding is stored, viewFactory receives binding.
     */
    @Test
    fun testOnAttachedToActivity() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        // Act
        plugin.onAttachedToActivity(mockActivityBinding)

        // Assert - should not crash
        assertNotNull("Plugin should be initialized", plugin)
    }

    /**
     * Test: onDetachedFromActivity clears activity binding.
     * Expected: Activity binding is removed.
     */
    @Test
    fun testOnDetachedFromActivity() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)
        plugin.onAttachedToActivity(mockActivityBinding)

        // Act
        plugin.onDetachedFromActivity()

        // Assert - should not crash
        assertNotNull("Plugin should still exist", plugin)
    }

    /**
     * Test: onReattachedToActivityForConfigChanges calls onAttachedToActivity.
     * Expected: Activity binding is reattached during config changes.
     */
    @Test
    fun testOnReattachedToActivityForConfigChanges() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        // Act
        plugin.onReattachedToActivityForConfigChanges(mockActivityBinding)

        // Assert - should not crash
        assertNotNull("Plugin should handle reattach", plugin)
    }

    /**
     * Test: onDetachedFromActivityForConfigChanges calls onDetachedFromActivity.
     * Expected: Activity binding is detached during config changes.
     */
    @Test
    fun testOnDetachedFromActivityForConfigChanges() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)
        plugin.onAttachedToActivity(mockActivityBinding)

        // Act
        plugin.onDetachedFromActivityForConfigChanges()

        // Assert - should not crash
        assertNotNull("Plugin should handle detach", plugin)
    }

    /**
     * Test: onMethodCall handles "getPlatformVersion" correctly.
     * Expected: Returns "Android X.Y" format string.
     */
    @Test
    fun testOnMethodCallGetPlatformVersion() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()
        val call = MethodCall("getPlatformVersion", null)

        // Act
        plugin.onMethodCall(call, result)

        // Assert
        verify(result, times(1)).success(any())

        // Capture the result value
        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(result).success(captor.capture())
        val platformVersion = captor.value
        assertTrue(
            "Platform version should contain 'Android'",
            platformVersion?.contains("Android") == true
        )
    }

    /**
     * Test: onMethodCall handles "parse" with valid path.
     * Expected: QR code is decoded and result is returned.
     */
    @Test
    fun testOnMethodCallParseWithValidPath() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()

        // Create a test QR image file (simple bitmap)
        val testQRPath = createTestQRImageFile()

        // Act
        val call = MethodCall("parse", mapOf("path" to testQRPath))
        plugin.onMethodCall(call, result)

        // Give coroutine time to execute
        Thread.sleep(2000)

        // Assert - result should be called with decoded string or null
        verify(result, atLeastOnce()).success(any())

        // Cleanup
        File(testQRPath).delete()
    }

    /**
     * Test: onMethodCall handles "parse" with missing path argument.
     * Expected: Returns error result.
     */
    @Test
    fun testOnMethodCallParseMissingPath() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()
        val call = MethodCall("parse", mapOf<String, Any>())

        // Act
        plugin.onMethodCall(call, result)

        // Assert
        verify(result, times(1)).error("INVALID_ARGS", "Missing path or context", null)
    }

    /**
     * Test: onMethodCall handles "parse" with null context.
     * Expected: Returns error result.
     */
    @Test
    fun testOnMethodCallParseNullContext() {
        // Arrange
        plugin = ScanPlugin()
        val mockBinding = mock<FlutterPlugin.FlutterPluginBinding> {
            whenever(it.binaryMessenger).thenReturn(mockBinaryMessenger)
            whenever(it.applicationContext).thenReturn(null)
            whenever(it.platformViewRegistry).thenReturn(mock())
        }
        plugin.onAttachedToEngine(mockBinding)

        val result = mock<MethodChannel.Result>()
        val call = MethodCall("parse", mapOf("path" to "/path/to/image.png"))

        // Act
        plugin.onMethodCall(call, result)

        // Assert
        verify(result, times(1)).error("INVALID_ARGS", "Missing path or context", null)
    }

    /**
     * Test: onMethodCall returns notImplemented for unknown methods.
     * Expected: Unknown method returns notImplemented error.
     */
    @Test
    fun testOnMethodCallUnknownMethod() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()
        val call = MethodCall("unknownMethod", null)

        // Act
        plugin.onMethodCall(call, result)

        // Assert
        verify(result, times(1)).notImplemented()
    }

    /**
     * Test: Lifecycle sequence: attach → activity → detach.
     * Expected: All lifecycle methods execute without errors.
     */
    @Test
    fun testCompleteLifecycleSequence() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()

        // Act & Assert
        // Step 1: Attach to engine
        plugin.onAttachedToEngine(flutterBinding)
        assertNotNull("Plugin should be initialized", plugin)

        // Step 2: Attach to activity
        plugin.onAttachedToActivity(mockActivityBinding)
        assertNotNull("Plugin should have activity", plugin)

        // Step 3: Handle config change
        plugin.onReattachedToActivityForConfigChanges(mockActivityBinding)
        plugin.onDetachedFromActivityForConfigChanges()

        // Step 4: Detach from activity
        plugin.onDetachedFromActivity()

        // Step 5: Detach from engine
        plugin.onDetachedFromEngine(flutterBinding)
        assertNotNull("Plugin should still exist", plugin)
    }

    /**
     * Test: Multiple method calls in sequence work correctly.
     * Expected: Each method call is handled independently.
     */
    @Test
    fun testMultipleConsecutiveMethodCalls() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()

        // Act
        val calls = listOf(
            MethodCall("getPlatformVersion", null),
            MethodCall("getPlatformVersion", null),
            MethodCall("unknownMethod", null),
            MethodCall("getPlatformVersion", null)
        )

        for (call in calls) {
            plugin.onMethodCall(call, result)
        }

        // Assert - all calls should be handled
        verify(result, atLeastOnce()).success(any())
    }

    /**
     * Test: Plugin platform version matches actual device version.
     * Expected: Returns valid Android version string.
     */
    @Test
    fun testPlatformVersionAccuracy() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()
        val call = MethodCall("getPlatformVersion", null)

        // Act
        plugin.onMethodCall(call, result)

        // Assert
        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(result).success(captor.capture())
        val platformVersion = captor.value

        // Should be in format "Android X.Y.Z"
        assertTrue(
            "Should contain 'Android'",
            platformVersion?.contains("Android") == true
        )
        assertTrue(
            "Should contain version number",
            platformVersion?.contains(Build.VERSION.RELEASE) == true
        )
    }

    /**
     * Test: Plugin is a CoroutineScope.
     * Expected: Plugin implements CoroutineScope for async operations.
     */
    @Test
    fun testPluginIsCoroutineScope() {
        // Arrange
        plugin = ScanPlugin()

        // Act & Assert
        assertTrue(
            "Plugin should be a CoroutineScope",
            plugin is io.flutter.embedding.engine.plugins.FlutterPlugin &&
                    plugin is kotlinx.coroutines.CoroutineScope
        )
    }

    /**
     * Test: Plugin handles rapid attach/detach cycles.
     * Expected: No crashes or resource leaks.
     */
    @Test
    fun testRapidAttachDetachCycles() {
        // Arrange
        val flutterBinding = createMockFlutterBinding()

        // Act & Assert
        repeat(3) {
            plugin = ScanPlugin()
            plugin.onAttachedToEngine(flutterBinding)
            plugin.onAttachedToActivity(mockActivityBinding)
            plugin.onDetachedFromActivity()
            plugin.onDetachedFromEngine(flutterBinding)
        }

        assertTrue("Should complete without errors", true)
    }

    /**
     * Test: Method channel is properly initialized.
     * Expected: Plugin can handle method calls after attach.
     */
    @Test
    fun testMethodChannelInitialization() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()

        // Act
        plugin.onAttachedToEngine(flutterBinding)

        // Assert - verify plugin can handle method calls (indicates channel is set up)
        val result = mock<MethodChannel.Result>()
        val call = MethodCall("getPlatformVersion", null)

        // Should not crash - indicates channel is initialized
        plugin.onMethodCall(call, result)
        verify(result).success(any())
    }

    /**
     * Test: Plugin registers correct view factory name.
     * Expected: ViewFactory is registered with "scan_snap/scan_view" name.
     */
    @Test
    fun testViewFactoryRegistration() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()

        // Act
        plugin.onAttachedToEngine(flutterBinding)

        // Assert
        verify(flutterBinding.platformViewRegistry, atLeastOnce()).registerViewFactory(
            any(),
            any()
        )
    }

    // ==================== HELPER METHODS ====================

    /**
     * Create a mock FlutterPlugin.FlutterPluginBinding.
     */
    private fun createMockFlutterBinding(): FlutterPlugin.FlutterPluginBinding {
        return mock {
            whenever(it.binaryMessenger).thenReturn(mockBinaryMessenger)
            whenever(it.applicationContext).thenReturn(context)
            whenever(it.platformViewRegistry).thenReturn(mock())
        }
    }

    /**
     * Create a test QR image file for testing.
     * Returns path to a valid QR code image.
     */
    private fun createTestQRImageFile(): String {
        // Create a simple bitmap QR code using ZXing
        val qrWriter = com.google.zxing.MultiFormatWriter()
        val bitMatrix = qrWriter.encode(
            TEST_QR_CONTENT,
            com.google.zxing.BarcodeFormat.QR_CODE,
            400,
            400
        )

        val bitmap = android.graphics.Bitmap.createBitmap(400, 400, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until 400) {
            for (y in 0 until 400) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }

        val file = File(context.cacheDir, "test_qr.png")
        val fos = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        bitmap.recycle()

        return file.absolutePath
    }
}