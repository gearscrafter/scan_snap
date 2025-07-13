package com.gearscrafter.scan_snap

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

/**
 * Flutter PlatformView implementation that manages the native camera preview and overlay.
 * Communicates with Flutter through a MethodChannel and handles lifecycle events, permissions, and QR capture results.
 */
class ScanPlatformView(
    messenger: BinaryMessenger,
    private val context: Context,
    private val activity: Activity,
    binding: ActivityPluginBinding,
    viewId: Int,
    args: Map<String, Any>?
) : PlatformView, MethodChannel.MethodCallHandler, ScanViewNew.CaptureListener {

    companion object {
        private const val CAMERA_REQUEST_CODE = 6537
        private const val TAG = "ScanPlatformView"
    }

    private val channel = MethodChannel(messenger, "scan_snap/scan/method_$viewId")
    private var flashlightOn = false

    // Get the LifecycleOwner to observe activity lifecycle events
    private val lifecycleOwner: LifecycleOwner = (context as? LifecycleOwner)
        ?: (activity as? LifecycleOwner)
        ?: throw IllegalStateException("No LifecycleOwner available")

    private val scanViewNew = ScanViewNew(context, lifecycleOwner, channel).apply {
        setCaptureListener(this@ScanPlatformView)
    }

    private val scanDrawView = ScanDrawView(context, args)

    // Root view that contains both camera feed and overlay
    private val parentLayout = RelativeLayout(context).apply {
        layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        addView(scanViewNew, layoutParams)
        addView(scanDrawView, layoutParams)
    }

    init {
        Log.d(TAG, "🔧 ScanPlatformView initialized")
        channel.setMethodCallHandler(this)
        binding.addRequestPermissionsResultListener { reqCode, _, grantResults ->
            if (reqCode == CAMERA_REQUEST_CODE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                startCameraSafe()
            } else {
                Log.e(TAG, "❌ Camera permission denied via request")
            }
            true
        }

        // Observe Android lifecycle to manage camera lifecycle properly
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                Log.d(TAG, "📴 Lifecycle onPause: stopping camera completely.")
                scanViewNew.stopCameraForLifecycle() 
                scanDrawView.pause()
            }

            override fun onResume(owner: LifecycleOwner) {
                Log.d(TAG, "📷 Lifecycle onResume: ensuring camera is started.")
                scanViewNew.startCamera() 
                scanDrawView.resume()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                Log.d(TAG, "💀 Lifecycle onDestroy: disposing view")
                dispose()
            }
        })
        requestCameraPermissionIfNeeded()
    }

    /**
     * Check camera permission, and request it if necessary.
    */
    private fun requestCameraPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            Log.d(TAG, "✅ Camera permission already granted")
            startCameraSafe()
        }
    }

    /**
     * Start the camera safely only if the activity is not finishing.
    */
    private fun startCameraSafe() {
        if (!activity.isFinishing) {
            scanViewNew.startCamera()
        } else {
            Log.w(TAG, "🚫 Activity is finishing, not starting camera")
        }
    }

    override fun getView(): View = parentLayout

    override fun dispose() {
        Log.d(TAG, "♻️ Disposing camera and overlay")
        scanViewNew.disposeView()
        scanDrawView.pause()
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "resume" -> {
                Log.d(TAG, "🎬 'resume' command received")
                scanViewNew.resumeScanning()
                scanDrawView.resume()
                result.success(null)
            }

            "pause" -> {
                Log.d(TAG, "⏸️ 'pause' command received")
                scanViewNew.pauseScanning()
                scanDrawView.pause()
                result.success(null)
            }

            "toggleTorchMode" -> {
                flashlightOn = !flashlightOn
                scanViewNew.toggleTorch(flashlightOn)
                result.success(flashlightOn)
            }

            "shutdown" -> {
                dispose()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    /**
     * Called when a QR code has been captured successfully.
     */
    override fun onCapture(text: String) {
        Log.i(TAG, "📸 QR Captured: $text")
        channel.invokeMethod("onCaptured", text)
        scanViewNew.pauseScanning()
        scanDrawView.pause()
    }
}