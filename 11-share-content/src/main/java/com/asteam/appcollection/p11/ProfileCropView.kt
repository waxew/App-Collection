package com.asteam.appcollection.p11

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Reusable in-app profile photo cropper.
 *
 * The view deliberately avoids third-party crop libraries so the common AS Team drawer can be
 * copied into every Android app without adding another dependency. The user can:
 * - drag the image with one finger;
 * - pinch with two fingers to zoom between the minimum cover scale and 5x that scale;
 * - preview the final circular profile area while the stored bitmap remains a square crop.
 */
class ProfileCropView(context: Context) : View(context) {

    /** Source bitmap selected from Android's document picker. */
    private var sourceBitmap: Bitmap? = null

    /** Square crop rectangle displayed in the center of this custom view. */
    private val cropRect = RectF()

    /** Paint used to draw the selected photograph. */
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    /** Semi-transparent paint used to darken everything outside the circular profile preview. */
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(145, 0, 0, 0)
        style = Paint.Style.FILL
    }

    /** Border paint makes the exact crop area visually obvious to the user. */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(2).toFloat()
    }

    /** Current absolute bitmap scale relative to its original pixel dimensions. */
    private var currentScale = 1f

    /** Smallest scale that still completely covers the crop square. */
    private var minimumScale = 1f

    /** Bitmap top-left position inside this view after scaling and dragging. */
    private var offsetX = 0f
    private var offsetY = 0f

    /** Last single-finger position used to calculate drag distance. */
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    /** Handles two-finger pinch gestures while keeping the pinch focus visually anchored. */
    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val previousScale = currentScale
                val requestedScale = currentScale * detector.scaleFactor

                // Limit zoom so the image can never become smaller than the crop area or absurdly large.
                currentScale = requestedScale.coerceIn(minimumScale, minimumScale * 5f)

                // Keep the pixels under the user's fingers in approximately the same screen position.
                val ratio = currentScale / previousScale
                offsetX = detector.focusX - (detector.focusX - offsetX) * ratio
                offsetY = detector.focusY - (detector.focusY - offsetY) * ratio

                // Prevent blank space from appearing inside the final crop square.
                clampOffsets()
                invalidate()
                return true
            }
        }
    )

    /** Receives the bitmap and prepares its initial cover/center transform. */
    fun setSourceBitmap(bitmap: Bitmap) {
        sourceBitmap = bitmap
        if (width > 0 && height > 0) {
            updateCropRect()
            resetTransform()
        }
        invalidate()
    }

    /** Recalculates the crop geometry whenever Android gives this view a new size. */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateCropRect()
        resetTransform()
    }

    /** Draws the transformed image, outside mask and circular crop guide. */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(28, 31, 36))

        val bitmap = sourceBitmap ?: return
        val scaledWidth = bitmap.width * currentScale
        val scaledHeight = bitmap.height * currentScale

        // Draw the complete transformed image underneath the crop overlay.
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight),
            imagePaint
        )

        // EVEN_ODD creates a transparent circular hole inside an otherwise dark overlay.
        val maskPath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
            addCircle(cropRect.centerX(), cropRect.centerY(), cropRect.width() / 2f, Path.Direction.CW)
        }
        canvas.drawPath(maskPath, overlayPaint)

        // The white circle indicates exactly what will be visible after the profile image is clipped.
        canvas.drawCircle(
            cropRect.centerX(),
            cropRect.centerY(),
            cropRect.width() / 2f,
            borderPaint
        )
    }

    /** Handles one-finger drag and delegates two-finger zoom to ScaleGestureDetector. */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                // A single pointer means the user is panning, not pinching.
                if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                    val deltaX = event.x - lastTouchX
                    val deltaY = event.y - lastTouchY
                    offsetX += deltaX
                    offsetY += deltaY
                    clampOffsets()
                    invalidate()
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }

            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                // Reset the reference point so a finished pinch does not cause a sudden drag jump.
                lastTouchX = event.x
                lastTouchY = event.y
            }
        }
        return true
    }

    /**
     * Produces the exact square represented by the crop guide and optionally scales it for storage.
     * The profile ImageView later clips this square into a circle, which avoids losing corner pixels.
     */
    fun createCroppedBitmap(outputSize: Int = 720): Bitmap? {
        val bitmap = sourceBitmap ?: return null
        if (cropRect.width() <= 0f || currentScale <= 0f) return null

        // Convert crop coordinates from screen pixels back into original bitmap pixels.
        val sourceLeft = ((cropRect.left - offsetX) / currentScale).coerceIn(0f, bitmap.width.toFloat())
        val sourceTop = ((cropRect.top - offsetY) / currentScale).coerceIn(0f, bitmap.height.toFloat())
        val sourceRight = ((cropRect.right - offsetX) / currentScale).coerceIn(0f, bitmap.width.toFloat())
        val sourceBottom = ((cropRect.bottom - offsetY) / currentScale).coerceIn(0f, bitmap.height.toFloat())

        val x = floor(sourceLeft).toInt().coerceIn(0, bitmap.width - 1)
        val y = floor(sourceTop).toInt().coerceIn(0, bitmap.height - 1)
        val cropWidth = ceil(sourceRight - sourceLeft).toInt().coerceAtLeast(1).coerceAtMost(bitmap.width - x)
        val cropHeight = ceil(sourceBottom - sourceTop).toInt().coerceAtLeast(1).coerceAtMost(bitmap.height - y)

        val rawCrop = Bitmap.createBitmap(bitmap, x, y, cropWidth, cropHeight)
        if (rawCrop.width == outputSize && rawCrop.height == outputSize) return rawCrop

        val scaledCrop = Bitmap.createScaledBitmap(rawCrop, outputSize, outputSize, true)
        if (scaledCrop !== rawCrop) rawCrop.recycle()
        return scaledCrop
    }

    /** Builds a centered square with small breathing room around its circular preview. */
    private fun updateCropRect() {
        if (width <= 0 || height <= 0) return
        val margin = dp(16).toFloat()
        val availableSide = min(width.toFloat() - margin * 2f, height.toFloat() - margin * 2f)
            .coerceAtLeast(1f)
        val left = (width - availableSide) / 2f
        val top = (height - availableSide) / 2f
        cropRect.set(left, top, left + availableSide, top + availableSide)
    }

    /** Fits the image so the crop square is fully covered, then centers it. */
    private fun resetTransform() {
        val bitmap = sourceBitmap ?: return
        if (cropRect.width() <= 0f || bitmap.width <= 0 || bitmap.height <= 0) return

        minimumScale = max(
            cropRect.width() / bitmap.width.toFloat(),
            cropRect.height() / bitmap.height.toFloat()
        )
        currentScale = minimumScale

        val scaledWidth = bitmap.width * currentScale
        val scaledHeight = bitmap.height * currentScale
        offsetX = cropRect.centerX() - scaledWidth / 2f
        offsetY = cropRect.centerY() - scaledHeight / 2f
        clampOffsets()
        invalidate()
    }

    /** Restricts image movement so every point of the crop square always contains photograph pixels. */
    private fun clampOffsets() {
        val bitmap = sourceBitmap ?: return
        val scaledWidth = bitmap.width * currentScale
        val scaledHeight = bitmap.height * currentScale

        val minimumX = cropRect.right - scaledWidth
        val maximumX = cropRect.left
        val minimumY = cropRect.bottom - scaledHeight
        val maximumY = cropRect.top

        offsetX = offsetX.coerceIn(minimumX, maximumX)
        offsetY = offsetY.coerceIn(minimumY, maximumY)
    }

    /** Converts density-independent pixels into physical pixels for consistent visual sizing. */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
