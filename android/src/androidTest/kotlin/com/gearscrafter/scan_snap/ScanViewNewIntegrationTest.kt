package com.gearscrafter.scan_snap

import android.Manifest
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for ScanViewNew - Permission Tests Only
 */
@RunWith(AndroidJUnit4::class)
class ScanViewNewIntegrationTest {

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    private lateinit var context: Context

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Test: Camera permission is granted by @GrantPermissionRule.
     * Expected: CAMERA permission is available.
     */
    @Test
    fun testCameraPermissionIsGranted() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        )

        assertEquals(
            "Camera permission should be granted by @GrantPermissionRule",
            android.content.pm.PackageManager.PERMISSION_GRANTED,
            hasPermission
        )
    }

    /**
     * Test: Camera permission is required in manifest.
     * Expected: CAMERA permission declared in AndroidManifest.xml.
     */
    @Test
    fun testCameraPermissionInManifest() {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_PERMISSIONS
        )

        val permissions = packageInfo.requestedPermissions ?: arrayOf()
        assertTrue(
            "CAMERA permission should be declared in manifest",
            permissions.contains(CAMERA_PERMISSION)
        )
    }

    /**
     * Test: Permission check is consistent across multiple calls.
     * Expected: All permission checks return the same result.
     */
    @Test
    fun testPermissionCheckConsistency() {
        val check1 = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
        val check2 = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
        val check3 = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)

        assertEquals("All checks should be identical", check1, check2)
        assertEquals("All checks should be identical", check2, check3)
        assertEquals(
            "Permission should be granted",
            android.content.pm.PackageManager.PERMISSION_GRANTED,
            check1
        )
    }
}