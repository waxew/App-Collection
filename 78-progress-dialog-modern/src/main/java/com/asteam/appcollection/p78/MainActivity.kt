package com.asteam.appcollection.p78


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
 * Rebuilt Kotlin application #78.
 * Original Dropbox source: جعبه ابزار - لکدینگ پروگرس تایمر.txt
 * Classification: تکراری / منسوخ بازسازی‌شده
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Progress Dialog Modern"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "ProgressDialog منسوخ، بازسازی با ProgressBar داخل Dialog"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - لکدینگ پروگرس تایمر.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        container.addView(button("نمایش پیشرفت مدرن") {
            val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(24), dp(24), dp(24)) }
            val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
            val text = label("0%")
            wrapper.addView(progress); wrapper.addView(text)
            val dialog = AlertDialog.Builder(this).setTitle("در حال انجام عملیات").setView(wrapper).setNegativeButton("لغو", null).create()
            dialog.show()
            val handler = Handler(Looper.getMainLooper())
            var value = 0
            val task = object : Runnable {
                override fun run() {
                    value += 10; progress.progress = value; text.text = "$value%"
                    if (value < 100 && dialog.isShowing) handler.postDelayed(this, 250L) else if (dialog.isShowing) dialog.dismiss()
                }
            }
            handler.postDelayed(task, 250L)
        })
    }


}
