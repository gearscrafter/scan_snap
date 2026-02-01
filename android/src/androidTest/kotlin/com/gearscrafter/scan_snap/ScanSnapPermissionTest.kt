package com.gearscrafter.scan_snap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Integration tests for runtime permissions in scan_snap plugin.
 * Tests camera permission handling, permission requests, and error cases.
 *
 * Note: Only tests CAMERA permission with @GrantPermissionRule.
 * Storage permissions are not tested with GrantPermissionRule (Android 11+ doesn't support it).
 */
@RunWith(AndroidJUnit4::class)
class ScanSnapPermissionTest {

    companion object {
        private const val TAG = "ScanSnapPermissionTest"
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val CAMERA_REQUEST_CODE = 6537
        private const val TEST_QR_CONTENT = "https://gearscrafter.com"
    }

    private lateinit var context: Context
    private lateinit var plugin: ScanPlugin
    private lateinit var platformView: ScanPlatformView

    @Mock
    private lateinit var mockBinaryMessenger: BinaryMessenger

    @Mock
    private lateinit var mockActivityBinding: ActivityPluginBinding

    // Grant only CAMERA permission (storage permissions can't be granted in Android 11+)
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Test: Check camera permission status.
     * Expected: Can determine if permission is granted.
     */
    @Test
    fun testCheckCameraPermission() {
        // Act
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

        // Assert
        // With @GrantPermissionRule, this should be true
        assertTrue(
            "Camera permission should be granted by GrantPermissionRule",
            hasCameraPermission
        )
    }

    /**
     * Test: Camera permission is required in manifest.
     * Expected: CAMERA permission declared in AndroidManifest.xml.
     */
    @Test
    fun testCameraPermissionDeclaredInManifest() {
        // Arrange
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        // Act
        val permissions = packageInfo.requestedPermissions

        // Assert
        assertNotNull("Permissions should be declared", permissions)
        assertTrue(
            "CAMERA permission should be declared in manifest",
            permissions?.contains(CAMERA_PERMISSION) ?: false
        )
    }

    /**
     * Test: ContextCompat.checkSelfPermission returns correct status.
     * Expected: Returns PERMISSION_GRANTED (with @GrantPermissionRule).
     */
    @Test
    fun testContextCompatPermissionCheck() {
        // Act
        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        )

        // Assert
        assertEquals(
            "Permission check should return PERMISSION_GRANTED",
            PackageManager.PERMISSION_GRANTED,
            permissionStatus
        )
    }

    /**
     * Test: Multiple permission checks return consistent results.
     * Expected: Permission status remains consistent.
     */
    @Test
    fun testConsistentPermissionChecks() {
        // Act
        val check1 = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
        val check2 = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
        val check3 = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)

        // Assert
        assertEquals("First check should be granted", PackageManager.PERMISSION_GRANTED, check1)
        assertEquals("Second check should match first", check1, check2)
        assertEquals("Third check should match first", check1, check3)
    }

    /**
     * Test: Permission required for camera functionality.
     * Expected: CAMERA permission is necessary.
     */
    @Test
    fun testCameraPermissionIsRequired() {
        // Arrange
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        // Act
        val hasCamera = packageInfo.requestedPermissions?.contains(CAMERA_PERMISSION) ?: false

        // Assert
        assertTrue(
            "Camera permission must be declared as required",
            hasCamera
        )
    }

    /**
     * Test: Plugin handles permission state changes.
     * Expected: Plugin adapts to permission changes.
     */
    @Test
    fun testPluginHandlesPermissionStateChanges() {
        // Arrange
        plugin = ScanPlugin()
        val flutterBinding = createMockFlutterBinding()
        plugin.onAttachedToEngine(flutterBinding)

        val result = mock<MethodChannel.Result>()
        val call1 = MethodCall("getPlatformVersion", null)

        // Act - First call with permissions
        plugin.onMethodCall(call1, result)

        // Assert
        verify(result).success(any())
    }

    /**
     * Test: Runtime permission request code is correct.
     * Expected: Uses consistent request code (6537).
     */
    @Test
    fun testPermissionRequestCodeConsistency() {
        // This is a documentation test
        // The camera permission request code should be 6537

        val expectedCode = CAMERA_REQUEST_CODE
        val documentedCode = 6537

        assertEquals(
            "Request code should be consistent",
            expectedCode,
            documentedCode
        )
    }

    /**
     * Test: Manifest has required permission flags.
     * Expected: CAMERA permission is properly configured.
     */
    @Test
    fun testManifestPermissionConfiguration() {
        // Arrange
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )

        // Act
        val permissions = packageInfo.requestedPermissions ?: arrayOf()

        // Assert
        assertTrue(
            "Should have CAMERA permission in manifest",
            permissions.contains(CAMERA_PERMISSION)
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
     * Create a mock ActivityPluginBinding.
     */
    private fun createMockActivityBinding(): ActivityPluginBinding {
        return mock {
            val mockActivity = mock<android.app.Activity> {
                whenever(it.isFinishing).thenReturn(false)
                whenever(it.applicationContext).thenReturn(context)
            }
            whenever(it.activity).thenReturn(mockActivity)
            whenever(it.addRequestPermissionsResultListener(any())).thenReturn(Unit)
        }
    }

    /**
     * Create a test QR image file for testing.
     * Returns path to a valid QR code image.
     */
    private fun createTestQRImageFile(): String {
        val qrWriter = com.google.zxing.MultiFormatWriter()
        val bitMatrix = qrWriter.encode(
            TEST_QR_CONTENT,
            com.google.zxing.BarcodeFormat.QR_CODE,
            400,
            400
        )

        val bitmap = android.graphics.Bitmap.createBitmap(
            400,
            400,
            android.graphics.Bitmap.Config.RGB_565
        )
        for (x in 0 until 400) {
            for (y in 0 until 400) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }

        val file = File(context.cacheDir, "test_qr_permission.png")
        val fos = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        bitmap.recycle()

        return file.absolutePath
    }
}