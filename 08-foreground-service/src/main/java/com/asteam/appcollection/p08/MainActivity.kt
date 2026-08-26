package com.asteam.appcollection.p08


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
 * Rebuilt Kotlin application #08.
 * Original Dropbox source: backgrounds service.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Foreground Service"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "سرویس پس‌زمینه مدرن"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "backgrounds service.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val status = label("سرویس متوقف است.")
        container.addView(status)
        container.addView(button("شروع سرویس Foreground") {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 503)
            }
            startForegroundService(Intent(this, DemoForegroundService::class.java))
            status.text = "سرویس اجرا شد."
        })
        container.addView(button("توقف سرویس") {
            stopService(Intent(this, DemoForegroundService::class.java))
            status.text = "سرویس متوقف شد."
        })
    }

    }

    /** Foreground service replacing the unrestricted background service pattern used by old Android. */
    class DemoForegroundService : Service() {
        override fun onCreate() {
            super.onCreate()
            val channelId = "demo_service"
            if (Build.VERSION.SDK_INT >= 26) {
                val channel = NotificationChannel(channelId, "Demo service", NotificationManager.IMPORTANCE_LOW)
                getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
            val notification = if (Build.VERSION.SDK_INT >= 26) {
                Notification.Builder(this, channelId).setContentTitle("سرویس نمونه فعال است").setSmallIcon(android.R.drawable.ic_media_play).build()
            } else {
                @Suppress("DEPRECATION") Notification.Builder(this).setContentTitle("سرویس نمونه فعال است").setSmallIcon(android.R.drawable.ic_media_play).build()
            }
            startForeground(8, notification)
        }
        override fun onBind(intent: Intent?) = null
}
