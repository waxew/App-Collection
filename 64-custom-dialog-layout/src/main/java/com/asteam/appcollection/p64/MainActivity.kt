package com.asteam.appcollection.p64


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
 * Rebuilt Kotlin application #64.
 * Original Dropbox source: dialog - custom1 - xnl.txt
 * Classification: تکراری / منسوخ بازسازی‌شده
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Custom Dialog Layout"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "XML دیالوگ با نام فایل اشتباه، بازسازی UI"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "dialog - custom1 - xnl.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        container.addView(button("نمایش دیالوگ") {
            AlertDialog.Builder(this)
                .setTitle("نمونه دیالوگ")
                .setSingleChoiceItems(arrayOf("گزینه ۱", "گزینه ۲", "گزینه ۳"), -1, null)
                .setPositiveButton("تأیید", null)
                .setNegativeButton("انصراف", null)
                .show()
        })
    }


}
