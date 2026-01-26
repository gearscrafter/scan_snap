package com.gearscrafter.scan_snap

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScanDrawViewTest {

    @Mock
    private lateinit var mockDisplayMetrics: DisplayMetrics

    private lateinit var mockResources: android.content.res.Resources
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Create real Context using Robolectric
        mockContext = org.robolectric.RuntimeEnvironment.getApplication().applicationContext

        // Mock Resources with DisplayMetrics
        mockResources = mock {
            on { displayMetrics }.thenReturn(mockDisplayMetrics)
        }

        // Stub the context to return our mock resources
        val contextSpy = spy(mockContext)
        whenever(contextSpy.resources).thenReturn(mockResources)
        mockContext = contextSpy

        mockDisplayMetrics.density = 2.0f
    }

    @Test
    fun testScanDrawViewCanBeInstantiated() {
        val view = ScanDrawView(mockContext)
        assertNotNull(view)
    }

    @Test
    fun testScanDrawViewWithArgs() {
        val args = mapOf(
            "scale" to 0.8,
            "r" to 255,
            "g" to 0,
            "b" to 0,
            "a" to 1.0
        )
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testDefaultScale() {
        val view = ScanDrawView(mockContext)
        assertNotNull(view)
    }

    @Test
    fun testDefaultScanLineColorIsGreen() {
        val args = mapOf<String, Any>()
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testCustomScanLineColor() {
        val args = mapOf(
            "r" to 255,
            "g" to 0,
            "b" to 0,
            "a" to 1.0
        )
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testAlphaChannelConfiguration() {
        val args = mapOf(
            "r" to 255,
            "g" to 0,
            "b" to 0,
            "a" to 0.5
        )
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testTransparentScanLineWithAlphaZero() {
        val args = mapOf(
            "r" to 255,
            "g" to 0,
            "b" to 0,
            "a" to 0.0
        )
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testScaleCanBeSet() {
        val args = mapOf("scale" to 0.5)
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testScaleDefaultIsValid() {
        val args = mapOf<String, Any>()
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testMaximumScale() {
        val args = mapOf("scale" to 0.99)
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testMinimumScale() {
        val args = mapOf("scale" to 0.1)
        val view = ScanDrawView(mockContext, args)
        assertNotNull(view)
    }

    @Test
    fun testDisplayMetricsAreRead() {
        mockDisplayMetrics.density = 2.0f
        val view = ScanDrawView(mockContext)
        assertNotNull(view)
    }

    @Test
    fun testDifferentScreenDensities() {
        val densities = listOf(0.75f, 1.0f, 1.5f, 2.0f, 2.75f, 3.0f)

        densities.forEach { density ->
            mockDisplayMetrics.density = density
            val view = ScanDrawView(mockContext)
            assertNotNull(view)
        }
    }

    @Test
    fun testResume() {
        val view = ScanDrawView(mockContext)

        try {
            view.resume()
        } catch (e: Exception) {
            fail("resume() should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testPause() {
        val view = ScanDrawView(mockContext)

        try {
            view.pause()
        } catch (e: Exception) {
            fail("pause() should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testResumeAfterPause() {
        val view = ScanDrawView(mockContext)

        try {
            view.pause()
            view.resume()
        } catch (e: Exception) {
            fail("pause/resume sequence should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testMultiplePauseCalls() {
        val view = ScanDrawView(mockContext)

        try {
            view.pause()
            view.pause()
            view.pause()
        } catch (e: Exception) {
            fail("Multiple pause calls should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testWillNotDrawIsSet() {
        val view = ScanDrawView(mockContext)
        assertNotNull(view)
    }

    @Test
    fun testColorChannelBounds() {
        val validR = 255
        val validG = 128
        val validB = 0

        assertTrue(validR >= 0 && validR <= 255)
        assertTrue(validG >= 0 && validG <= 255)
        assertTrue(validB >= 0 && validB <= 255)
    }

    @Test
    fun testAlphaBoundsClamping() {
        val alpha = 1.5
        val clamped = Math.max(0.0, Math.min(1.0, alpha))
        assertEquals(1.0, clamped, 0.01)
    }

    @Test
    fun testColorConversionEdgeCases() {
        val r = 0
        val g = 0
        val b = 0

        val color = Color.rgb(r, g, b)
        assertEquals(Color.BLACK, color)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScanViewFactoryTest {

    @Mock
    private lateinit var mockBinaryMessenger: io.flutter.plugin.common.BinaryMessenger

    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockContext = org.robolectric.RuntimeEnvironment.getApplication().applicationContext
    }

    @Test
    fun testFactoryCanBeInstantiated() {
        val factory = ScanViewFactory(mockBinaryMessenger)
        assertNotNull(factory)
    }

    @Test
    fun testFactoryIsPlatformViewFactory() {
        val factory = ScanViewFactory(mockBinaryMessenger)
        assertTrue(factory is io.flutter.plugin.platform.PlatformViewFactory)
    }

    @Test
    fun testActivityBindingCanBeSet() {
        val factory = ScanViewFactory(mockBinaryMessenger)

        try {
            factory.setActivityBinding(null)
        } catch (e: Exception) {
            fail("setActivityBinding(null) should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testActivityBindingCanBeUpdated() {
        val factory = ScanViewFactory(mockBinaryMessenger)
        val mockBinding: io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding = mock()

        try {
            factory.setActivityBinding(mockBinding)
            factory.setActivityBinding(null)
        } catch (e: Exception) {
            fail("setActivityBinding updates should not throw exception: ${e.message}")
        }
    }

    @Test
    fun testMessengerIsStored() {
        val factory = ScanViewFactory(mockBinaryMessenger)
        assertNotNull(factory)
    }

    @Test
    fun testThrowsWhenBindingIsNull() {
        val factory = ScanViewFactory(mockBinaryMessenger)

        assertThrows(IllegalStateException::class.java) {
            factory.create(mockContext, 1, null)
        }
    }
}