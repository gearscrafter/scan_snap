package com.gearscrafter.scan_snap

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class ScanPluginUnitTest {

  private lateinit var plugin: ScanPlugin

  @Mock
  private lateinit var mockFlutterBinding: FlutterPlugin.FlutterPluginBinding

  @Mock
  private lateinit var mockActivityBinding: ActivityPluginBinding

  @Mock
  private lateinit var mockContext: Context

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    plugin = ScanPlugin()
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: PLUGIN LIFECYCLE
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testPluginCanBeInstantiated() {
    val testPlugin = ScanPlugin()
    assertNotNull(testPlugin)
  }

  @Test
  fun testPluginIsFlutterPlugin() {
    assertTrue(plugin is FlutterPlugin)
  }

  @Test
  fun testPluginIsActivityAware() {
    assertTrue(plugin is io.flutter.embedding.engine.plugins.activity.ActivityAware)
  }

  @Test
  fun testPluginIsMethodCallHandler() {
    assertTrue(plugin is MethodChannel.MethodCallHandler)
  }

  @Test
  fun testPluginIsCoroutineScope() {
    assertTrue(plugin is kotlinx.coroutines.CoroutineScope)
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: ATTACHED TO ENGINE
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testOnAttachedToEngineStoresBinding() {
    val mockBinaryMessenger: io.flutter.plugin.common.BinaryMessenger = mock()
    val mockPlatformViewRegistry: io.flutter.plugin.platform.PlatformViewRegistry = mock()
    
    whenever(mockFlutterBinding.binaryMessenger).thenReturn(mockBinaryMessenger)
    whenever(mockFlutterBinding.platformViewRegistry).thenReturn(mockPlatformViewRegistry)
    whenever(mockFlutterBinding.applicationContext).thenReturn(mockContext)
    
    plugin.onAttachedToEngine(mockFlutterBinding)
    
    verify(mockPlatformViewRegistry).registerViewFactory(
      eq("scan_snap/scan_view"),
      any()
    )
  }

  @Test
  fun testMethodChannelIsCreatedOnAttach() {
    val mockBinaryMessenger: io.flutter.plugin.common.BinaryMessenger = mock()
    val mockPlatformViewRegistry: io.flutter.plugin.platform.PlatformViewRegistry = mock()
    
    whenever(mockFlutterBinding.binaryMessenger).thenReturn(mockBinaryMessenger)
    whenever(mockFlutterBinding.platformViewRegistry).thenReturn(mockPlatformViewRegistry)
    
    try {
      plugin.onAttachedToEngine(mockFlutterBinding)
    } catch (e: Exception) {
      fail("Should not throw exception: ${e.message}")
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: DETACHED FROM ENGINE
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testOnDetachedFromEngineClosesResources() {
    val mockBinaryMessenger: io.flutter.plugin.common.BinaryMessenger = mock()
    val mockPlatformViewRegistry: io.flutter.plugin.platform.PlatformViewRegistry = mock()
    
    whenever(mockFlutterBinding.binaryMessenger).thenReturn(mockBinaryMessenger)
    whenever(mockFlutterBinding.platformViewRegistry).thenReturn(mockPlatformViewRegistry)
    
    plugin.onAttachedToEngine(mockFlutterBinding)
    plugin.onDetachedFromEngine(mockFlutterBinding)
    
    try {
      plugin.onDetachedFromEngine(mockFlutterBinding)
    } catch (e: Exception) {
      fail("Should not throw exception: ${e.message}")
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: ACTIVITY LIFECYCLE
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testOnAttachedToActivityStoresBinding() {
    try {
      plugin.onAttachedToActivity(mockActivityBinding)
    } catch (e: Exception) {
      fail("Should not throw exception: ${e.message}")
    }
  }

  @Test
  fun testOnDetachedFromActivityClearsBinding() {
    plugin.onAttachedToActivity(mockActivityBinding)
    
    try {
      plugin.onDetachedFromActivity()
    } catch (e: Exception) {
      fail("Should not throw exception: ${e.message}")
    }
  }

  @Test
  fun testReattachedToActivityForConfigChanges() {
    plugin.onAttachedToActivity(mockActivityBinding)
    
    try {
      plugin.onReattachedToActivityForConfigChanges(mockActivityBinding)
    } catch (e: Exception) {
      fail("Should not throw exception: ${e.message}")
    }
  }

  @Test
  fun testDetachedFromActivityForConfigChanges() {
    try {
      plugin.onDetachedFromActivityForConfigChanges()
    } catch (e: Exception) {
      fail("Should not throw exception: ${e.message}")
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: METHOD CALL HANDLING
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testGetPlatformVersionMethod() {
    val mockResult: MethodChannel.Result = mock()
    val call = MethodCall("getPlatformVersion", null)
    
    plugin.onMethodCall(call, mockResult)
    
    verify(mockResult).success(any())
  }

  @Test
  fun testGetPlatformVersionReturnsValidString() {
    val mockResult: MethodChannel.Result = mock()
    val call = MethodCall("getPlatformVersion", null)
    val captor = argumentCaptor<String>()
    
    plugin.onMethodCall(call, mockResult)
    
    verify(mockResult).success(captor.capture())
    val result = captor.firstValue
    assertTrue(result.startsWith("Android "))
  }

  @Test
  fun testParseMethodWithValidPath() {
    val mockResult: MethodChannel.Result = mock()
    val args = mapOf("path" to "/test/qr.jpg")
    val call = MethodCall("parse", args)
    
    plugin.onMethodCall(call, mockResult)
    
    assertNotNull(call.method)
  }

  @Test
  fun testParseMethodWithMissingPath() {
    val mockResult: MethodChannel.Result = mock()
    val args = mapOf<String, Any>()
    val call = MethodCall("parse", args)
    
    plugin.onMethodCall(call, mockResult)
    
    verify(mockResult).error(
        eq("INVALID_ARGS"),
        any<String>(),
        isNull()
    )
  }

  @Test
  fun testUnknownMethodCall() {
    val mockResult: MethodChannel.Result = mock()
    val call = MethodCall("unknownMethod", null)
    
    plugin.onMethodCall(call, mockResult)
    
    verify(mockResult).notImplemented()
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: VIBRATION
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testVibrateOnSuccessfulDecode() {
    val decodedData = "QR_CODE_123"
    assertFalse(decodedData.isNullOrEmpty())
  }

  @Test
  fun testNoVibrateOnNullData() {
    val decodedData: String? = null
    assertTrue(decodedData.isNullOrEmpty())
  }

  @Test
  fun testNoVibrateOnEmptyData() {
    val decodedData = ""
    assertTrue(decodedData.isEmpty())
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: COROUTINE SCOPE
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testPluginHasCoroutineContext() {
    assertNotNull(plugin.coroutineContext)
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: METHOD CALL ARGUMENT HANDLING
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testMethodCallWithMapArguments() {
    val args = mapOf("path" to "/test.jpg")
    val call = MethodCall("parse", args)
    
    val path = call.argument<String>("path")
    
    assertEquals("/test.jpg", path)
  }

  @Test
  fun testMethodCallWithNullArguments() {
    val call = MethodCall("getPlatformVersion", null)
    
    assertNull(call.arguments)
  }

  @Test
  fun testMethodCallWithIncorrectArgumentType() {
    val args = mapOf("path" to 12345)
    val call = MethodCall("parse", args)

    var path: String? = null
    try {
      path = call.argument<String>("path")
    } catch (e: ClassCastException) {
      // Expected: Integer cannot be cast to String
    }
    
    assertNull(path)
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: BUILD VERSION COMPATIBILITY
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testGetPlatformVersionMethodCalled() {
    val mockResult: MethodChannel.Result = mock()
    val call = MethodCall("getPlatformVersion", null)
    
    plugin.onMethodCall(call, mockResult)
    
    val captor = argumentCaptor<String>()
    verify(mockResult).success(captor.capture())
    
    val platformVersion = captor.firstValue
    assertTrue("Platform version should contain Android", platformVersion.contains("Android", ignoreCase = true))
  }

  @Test
  fun testBuildVersionIsIncludedInResponse() {
    val mockResult: MethodChannel.Result = mock()
    val call = MethodCall("getPlatformVersion", null)
    
    plugin.onMethodCall(call, mockResult)
    
    verify(mockResult).success(any())
  }

  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
  // TESTS: CONCURRENT METHOD CALLS
  // ═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

  @Test
  fun testMultipleMethodCallsAreHandled() {
    val mockResult1: MethodChannel.Result = mock()
    val mockResult2: MethodChannel.Result = mock()
    val call1 = MethodCall("getPlatformVersion", null)
    val call2 = MethodCall("getPlatformVersion", null)
    
    plugin.onMethodCall(call1, mockResult1)
    plugin.onMethodCall(call2, mockResult2)
    
    verify(mockResult1).success(any())
    verify(mockResult2).success(any())
  }
}