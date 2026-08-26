package com.asteam.appcollection.p07

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
 * Rebuilt Kotlin application #07.
 * Original Dropbox source: جعبه ابزار - نمایش شارژ باتری.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Battery Monitor"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "نمایش درصد و وضعیت باتری"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - نمایش شارژ باتری.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val output = label()

        /** Reads the latest sticky battery broadcast and refreshes the visible status. */
        fun refresh() {
            val battery = registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val percent = if (level >= 0 && scale > 0) {
                level * 100f / scale
            } else {
                0f
            }
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            output.text = "شارژ: %.1f%%\nدر حال شارژ: %s".format(
                Locale.getDefault(),
                percent,
                if (charging) "بله" else "خیر"
            )
        }

        container.addView(output)
        container.addView(button("بروزرسانی وضعیت") { refresh() })
        refresh()
    }
}
