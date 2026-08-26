package com.asteam.appcollection.p53


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
 * Rebuilt Kotlin application #53.
 * Original Dropbox source: dialog - custom - material - gradle.txt
 * Classification: تکراری / منسوخ بازسازی‌شده
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Dialog Dependency Migration"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "جایگزینی dependency قدیمی NiftyDialog"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "dialog - custom - material - gradle.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        container.addView(label("کد ۲۰۱۷ به کتابخانه‌های com.nineoldandroids و niftydialogeffects وابسته بود."))
        container.addView(label("بازسازی جدید dependency قدیمی را حذف کرده و از Dialog + ViewPropertyAnimator داخلی Android استفاده می‌کند."))
        container.addView(button("دیدن جایگزین") {
            val view = label("بدون dependency خارجی", 20f)
            AlertDialog.Builder(this).setTitle("Migration").setView(view).setPositiveButton("باشه", null).show()
        })
    }


}
