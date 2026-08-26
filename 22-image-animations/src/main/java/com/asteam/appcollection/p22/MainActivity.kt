package com.asteam.appcollection.p22


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
 * Rebuilt Kotlin application #22.
 * Original Dropbox source: جعبه ابزار - انیمیشن های روی عکس.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Image Animations"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "انیمیشن Alpha/Rotate/Scale/Translate"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - انیمیشن های روی عکس.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val target = label("عنصر متحرک", 24f).apply {
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(230, 235, 250))
        }
        container.addView(target, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(90)))
        container.addView(button("حرکت و چرخش") {
            target.animate().translationXBy(dp(80).toFloat()).rotationBy(360f).alpha(0.35f).setDuration(900).withEndAction {
                target.animate().translationX(0f).rotation(0f).alpha(1f).setDuration(500).start()
            }.start()
        })
    }


}
