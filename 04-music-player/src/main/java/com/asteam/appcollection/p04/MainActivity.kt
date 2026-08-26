package com.asteam.appcollection.p04


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
 * Rebuilt Kotlin application #04.
 * Original Dropbox source: music player - java.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Music Player"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "پخش صوت و کنترل Play/Pause"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "music player - java.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val status = label("پخش متوقف است.")
        container.addView(status)
        container.addView(button("پخش صدای نمونه") {
            startSampleAudio()
            status.text = "در حال پخش صدای نمونه سیستم"
        })
        container.addView(button("توقف") {
            stopSampleAudio()
            status.text = "پخش متوقف شد."
        })
    }

    /** MediaPlayer is created lazily from the device notification tone, so no binary audio asset is required. */
    private var mediaPlayer: MediaPlayer? = null
    private fun startSampleAudio() {
        if (mediaPlayer == null) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(this, uri)
        }
        mediaPlayer?.start()
    }
    private fun stopSampleAudio() {
        mediaPlayer?.pause()
        mediaPlayer?.seekTo(0)
    }
    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
