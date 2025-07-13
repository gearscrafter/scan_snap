package com.chavesgu.scan

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Custom view that draws the animated scan area and line for QR scanning.
 */
class ScanDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val args: Map<String, Any>? = null
) : View(context, attrs) {


    constructor(context: Context, args: Map<String, Any>?) : this(context, null, args)

    private var scale = 0.7
    private var scanLineColor = Color.GREEN
    private var transparentScanLine = false
    private var dpi = 1f

    private var vw = 0.0
    private var vh = 0.0
    private var areaX = 0.0
    private var areaY = 0.0
    private var areaWidth = 0.0

    private var running = true
    private var positionAnimator: ValueAnimator? = null
    private var scanLinePositionValue = 0f

    init {
        initParams(context)
    }

    /**
     * Initializes drawing parameters from arguments and display metrics.
     */
    private fun initParams(context: Context) {
        args?.let {
            scale = (it["scale"] as? Double) ?: 0.7
            val r = (it["r"] as? Int) ?: 0
            val g = (it["g"] as? Int) ?: 255
            val b = (it["b"] as? Int) ?: 0
            val alpha = (it["a"] as? Double) ?: 1.0
            val a = max(0, min(255, floor(alpha * 256.0).toInt()))
            transparentScanLine = a == 0
            scanLineColor = Color.argb(a, r, g, b)
        }

        val dm: DisplayMetrics = context.resources.displayMetrics
        dpi = dm.density
        setWillNotDraw(false)
    }

    /**
     * Calculates scanning area and starts the scan line animation.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        vw = w.toDouble()
        vh = h.toDouble()
        areaWidth = min(vw, vh) * scale
        areaX = (vw - areaWidth) / 2.0
        areaY = (vh - areaWidth) / 2.0

        val scanLineWidth = (areaWidth * 0.8).toFloat()
        val duration = (areaWidth / 175.0 / dpi * 1.5 * 1000).toLong()
        positionAnimator = ValueAnimator.ofFloat(0f, scanLineWidth).apply {
            this.duration = duration
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                scanLinePositionValue = it.animatedValue as Float
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawOverlay(canvas)
    }

    /**
     * Draws the scan area overlay:
     * - Border corners
     * - Dark mask outside scan area
     * - Animated scan line
     */
    private fun drawOverlay(canvas: Canvas) {
        val x = areaX.toFloat()
        val y = areaY.toFloat()
        val width = areaWidth.toFloat()
        val shortLen = width * 0.1f
        val scanLen = width * 0.8f
        val scanX = ((vw - scanLen) / 2f).toFloat()
        val scanY = ((vh - scanLen) / 2f).toFloat()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2 * dpi
            color = scanLineColor
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        // Draw corners
        canvas.drawLine(x, y, x + shortLen, y, paint)
        canvas.drawLine(x, y, x, y + shortLen, paint)

        canvas.drawLine(x + width, y, x + width - shortLen, y, paint)
        canvas.drawLine(x + width, y, x + width, y + shortLen, paint)

        canvas.drawLine(x + width, y + width, x + width - shortLen, y + width, paint)
        canvas.drawLine(x + width, y + width, x + width, y + width - shortLen, paint)

        canvas.drawLine(x, y + width, x + shortLen, y + width, paint)
        canvas.drawLine(x, y + width, x, y + width - shortLen, paint)

        // Draw mask around the scan area
        canvas.save()
        val clip = Path().apply {
            addRect(x - 2, y - 2, x + width + 2, y + width + 2, Path.Direction.CCW)
        }
        canvas.clipPath(clip)
        val mask = Paint().apply {
            style = Paint.Style.FILL
            color = Color.argb(128, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, vw.toFloat(), vh.toFloat(), mask)
        canvas.restore()

        // Draw animated scan line
        if (running && !transparentScanLine) {
            val fraction = scanLinePositionValue / scanLen
            paint.alpha = if (fraction < 2.0 / 3.0) {
                255
            } else {
                val a = 1 - (fraction - 2.0f / 3.0f) * 3
                max(0, floor(a * 256).toInt())
            }
            canvas.drawLine(scanX, scanY + scanLinePositionValue,
                scanX + scanLen, scanY + scanLinePositionValue, paint)
        }
    }

    fun resume() {
        running = true
        positionAnimator?.resume()
        postInvalidateOnAnimation()
    }

    fun pause() {
        running = false
        positionAnimator?.pause()
        postInvalidateOnAnimation()
    }
}
