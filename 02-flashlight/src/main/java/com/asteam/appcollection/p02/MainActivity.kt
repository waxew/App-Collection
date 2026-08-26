package com.asteam.appcollection.p02


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
 * Rebuilt Kotlin application #02.
 * Original Dropbox source: جعبه ابزار - چراغ قوه.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Flashlight"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "چراغ قوه با CameraManager"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "جعبه ابزار - چراغ قوه.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val state = label("وضعیت چراغ قوه: خاموش")
        container.addView(state)
        container.addView(button("روشن / خاموش") {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 502)
            } else {
                toggleTorch(state)
            }
        })
    }

    /** Remembers torch state so the button can toggle reliably. */
    private var torchEnabled = false

    /** Uses CameraManager instead of the removed android.hardware.Camera API. */
    private fun toggleTorch(output: TextView) {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
        if (cameraId == null) {
            output.text = "این دستگاه فلش قابل کنترل ندارد."
            return
        }
        torchEnabled = !torchEnabled
        manager.setTorchMode(cameraId, torchEnabled)
        output.text = if (torchEnabled) "وضعیت چراغ قوه: روشن" else "وضعیت چراغ قوه: خاموش"
    }
}
