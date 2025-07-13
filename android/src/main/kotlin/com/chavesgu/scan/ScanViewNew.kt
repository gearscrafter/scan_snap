package com.chavesgu.scan // Asegúrate de que este sea el paquete correcto para tu proyecto

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
     * Initializes and starts the camera using CameraX if it hasn't been started already.
     */
    fun startCamera() {
        if (cameraStarted) {
            Log.w(TAG, "⚠️ startCamera() was already called. Ignoring...")
            return
        }
        cameraStarted = true
        Log.d(TAG, "📷 Starting camera...")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCamera()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to get CameraProvider", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Binds the camera to the lifecycle and sets up preview and analysis use cases.
     */
    private fun bindCamera() {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "CameraProvider is not available.")
            return
        }

        try {
            provider.unbindAll()
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Error during unbindAll (can be normal): ${e.message}")
        }

        val preview = Preview.Builder()
            .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Setup the image analyzer for QR scanning
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        Log.d(TAG, "📦 ImageAnalysis configurado, esperando 500ms...")

       
        val executor = cameraExecutor ?: return

        imageAnalysis?.setAnalyzer(executor) { imageProxy ->
            try {
                if (scannedOnce) {
                    return@setAnalyzer
                }

                val result = QRCodeDecoder.decodeQRCode(imageProxy)

                if (result != null) {
                    scannedOnce = true
                    Log.i(TAG, "✅ QR code detected: $result")

                    // Notify Flutter and vibrate on the main thread
                    Handler(Looper.getMainLooper()).post {
                        captureListener?.onCapture(result)
                        vibrate()
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error analyzing image", e)
            } finally {
                // Always close imageProxy to avoid blocking the stream
                imageProxy.close()
            }
        }
       

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
            Log.e(TAG, "❌ Failed to bind camera", e)
        }
    }

    /**
     * Sets the listener that will be called when a QR code is captured.
     */
    fun setCaptureListener(listener: CaptureListener?) {
        captureListener = listener
    }


    /**
     * Toggles the device flashlight (torch) on or off.
     */
    fun toggleTorch(on: Boolean) {
        try {
            cameraControl?.enableTorch(on)
            Log.d(TAG, "💡 Torch ${if (on) "enabled" else "disabled"}")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to toggle torch", e)
        }
    }

    /**
     * Resets the scanned state, allowing another QR code to be scanned.
     */
    fun resumeCamera() {
        scannedOnce = false
        Log.d(TAG, "🔁 Camera resumed (ready for another scan).")
    }

    /**
     * Clears the image analyzer to stop processing frames.
     */
    fun pauseCamera() {
        Log.d(TAG, "⏸️ Camera paused")
        imageAnalysis?.clearAnalyzer()
    }

    /**
     * Releases camera resources and shuts down executors.
     */
    fun disposeView() {
        Log.d(TAG, "🧹Releasing camera resources...")
        try {
            cameraProvider?.unbindAll()
            cameraExecutor?.shutdown()
            
            // Reseteamos las variables
            cameraExecutor = null
            cameraProvider = null
            cameraStarted = false
            Log.d(TAG, "Resources successfully released.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release camera resources.", e)
        } 
    }

    /**
     * Vibrates the device to indicate a QR code has been captured.
     */
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