package com.asteam.appcollection.p72


import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.Html
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import com.asteam.appcollection.sharedui.BaseDemoActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Rebuilt Kotlin application #72.
 * Original Dropbox source: جعبه ابزار - ساعت دیجیتال و آنالوگ.txt
 * Classification: تکراری / منسوخ بازسازی‌شده
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Clock Toolbox"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "نسخه فارسی همان ساعت، بازسازی مستقل"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - ساعت دیجیتال و آنالوگ.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val digital = TextClock(this).apply { format24Hour = "HH:mm:ss"; textSize = 32f; gravity = Gravity.CENTER }
        val analog = AnalogClockView(this)
        container.addView(digital)
        container.addView(analog, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(320)))
    }

    /** Custom analog clock replaces the deprecated platform AnalogClock widget. */
    private class AnalogClockView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.DKGRAY; strokeWidth = 6f; style = Paint.Style.STROKE }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f; val cy = height / 2f; val r = minOf(width, height) * 0.4f
            canvas.drawCircle(cx, cy, r, paint)
            val cal = java.util.Calendar.getInstance()
            val minute = cal.get(java.util.Calendar.MINUTE)
            val hour = cal.get(java.util.Calendar.HOUR)
            fun hand(angleDeg: Float, length: Float, width: Float) {
                val rad = Math.toRadians((angleDeg - 90).toDouble())
                paint.strokeWidth = width
                canvas.drawLine(cx, cy, cx + kotlin.math.cos(rad).toFloat() * length, cy + kotlin.math.sin(rad).toFloat() * length, paint)
            }
            hand(minute * 6f, r * 0.82f, 5f)
            hand((hour + minute / 60f) * 30f, r * 0.58f, 9f)
            postInvalidateDelayed(1_000L)
        }
    }
}
