package com.asteam.appcollection.p51


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
 * Rebuilt Kotlin application #51.
 * Original Dropbox source: notfigation - manifest.txt
 * Classification: تکراری / منسوخ بازسازی‌شده
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Notification Permission"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "مجوز اعلان Android 13+"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "notfigation - manifest.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val titleInput = input("عنوان اعلان").apply { setText("App Collection") }
        val textInput = input("متن اعلان").apply { setText("اعلان نمونه با API جدید Android") }
        container.addView(titleInput)
        container.addView(textInput)
        container.addView(button("ارسال اعلان") {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 504)
            } else {
                sendDemoNotification(titleInput.text.toString(), textInput.text.toString())
            }
        })
    }

    /** Creates a NotificationChannel on Android 8+ and posts an OS-compliant notification. */
    private fun sendDemoNotification(title: String, text: String) {
        val channelId = "general"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "اعلان‌های عمومی", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else @Suppress("DEPRECATION") Notification.Builder(this)
        builder.setContentTitle(title).setContentText(text).setSmallIcon(android.R.drawable.ic_dialog_info).setAutoCancel(true)
        manager.notify(100 + 51, builder.build())
    }
}
