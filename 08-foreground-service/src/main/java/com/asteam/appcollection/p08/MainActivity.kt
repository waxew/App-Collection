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
 * The unrestricted 2017 background Service pattern is replaced with a foreground service.
 * Android 8+ starts it with startForegroundService(), while older supported Android versions
 * use startService() so the same APK remains compatible down to this project's minSdk.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    override val demoTitle: String = "Foreground Service"
    override val demoDescription: String = "سرویس پس‌زمینه مدرن"
    override val sourceReference: String = "backgrounds service.txt"

    /** Adds controls for starting and stopping the modern foreground service. */
    override fun renderDemo(container: LinearLayout) {
        val status = label("سرویس متوقف است.")
        container.addView(status)

        container.addView(button("شروع سرویس Foreground") {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    503
                )
            }

            val serviceIntent = Intent(this, DemoForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            status.text = "سرویس اجرا شد."
        })

        container.addView(button("توقف سرویس") {
            stopService(Intent(this, DemoForegroundService::class.java))
            status.text = "سرویس متوقف شد."
        })
    }
}

/**
 * Foreground service replacing the old unrestricted background-service example.
 * The service is private to this application and immediately publishes its foreground notice.
 */
class DemoForegroundService : Service() {

    private companion object {
        const val CHANNEL_ID = "demo_service"
        const val NOTIFICATION_ID = 8
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannelIfNeeded()
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    /** This sample has no bound API, so binding is deliberately unsupported. */
    override fun onBind(intent: Intent?): IBinder? = null

    /** Avoids recreating background work automatically after the user/app stops the service. */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    /** Notification channels are mandatory from Android 8 onward. */
    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Demo service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /** Builds the persistent foreground notification using the API appropriate for the OS. */
    @Suppress("DEPRECATION")
    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("سرویس نمونه فعال است")
            .setContentText("نمونه‌ی سرویس پس‌زمینه مدرن در حال اجراست")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }
}
