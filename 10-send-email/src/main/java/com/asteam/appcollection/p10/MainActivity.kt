package com.asteam.appcollection.p10


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
 * Rebuilt Kotlin application #10.
 * Original Dropbox source: send mail - java.txt
 * Classification: بازسازی ارزشمند
 *
 * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle
 * handling are replaced with Android-compatible implementations for the current toolchain.
 */
@Suppress("DEPRECATION")
class MainActivity : BaseDemoActivity() {

    /** Visible title used by the shared app shell. */
    override val demoTitle: String = "Send Email"

    /** Short user-facing explanation of the rebuilt sample. */
    override val demoDescription: String = "ارسال ایمیل با Intent"

    /** Keeps traceability to the original 2017 text file. */
    override val sourceReference: String = "send mail - java.txt"

    /** Adds the interactive controls for this specific sample. */
    override fun renderDemo(container: LinearLayout) {
        val to = input("example@example.com")
        val subject = input("موضوع")
        val message = input("متن پیام")
        container.addView(to)
        container.addView(subject)
        container.addView(message)
        container.addView(button("ارسال با برنامه ایمیل") {
            val recipient = to.text.toString().trim()
            if (!recipient.contains("@")) {
                showInfo("ایمیل", "آدرس ایمیل معتبر وارد کنید.")
            } else {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + Uri.encode(recipient))).apply {
                    putExtra(Intent.EXTRA_SUBJECT, subject.text.toString())
                    putExtra(Intent.EXTRA_TEXT, message.text.toString())
                }
                if (intent.resolveActivity(packageManager) != null) startActivity(intent) else showInfo("ایمیل", "کلاینت ایمیل پیدا نشد.")
            }
        })
    }


}
