package com.asteam.appcollection.p12


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
 * Rebuilt Kotlin application #12.
 * Original Dropbox source: جعبه ابزار - انتخاب صدای زنگ.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Ringtone Picker"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "انتخاب صدای زنگ سیستم"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - انتخاب صدای زنگ.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        container.addView(label("Ringtone Picker سیستم بدون دسترسی مستقیم به فایل‌های صوتی باز می‌شود."))
        container.addView(button("انتخاب صدای زنگ") {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE or RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            }
            startActivity(intent)
        })
    }


}
