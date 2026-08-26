package com.asteam.appcollection.p34


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
 * Rebuilt Kotlin application #34.
 * Original Dropbox source: جعبه ابزار - تغیر رنگ متن با فشار طولانی.txt
 * Classification: کد آموزشی مفید
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Long Press Context"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "Long Press و Context Action"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - تغیر رنگ متن با فشار طولانی.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val text = label("روی این متن لمس طولانی انجام دهید.", 20f).apply { setBackgroundColor(Color.YELLOW) }
        text.setOnLongClickListener {
            text.setBackgroundColor(Color.rgb(170, 220, 255))
            showInfo("Long Press", "رویداد لمس طولانی دریافت شد.")
            true
        }
        container.addView(text)
    }


}
