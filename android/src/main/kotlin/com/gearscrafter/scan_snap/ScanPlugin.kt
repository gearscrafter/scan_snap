package com.gearscrafter.scan_snap

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Main plugin class that bridges Flutter and native Android.
 * Registers the platform view for scanning and handles method calls from Dart.
 */
class ScanPlugin : FlutterPlugin, ActivityAware, MethodChannel.MethodCallHandler, CoroutineScope {

    private var channel: MethodChannel? = null
    private var flutterBinding: FlutterPlugin.FlutterPluginBinding? = null
    private var activityBinding: ActivityPluginBinding? = null
    private var viewFactory: ScanViewFactory? = null
   
    // Coroutine scope for background processing
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * Called when the plugin is attached to the Flutter engine.
     * Initializes the method channel and registers the platform view.
     */
    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        flutterBinding = binding
        channel = MethodChannel(binding.binaryMessenger, "scan_snap/scan")
        channel?.setMethodCallHandler(this)
        viewFactory = ScanViewFactory(binding.binaryMessenger)
        binding.platformViewRegistry.registerViewFactory("scan_snap/scan_view", viewFactory!!)
    }

    /**
     * Called when the plugin is detached from the Flutter engine.
     * Cleans up resources.
     */
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
        flutterBinding = null
        viewFactory = null
        job.cancel()
    }

    /**
     * Called when the plugin is attached to an Activity.
     * Needed for accessing the Activity and registering lifecycle-aware views.
     */
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        viewFactory?.setActivityBinding(binding)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromActivity() {
        viewFactory?.setActivityBinding(null)
        activityBinding = null
    }

    /**
     * Handles method calls from Dart (Flutter side).
     */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            // Returns the current Android OS version
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }
            // Decodes a QR code from an image file path
            "parse" -> {
                val path = call.argument<String>("path")
                val context = flutterBinding?.applicationContext

                if (path != null && context != null) {
                    launch {
                        val decoded = withContext(Dispatchers.IO) {
                            QRCodeDecoder.syncDecodeQRCode(path)
                        }
                        vibrateIfNeeded(context, decoded)
                        result.success(decoded)
                    }
                } else {
                    result.error("INVALID_ARGS", "Missing path or context", null)
                }
            }

            else -> result.notImplemented()
        }
    }

    /**
     * Triggers a vibration when a QR code is successfully decoded.
     */
    private fun vibrateIfNeeded(context: Context, decoded: String?) {
        if (!decoded.isNullOrEmpty()) {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(50)
                }
            }
        }
    }
}
