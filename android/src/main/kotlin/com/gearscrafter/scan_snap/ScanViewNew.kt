package com.gearscrafter.scan_snap

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Surface
import android.widget.FrameLayout
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A custom native view that handles CameraX preview and QR code detection.
 * This class integrates directly into the Flutter view hierarchy via PlatformView.
 */
class ScanViewNew(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val channel: MethodChannel?
) : FrameLayout(context) {

    interface CaptureListener {
        fun onCapture(text: String)
    }

    private val previewView = PreviewView(context)
    private var cameraExecutor: ExecutorService? = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraControl: CameraControl? = null
    private var captureListener: CaptureListener? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var scannedOnce = false
    private var cameraStarted = false

    companion object {
        private const val TAG = "ScanViewNew"
    }

    init {
        previewView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(previewView)
    }

    /**
     * Starts the camera using CameraX, if not already started.
     */
    fun startCamera() {
        if (cameraStarted) {
            Log.w(TAG, "⚠️ startCamera() has already been called. Ignoring...")
            return
        }
        cameraStarted = true
        Log.d(TAG, "📷 Starting camera...")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to obtain CameraProvider", e)
                channel?.invokeMethod("onCameraError", null)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Temporarily stops image analysis. 
     * Used when the "pause" button is pressed on the Flutter UI.
     */
    fun pauseScanning() {
        Log.d(TAG, "⏸️ Pausing image analysis only.")
        imageAnalysis?.clearAnalyzer()
    }

    /**
     * Resumes image analysis and resets internal state to allow a new scan.
     * Used when the "resume" button is pressed on the Flutter UI.
     */
    fun resumeScanning() {
        Log.d(TAG, "🎬 Resuming image analysis.")
        scannedOnce = false
        val executor = cameraExecutor ?: return
        imageAnalysis?.setAnalyzer(executor, getAnalyzer())
    }

    /**
     * Stops and unbinds all camera use cases.
     * Typically called from Flutter's lifecycle onPause event.
     */
    fun stopCameraForLifecycle() {
        Log.d(TAG, "⛔ Stopping and unbinding camera (lifecycle onPause).")
        cameraProvider?.unbindAll()
        cameraStarted = false
    }

    /**
     * Releases all camera-related resources.
     * Called when the view is about to be destroyed.
     */
    fun disposeView() {
        Log.d(TAG, "🧹 Releasing all camera resources.")
        try {
            stopCameraForLifecycle()
            cameraExecutor?.shutdown()
            cameraExecutor = null
            cameraProvider = null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error while releasing camera resources.", e)
        }
    }

    /**
     * Sets the callback to be triggered when a QR code is captured.
     */
    fun setCaptureListener(listener: CaptureListener?) {
        this.captureListener = listener
    }

    /**
     * Toggles the flashlight (torch) on or off.
     */
    fun toggleTorch(on: Boolean) {
        cameraControl?.enableTorch(on)
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        try {
            provider.unbindAll()
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error during unbindAll (can be safely ignored): ${e.message}")
        }

        val preview = Preview.Builder()
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val executor = cameraExecutor ?: return
        imageAnalysis?.setAnalyzer(executor, getAnalyzer())

        try {
            val camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
            cameraControl = camera.cameraControl
            Log.d(TAG, "📷 Camera successfully bound.")
            channel?.invokeMethod("onCameraReady", null)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to bind camera use cases.", e)
            channel?.invokeMethod("onCameraError", null)
        }
    }

    private fun getAnalyzer(): ImageAnalysis.Analyzer {
        return ImageAnalysis.Analyzer { imageProxy ->
            try {
                if (!scannedOnce) {
                    QRCodeDecoder.decodeQRCode(imageProxy, context)?.let { result ->
                        scannedOnce = true
                        Log.i(TAG, "✅ QR code detected: $result")
                        Handler(Looper.getMainLooper()).post {
                            captureListener?.onCapture(result)
                            vibrate()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error analyzing image", e)
            } finally {
                imageProxy.close()
            }
        }
    }

    private fun vibrate() {
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