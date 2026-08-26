package com.asteam.appcollection.p18


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
 * Rebuilt Kotlin application #18.
 * Original Dropbox source: project gallery view.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Image Gallery"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "گالری افقی تصاویر جایگزین Gallery منسوخ"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "project gallery view.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val scroller = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(Color.rgb(60, 90, 180), Color.rgb(30, 150, 110), Color.rgb(210, 100, 70), Color.rgb(120, 80, 170)).forEachIndexed { index, color ->
            val tile = TextView(this).apply {
                text = "تصویر ${index + 1}"
                gravity = Gravity.CENTER
                textSize = 18f
                setTextColor(Color.WHITE)
                setBackgroundColor(color)
                setOnClickListener { showInfo("گالری", "آیتم ${index + 1} انتخاب شد.") }
            }
            row.addView(tile, LinearLayout.LayoutParams(dp(180), dp(180)).apply { setMargins(dp(6), dp(6), dp(6), dp(6)) })
        }
        scroller.addView(row)
        container.addView(scroller)
    }


}
