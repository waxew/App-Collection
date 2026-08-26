package com.asteam.appcollection.p46


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
 * Rebuilt Kotlin application #46.
 * Original Dropbox source: webview - manifest.txt
 * Classification: کد آموزشی مفید
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Webview Manifest Modern"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "آموزش مجوز و Manifest وب"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "webview - manifest.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val address = input("https://developer.android.com")
        address.setText("https://developer.android.com")
        val web = WebView(this).apply {
            settings.javaScriptEnabled = false
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
        container.addView(address)
        container.addView(button("باز کردن آدرس") {
            val url = address.text.toString().trim()
            if (url.startsWith("https://")) web.loadUrl(url) else showInfo("URL", "برای این نمونه فقط HTTPS پذیرفته می‌شود.")
        })
        container.addView(web, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(420)))
    }


}
