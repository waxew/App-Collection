package com.asteam.appcollection.p01


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
 * Rebuilt Kotlin application #01.
 * Original Dropbox source: gps - Java.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Gps Live Location"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "GPS و نمایش مختصات زنده"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "gps - Java.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val output = label("برای دریافت مختصات، مجوز موقعیت را تأیید کنید.")
        container.addView(output)
        container.addView(button("دریافت موقعیت زنده") {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 501)
            } else {
                startLocationUpdates(output)
            }
        })
        container.addView(button("باز کردن تنظیمات موقعیت") {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        })
    }

    /** Starts GPS updates after runtime permission has been granted. */
        private fun startLocationUpdates(output: TextView) {
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                output.text = "GPS خاموش است؛ ابتدا آن را از تنظیمات روشن کنید."
                return
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2_000L, 1f, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    output.text = "Latitude: ${location.latitude}
    Longitude: ${location.longitude}
    Accuracy: ${location.accuracy} m"
                }
            })
        }

        /** Continues the requested action immediately after permission approval. */
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 501 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                showInfo("GPS", "مجوز موقعیت صادر شد. دوباره دکمه دریافت موقعیت را بزنید.")
            }
        }
}
