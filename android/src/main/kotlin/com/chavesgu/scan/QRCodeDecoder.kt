package com.chavesgu.scan 

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.*

/**
 * Optimized QR decoder that works directly with CameraX's ImageProxy
 * and also supports decoding from image file paths.
 */
object QRCodeDecoder {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(
            BarcodeFormat.QR_CODE,
            BarcodeFormat.CODE_128,
            BarcodeFormat.EAN_13,
            BarcodeFormat.DATA_MATRIX
        )
        setHints(hints)
    }

    /**
     * Decodes a QR code directly from an ImageProxy (from CameraX).
     */
    @SuppressLint("UnsafeOptInUsageError")
    fun decodeQRCode(imageProxy: ImageProxy): String? {
        val image = imageProxy.image ?: return null
        val planes = image.planes

        // Extract the luminance (Y) data
        val yBuffer = planes[0].buffer.apply { rewind() }
        val yByteArray = ByteArray(yBuffer.remaining())
        yBuffer.get(yByteArray)

        val source = PlanarYUVLuminanceSource(
            yByteArray,
            planes[0].rowStride,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        return try {
            reader.decodeWithState(binaryBitmap).text
        } catch (e: Exception) {
            null
        } finally {
            reader.reset()
        }
    }


    /**
     * Decodes a QR code from an image file path.
     * This method is typically used by the ScanPlugin.kt class.
     */
    fun syncDecodeQRCode(path: String): String? {
        val bitmap = pathToBitmap(path) ?: return null
        val yuvData = bitmapToYuv(bitmap)
        val source = PlanarYUVLuminanceSource(
            yuvData, bitmap.width, bitmap.height, 0, 0, bitmap.width, bitmap.height, false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

        return try {
            reader.decodeWithState(binaryBitmap).text
        } catch (e: Exception) {
            null
        } finally {
            reader.reset()
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

     /**
     * Converts a Bitmap to a YUV byte array so it can be processed by ZXing.
     */
    private fun bitmapToYuv(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val yuv = ByteArray(width * height * 3 / 2)
        var yIndex = 0
        var uvIndex = width * height
        var r: Int
        var g: Int
        var b: Int
        var y: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                r = pixels[index] and 0xff0000 shr 16
                g = pixels[index] and 0xff00 shr 8
                b = pixels[index] and 0xff
                y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                yuv[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    val v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                    val u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                    yuv[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    yuv[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                }
                index++
            }
        }
        return yuv
    }

    /**
     * Loads a bitmap from a file path, scaling it down if necessary to reduce memory usage.
     */
    private fun pathToBitmap(path: String): Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        opts.inSampleSize = calculateInSampleSize(opts, 800, 800) // Limita el tamaño a 800x800
        opts.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, opts)
    }

    /**
     * Calculates the optimal sample size for downscaling an image.
     */
    private fun calculateInSampleSize(opts: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = opts.outHeight to opts.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfH = height / 2
            val halfW = width / 2
            while (halfH / inSampleSize >= reqHeight && halfW / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}