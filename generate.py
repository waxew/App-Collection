from pathlib import Path
import runpy, re, textwrap
HERE = Path(__file__).resolve().parent
specs = runpy.run_path(str(HERE / 'specs.py'))['specs']
ROOT = HERE
import shutil
# Remove only generated folders; never touch .git, workflows or bootstrap scripts.
for generated_dir in ROOT.glob('[0-9][0-9]-*'):
    if generated_dir.is_dir():
        shutil.rmtree(generated_dir)
shutil.rmtree(ROOT / 'shared-ui', ignore_errors=True)
ROOT.mkdir(parents=True, exist_ok=True)

def w(path, content):
    p=ROOT/path
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding='utf-8')

def k(s): return textwrap.dedent(s).strip('\n')

# Root Gradle files.
w(Path('settings.gradle.kts'), k('''
// Root settings for the 78-app Android collection.
// Each numbered folder is mapped to a short Gradle module name so paths stay readable.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "App-Collection"
include(":shared-ui")
''') + '\n' + '\n'.join([f'include(":p{r:02d}")\nproject(":p{r:02d}").projectDir = file("{r:02d}-{slug}")' for r,_,slug,*_ in specs]) + '\n')

w(Path('build.gradle.kts'), k('''
// Android Gradle Plugin is declared once and reused by all 78 application modules.
// AGP 9.3 includes built-in Kotlin support, so a separate Kotlin Android plugin is not required.
plugins {
    id("com.android.application") version "9.3.0" apply false
    id("com.android.library") version "9.3.0" apply false
}
''')+'\n')
w(Path('gradle.properties'), k('''
# AndroidX namespace behavior is enabled for compatibility with modern Android tooling.
android.useAndroidX=true
# Keep generated R classes small and module-scoped.
android.nonTransitiveRClass=true
# Allocate enough heap for a large multi-module collection.
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
# Allow Gradle to build independent app modules concurrently when possible.
org.gradle.parallel=true
''')+'\n')

# Shared UI module providing the global app shell (right-side hamburger drawer, settings, about, etc.).
w(Path('shared-ui/build.gradle.kts'), k('''
// Shared UI library used by every demo app in this repository.
plugins {
    id("com.android.library")
}

android {
    namespace = "com.asteam.appcollection.sharedui"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
    }
}
''')+'\n')
w(Path('shared-ui/src/main/AndroidManifest.xml'), k('''
<!-- Shared library manifest. The library declares no exported Android component. -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android" />
''')+'\n')
base_activity = r'''
package com.asteam.appcollection.sharedui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/**
 * Base screen shared by all 78 rebuilt applications.
 *
 * It intentionally uses Android platform Views only. This keeps every APK small and avoids
 * bringing an external UI dependency into 78 tiny educational applications.
 *
 * The shell implements the project-wide requirements:
 * - A hamburger button at the top-right.
 * - A drawer that opens from the right side.
 * - Settings with a notifications preference.
 * - Share, about-team, contact and about-software entries.
 * - About-software shows only a short description and the version, never the package name.
 */
abstract class BaseDemoActivity : Activity() {

    /** Human-readable screen title supplied by each app module. */
    protected abstract val demoTitle: String

    /** Short explanation displayed at the top of the demo content. */
    protected abstract val demoDescription: String

    /** Numbered source reference that was rebuilt into the current application. */
    protected abstract val sourceReference: String

    /** Right-side drawer container. */
    private lateinit var drawer: LinearLayout

    /** Main content area populated by the concrete demo. */
    private lateinit var demoContainer: LinearLayout

    /** Called once when Android creates the activity. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the complete screen in code so the educational source is visible in one place.
        setContentView(buildAppShell())

        // Add a concise description before the actual interactive sample.
        demoContainer.addView(label(demoDescription, 17f))

        // Delegate feature-specific controls to the concrete project.
        renderDemo(demoContainer)
    }

    /** Each application implements its own feature inside this method. */
    protected abstract fun renderDemo(container: LinearLayout)

    /** Builds the toolbar, scrollable content and right-side drawer. */
    private fun buildAppShell(): View {
        // FrameLayout lets the drawer overlay the main content when it opens.
        val frame = FrameLayout(this)

        // Main vertical page containing toolbar and content.
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 249, 252))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Compact top bar. The menu button is added first in RTL, therefore it appears on the right.
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
        }

        // Hamburger control opening the custom right drawer.
        val menuButton = Button(this).apply {
            text = "☰"
            textSize = 22f
            contentDescription = "باز کردن نوار همبرگری"
            setOnClickListener { toggleDrawer() }
        }

        // App title consumes the remaining width in the toolbar.
        val title = TextView(this).apply {
            text = demoTitle
            textSize = 20f
            setTextColor(Color.rgb(25, 30, 40))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }

        toolbar.addView(menuButton, LinearLayout.LayoutParams(dp(64), dp(52)))
        toolbar.addView(title, LinearLayout.LayoutParams(0, dp(52), 1f))
        page.addView(toolbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // Vertical container holding the feature-specific controls.
        demoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(32))
        }

        // Scrolling prevents small screens from cutting off controls.
        val scroll = ScrollView(this).apply {
            addView(demoContainer, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        page.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        frame.addView(page, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // Drawer is a real overlay anchored to Gravity.END (right in LTR and also kept visually right here).
        drawer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(28), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
            elevation = dp(16).toFloat()
        }

        // Drawer header identifies the developer group without exposing technical package metadata.
        drawer.addView(label("AS Team", 22f))
        drawer.addView(label("منوی برنامه", 15f))
        addDrawerItem("تنظیمات") { showSettings() }
        addDrawerItem("معرفی به دوستان") { shareApp() }
        addDrawerItem("درباره ما") { showAboutTeam() }
        addDrawerItem("تماس با ما") { showContact() }
        addDrawerItem("درباره نرم افزار") { showAboutSoftware() }
        addDrawerItem("بستن منو") { toggleDrawer() }

        val drawerParams = FrameLayout.LayoutParams(dp(290), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END)
        frame.addView(drawer, drawerParams)
        return frame
    }

    /** Adds one clickable row to the right drawer. */
    private fun addDrawerItem(text: String, action: () -> Unit) {
        val item = Button(this).apply {
            this.text = text
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setOnClickListener {
                // Close drawer before opening the destination dialog/action.
                drawer.visibility = View.GONE
                action()
            }
        }
        drawer.addView(item, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)))
    }

    /** Shows or hides the right-side hamburger drawer. */
    private fun toggleDrawer() {
        drawer.visibility = if (drawer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    /** Settings screen. Notification state is stored in SharedPreferences. */
    private fun showSettings() {
        val preferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        val notificationSwitch = Switch(this).apply {
            text = "اعلان‌ها فعال باشد"
            isChecked = preferences.getBoolean("notifications_enabled", true)
        }
        AlertDialog.Builder(this)
            .setTitle("تنظیمات")
            .setView(notificationSwitch)
            .setPositiveButton("ذخیره") { _, _ ->
                preferences.edit().putBoolean("notifications_enabled", notificationSwitch.isChecked).apply()
                Toast.makeText(this, "تنظیمات ذخیره شد", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /** Uses Android Sharesheet to introduce/share the current app. */
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, demoTitle)
            putExtra(Intent.EXTRA_TEXT, "$demoTitle - ساخته شده توسط گروه توسعه و برنامه نویسی AS Team")
        }
        startActivity(Intent.createChooser(shareIntent, "معرفی به دوستان"))
    }

    /** Displays the fixed developer-group copyright text. */
    private fun showAboutTeam() {
        showInfo(
            "درباره ما",
            "گروه توسعه و برنامه نویسی AS Team\n\nتمامی حقوق مربوط به این برنامه انحصاری میباشد"
        )
    }

    /** Displays support contact details. */
    private fun showContact() {
        showInfo(
            "تماس با ما",
            "گروه توسعه و برنامه نویسی AS Team\n\nایمیل پشتیبانی\nas.team.support@gmail.com"
        )
    }

    /** Shows only product description and version, as required by the project rules. */
    private fun showAboutSoftware() {
        val versionName = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")

        showInfo(
            "درباره نرم افزار",
            "$demoDescription\n\nنسخه: $versionName\n\nمنبع بازسازی: $sourceReference"
        )
    }

    /** Small helper for consistent informational dialogs. */
    protected fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("باشه", null)
            .show()
    }

    /** Creates a reusable TextView for explanations and output. */
    protected fun label(text: String = "", sizeSp: Float = 16f): TextView = TextView(this).apply {
        this.text = text
        textSize = sizeSp
        setTextColor(Color.rgb(35, 40, 50))
        setPadding(dp(6), dp(8), dp(6), dp(8))
        layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    /** Creates a reusable action button. */
    protected fun button(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = 16f
        isAllCaps = false
        setOnClickListener { onClick() }
    }

    /** Creates a reusable EditText with a visible hint. */
    protected fun input(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 16f
        setPadding(dp(10), dp(10), dp(10), dp(10))
    }

    /** Converts density-independent pixels into screen pixels. */
    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** Back first closes the drawer; a second back follows normal Android behavior. */
    @Deprecated("Android calls this for legacy back dispatch on supported API levels")
    override fun onBackPressed() {
        if (::drawer.isInitialized && drawer.visibility == View.VISIBLE) {
            drawer.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }
}
'''
w(Path('shared-ui/src/main/java/com/asteam/appcollection/sharedui/BaseDemoActivity.kt'), k(base_activity)+'\n')

# Common Kotlin import block used by app modules.
imports = r'''
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
'''

def render_for(kind, rank):
    extras=''
    if kind == 'gps':
        body = '''
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
'''
        extras = '''
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
                output.text = "Latitude: ${location.latitude}\nLongitude: ${location.longitude}\nAccuracy: ${location.accuracy} m"
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
'''
    elif kind == 'flashlight':
        body='''
        val state = label("وضعیت چراغ قوه: خاموش")
        container.addView(state)
        container.addView(button("روشن / خاموش") {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 502)
            } else {
                toggleTorch(state)
            }
        })
'''
        extras='''
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
'''
    elif kind == 'sqlite':
        body='''
        val nameInput = input("نام را وارد کنید")
        val output = label("هنوز رکوردی نمایش داده نشده است.")
        val helper = PeopleDb(this)
        container.addView(nameInput)
        container.addView(button("افزودن") {
            val value = nameInput.text.toString().trim()
            if (value.isNotEmpty()) helper.insertName(value)
            output.text = helper.readNames().joinToString(prefix = "رکوردها:\n", separator = "\n")
            nameInput.text.clear()
        })
        container.addView(button("تغییر اولین رکورد به مقدار واردشده") {
            val value = nameInput.text.toString().trim()
            if (value.isNotEmpty()) helper.updateFirst(value)
            output.text = helper.readNames().joinToString(prefix = "رکوردها:\n", separator = "\n")
        })
        container.addView(button("حذف همه") {
            helper.clearAll()
            output.text = "دیتابیس خالی شد."
        })
        container.addView(output)
'''
        extras='''
    /** Minimal SQLiteOpenHelper keeping the original CRUD idea but fixing resource handling. */
    private class PeopleDb(context: Context) : SQLiteOpenHelper(context, "people.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE people(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        fun insertName(name: String) = writableDatabase.execSQL("INSERT INTO people(name) VALUES(?)", arrayOf(name))
        fun updateFirst(name: String) = writableDatabase.execSQL("UPDATE people SET name=? WHERE id=(SELECT id FROM people ORDER BY id LIMIT 1)", arrayOf(name))
        fun clearAll() = writableDatabase.execSQL("DELETE FROM people")
        fun readNames(): List<String> {
            val result = mutableListOf<String>()
            readableDatabase.rawQuery("SELECT name FROM people ORDER BY id", null).use { cursor ->
                while (cursor.moveToNext()) result += cursor.getString(0)
            }
            return result
        }
    }
'''
    elif kind in ('music','musicui','audiomanifest'):
        body='''
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
'''
        extras='''
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
'''
    elif kind in ('webview','webviewui','webviewmanifest'):
        body='''
        val address = input("https://developer.android.com")
        address.setText("https://developer.android.com")
        val web = WebView(this).apply {
            settings.javaScriptEnabled = false
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }
        container.addView(address)
        container.addView(button("باز کردن آدرس") {
            val url = address.text.toString().trim()
            if (url.startsWith("https://")) web.loadUrl(url) else showInfo("URL", "برای این نمونه فقط HTTPS پذیرفته می‌شود.")
        })
        container.addView(web, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(420)))
'''
    elif kind == 'camera':
        body='''
        container.addView(label("این نسخه به‌جای API قدیمی Camera، دوربین ثبت‌شده سیستم را با Intent باز می‌کند."))
        container.addView(button("باز کردن دوربین") {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) startActivity(intent)
            else showInfo("دوربین", "برنامه دوربین روی این دستگاه پیدا نشد.")
        })
'''
    elif kind == 'battery':
        body='''
        val output = label()
        fun refresh() {
            val battery = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val percent = if (level >= 0) (level * 100f / scale) else 0f
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            output.text = "شارژ: %.1f%%\nدر حال شارژ: %s".format(percent, if (charging) "بله" else "خیر")
        }
        container.addView(output)
        container.addView(button("بروزرسانی وضعیت") { refresh() })
        refresh()
'''
    elif kind == 'service':
        body='''
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
'''
        extras='''
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
'''
        return body, extras, True
    elif kind in ('notification','notificationui','notificationperm'):
        body='''
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
'''
        extras='''
    /** Creates a NotificationChannel on Android 8+ and posts an OS-compliant notification. */
    private fun sendDemoNotification(title: String, text: String) {
        val channelId = "general"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel(channelId, "اعلان‌های عمومی", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, channelId) else @Suppress("DEPRECATION") Notification.Builder(this)
        builder.setContentTitle(title).setContentText(text).setSmallIcon(android.R.drawable.ic_dialog_info).setAutoCancel(true)
        manager.notify(100 + %d, builder.build())
    }
''' % rank
    elif kind == 'email':
        body='''
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
'''
    elif kind == 'share':
        body='''
        val text = input("متن برای اشتراک‌گذاری").apply { setText("سلام از App Collection") }
        container.addView(text)
        container.addView(button("اشتراک‌گذاری") {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text.text.toString())
            }
            startActivity(Intent.createChooser(intent, "اشتراک با..."))
        })
'''
    elif kind == 'ringtone':
        body='''
        container.addView(label("Ringtone Picker سیستم بدون دسترسی مستقیم به فایل‌های صوتی باز می‌شود."))
        container.addView(button("انتخاب صدای زنگ") {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE or RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            }
            startActivity(intent)
        })
'''
    elif kind == 'date':
        body='''
        val output = label("تاریخی انتخاب نشده است.")
        container.addView(output)
        container.addView(button("انتخاب تاریخ") {
            val calendar = java.util.Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                output.text = "%04d-%02d-%02d".format(year, month + 1, day)
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        })
'''
    elif kind == 'countdown':
        body='''
        val output = label("30")
        container.addView(output)
        container.addView(button("شروع ۳۰ ثانیه") {
            object : CountDownTimer(30_000L, 1_000L) {
                override fun onTick(millisUntilFinished: Long) { output.text = "${millisUntilFinished / 1000} ثانیه" }
                override fun onFinish() { output.text = "تمام شد" }
            }.start()
        })
'''
    elif kind == 'datetime':
        body='''
        val output = label()
        container.addView(output)
        container.addView(button("نمایش زمان فعلی") {
            output.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        })
        output.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
'''
    elif kind == 'browser':
        body='''
        val address = input("https://www.google.com").apply { setText("https://www.google.com") }
        container.addView(address)
        container.addView(button("باز کردن مرورگر") {
            val value = address.text.toString().trim()
            if (value.startsWith("https://")) startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
            else showInfo("لینک", "برای امنیت، آدرس با https:// وارد شود.")
        })
'''
    elif kind == 'dialer':
        body='''
        val number = input("شماره تلفن").apply { inputType = InputType.TYPE_CLASS_PHONE }
        container.addView(number)
        container.addView(button("باز کردن شماره‌گیر") {
            val value = number.text.toString().trim()
            if (value.isNotEmpty()) startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(value))))
        })
'''
    elif kind == 'gallery':
        body='''
        val scroller = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf(Color.rgb(60, 90, 180), Color.rgb(30, 150, 110), Color.rgb(210, 100, 70), Color.rgb(120, 80, 170)).forEachIndexed { index, color ->
            val tile = TextView(this).apply {
                text = "تصویر ${index + 1}"
                gravity = Gravity.CENTER
                textSize = 18f
                setTextColor(Color.WHITE)
                setBackgroundColor(color)
                setOnClickListener { showInfo("گالری", "آیتم ${index + 1} انتخاب شد.") }
            }
            row.addView(tile, LinearLayout.LayoutParams(dp(180), dp(180)).apply { setMargins(dp(6), dp(6), dp(6), dp(6)) })
        }
        scroller.addView(row)
        container.addView(scroller)
'''
    elif kind in ('customlist','customlistui','customrow','list','listui'):
        body='''
        val items = arrayOf("Android", "Kotlin", "Camera", "GPS", "SQLite", "WebView")
        val list = ListView(this)
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        list.setOnItemClickListener { _, _, position, _ -> showInfo("انتخاب", items[position]) }
        container.addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(320)))
'''
    elif kind == 'font':
        body='''
        val normal = label("نمونه فونت Sans Serif", 22f).apply { typeface = Typeface.create("sans-serif", Typeface.NORMAL) }
        val serif = label("نمونه فونت Serif Bold", 22f).apply { typeface = Typeface.create("serif", Typeface.BOLD) }
        val mono = label("نمونه فونت Monospace", 22f).apply { typeface = Typeface.MONOSPACE }
        container.addView(normal)
        container.addView(serif)
        container.addView(mono)
        container.addView(label("برای جلوگیری از توزیع فایل فونت ثالث، این بازسازی از خانواده فونت‌های موجود دستگاه استفاده می‌کند."))
'''
    elif kind == 'paint':
        body='''
        container.addView(label("با انگشت در کادر زیر نقاشی کنید."))
        val canvasView = FingerPaintView(this)
        container.addView(canvasView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(420)))
        container.addView(button("پاک کردن بوم") { canvasView.clearCanvas() })
'''
        extras='''
    /** Custom View providing the same finger-paint behavior with explicit state and invalidate calls. */
    private class FingerPaintView(context: Context) : View(context) {
        private val path = Path()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(20, 90, 190)
            style = Paint.Style.STROKE
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        init { setBackgroundColor(Color.WHITE) }
        override fun onDraw(canvas: Canvas) { super.onDraw(canvas); canvas.drawPath(path, paint) }
        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> path.moveTo(event.x, event.y)
                MotionEvent.ACTION_MOVE -> path.lineTo(event.x, event.y)
                MotionEvent.ACTION_UP -> path.lineTo(event.x, event.y)
            }
            invalidate()
            return true
        }
        fun clearCanvas() { path.reset(); invalidate() }
    }
'''
    elif kind in ('animation','move'):
        body='''
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
'''
    elif kind == 'chronometer':
        body='''
        val chrono = Chronometer(this).apply { textSize = 26f }
        container.addView(chrono)
        container.addView(button("شروع") { chrono.base = SystemClock.elapsedRealtime(); chrono.start() })
        container.addView(button("توقف") { chrono.stop() })
        container.addView(button("ریست") { chrono.stop(); chrono.base = SystemClock.elapsedRealtime() })
'''
    elif kind == 'rating':
        body='''
        val output = label("امتیاز: 0")
        val rating = RatingBar(this).apply { numStars = 5; stepSize = 0.5f }
        rating.setOnRatingBarChangeListener { _, value, _ -> output.text = "امتیاز: $value" }
        container.addView(rating)
        container.addView(output)
'''
    elif kind == 'spinner':
        body='''
        val items = arrayOf("Android", "Kotlin", "Java", "Compose")
        val output = label("انتخاب: Android")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { output.text = "انتخاب: ${items[position]}" }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
        container.addView(spinner)
        container.addView(output)
'''
    elif kind == 'autocomplete':
        body='''
        val values = arrayOf("Iran", "Germany", "India", "Japan", "Canada", "Brazil")
        val field = AutoCompleteTextView(this).apply {
            hint = "نام کشور را تایپ کنید"
            threshold = 1
            setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, values))
        }
        container.addView(field)
'''
    elif kind == 'multiautocomplete':
        body='''
        val values = arrayOf("Iran", "Germany", "India", "Japan", "Canada", "Brazil")
        val field = MultiAutoCompleteTextView(this).apply {
            hint = "چند کشور، جداشده با ویرگول"
            threshold = 1
            setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
            setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, values))
        }
        container.addView(field)
'''
    elif kind == 'grid':
        body='''
        val values = arrayOf("GPS", "Camera", "Web", "DB", "Audio", "Share")
        val grid = GridView(this).apply {
            numColumns = 2
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, values)
            setOnItemClickListener { _, _, position, _ -> showInfo("Grid", values[position]) }
        }
        container.addView(grid, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(300)))
'''
    elif kind == 'radio':
        body='''
        val output = label("گزینه‌ای انتخاب نشده است.")
        val group = RadioGroup(this)
        listOf("Android", "Kotlin", "Web").forEach { text -> group.addView(RadioButton(this).apply { this.text = text }) }
        group.setOnCheckedChangeListener { g, id -> output.text = "انتخاب: ${g.findViewById<RadioButton>(id)?.text ?: "-"}" }
        container.addView(group)
        container.addView(output)
'''
    elif kind == 'checkbox':
        body='''
        val output = label("هیچ گزینه‌ای انتخاب نشده است.")
        val a = CheckBox(this).apply { text = "GPS" }
        val b = CheckBox(this).apply { text = "Camera" }
        fun refresh() { output.text = "انتخاب‌ها: " + listOfNotNull(if (a.isChecked) "GPS" else null, if (b.isChecked) "Camera" else null).joinToString() }
        a.setOnCheckedChangeListener { _, _ -> refresh() }; b.setOnCheckedChangeListener { _, _ -> refresh() }
        container.addView(a); container.addView(b); container.addView(output)
'''
    elif kind == 'toggle':
        body='''
        val output = label("خاموش")
        val toggle = ToggleButton(this).apply { textOn = "روشن"; textOff = "خاموش" }
        toggle.setOnCheckedChangeListener { _, checked -> output.text = if (checked) "روشن" else "خاموش" }
        container.addView(toggle); container.addView(output)
'''
    elif kind == 'keyboard':
        body='''
        val field = input("نمونه ورودی")
        container.addView(field)
        container.addView(button("کیبورد ایمیل") { field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; field.requestFocus() })
        container.addView(button("کیبورد عدد") { field.inputType = InputType.TYPE_CLASS_NUMBER; field.requestFocus() })
        container.addView(button("کیبورد رمز") { field.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; field.requestFocus() })
'''
    elif kind == 'longpress':
        body='''
        val text = label("روی این متن لمس طولانی انجام دهید.", 20f).apply { setBackgroundColor(Color.YELLOW) }
        text.setOnLongClickListener {
            text.setBackgroundColor(Color.rgb(170, 220, 255))
            showInfo("Long Press", "رویداد لمس طولانی دریافت شد.")
            true
        }
        container.addView(text)
'''
    elif kind == 'html':
        body='''
        val html = "<h2>Android</h2><b>Bold</b><br><i>Italic</i><br><u>Underline</u>"
        val text = label("", 20f)
        text.text = if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT) else @Suppress("DEPRECATION") Html.fromHtml(html)
        container.addView(text)
'''
    elif kind in ('dialog','animateddialogui','dialogui'):
        body='''
        container.addView(button("نمایش دیالوگ") {
            AlertDialog.Builder(this)
                .setTitle("نمونه دیالوگ")
                .setSingleChoiceItems(arrayOf("گزینه ۱", "گزینه ۲", "گزینه ۳"), -1, null)
                .setPositiveButton("تأیید", null)
                .setNegativeButton("انصراف", null)
                .show()
        })
'''
    elif kind == 'animateddialog':
        body='''
        container.addView(button("نمایش دیالوگ انیمیشنی") {
            val content = label("این دیالوگ بدون کتابخانه NiftyDialog ساخته شده است.", 18f).apply { setPadding(dp(24), dp(24), dp(24), dp(24)) }
            val dialog = Dialog(this)
            dialog.setContentView(content)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog.show()
            content.alpha = 0f; content.scaleX = 0.8f; content.scaleY = 0.8f
            content.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(450).start()
        })
'''
    elif kind == 'toast':
        body='''
        val message = input("متن پیام").apply { setText("سلام از Toast") }
        container.addView(message)
        container.addView(button("نمایش پیام") { Toast.makeText(this, message.text.toString(), Toast.LENGTH_SHORT).show() })
        container.addView(label("در Android جدید، Toast سفارشی محدود شده است؛ برای UI پیچیده بهتر است Snackbar یا بنر داخل برنامه استفاده شود."))
'''
    elif kind in ('form','formui'):
        body='''
        val name = input("نام و نام خانوادگی")
        val phone = input("شماره تماس").apply { inputType = InputType.TYPE_CLASS_PHONE }
        val output = label()
        container.addView(name); container.addView(phone)
        container.addView(button("اعتبارسنجی") {
            output.text = when {
                name.text.toString().trim().length < 2 -> "نام معتبر وارد کنید."
                phone.text.toString().filter { it.isDigit() }.length < 7 -> "شماره تماس معتبر وارد کنید."
                else -> "اطلاعات معتبر است: ${name.text} / ${phone.text}"
            }
        })
        container.addView(output)
'''
    elif kind == 'gpsui':
        body='''
        container.addView(label("کارت وضعیت GPS", 20f))
        val status = label()
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        status.text = "GPS Provider: " + if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) "فعال" else "غیرفعال"
        container.addView(status)
        container.addView(button("رفتن به تنظیمات Location") { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
'''
    elif kind == 'gpsperm':
        body='''
        val output = label()
        fun refresh() { output.text = "Fine location: " + if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) "مجاز" else "بدون مجوز" }
        container.addView(output)
        container.addView(button("درخواست مجوز موقعیت") { requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 510) })
        refresh()
'''
    elif kind == 'emailui':
        body='''
        container.addView(label("نمونه رابط فرم ایمیل"))
        container.addView(input("گیرنده")); container.addView(input("موضوع")); container.addView(input("متن پیام"))
        container.addView(label("این پروژه روی ساختار UI تمرکز دارد؛ نسخه ۱۰ منطق کامل ارسال را نشان می‌دهد."))
'''
    elif kind in ('emailmanifest','manifestdemo'):
        body='''
        container.addView(label("این نمونه، فایل Manifest قدیمی را به یک Activity امن با android:exported=true فقط برای Launcher تبدیل کرده است."))
        container.addView(label("قاعده مدرن: فقط کامپوننت‌هایی که واقعاً باید از بیرون قابل دسترسی باشند exported=true می‌گیرند."))
        container.addView(button("نمایش تنظیمات برنامه در سیستم") {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        })
'''
    elif kind == 'notificationperm':
        body='''
        val output = label()
        fun refresh() { output.text = if (Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) "مجوز اعلان فعال است" else "مجوز اعلان داده نشده است" }
        container.addView(output)
        container.addView(button("درخواست مجوز اعلان") { if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 511); refresh() })
        refresh()
'''
    elif kind == 'migration':
        body='''
        container.addView(label("کد ۲۰۱۷ به کتابخانه‌های com.nineoldandroids و niftydialogeffects وابسته بود."))
        container.addView(label("بازسازی جدید dependency قدیمی را حذف کرده و از Dialog + ViewPropertyAnimator داخلی Android استفاده می‌کند."))
        container.addView(button("دیدن جایگزین") {
            val view = label("بدون dependency خارجی", 20f)
            AlertDialog.Builder(this).setTitle("Migration").setView(view).setPositiveButton("باشه", null).show()
        })
'''
    elif kind == 'clock':
        body='''
        val digital = TextClock(this).apply { format24Hour = "HH:mm:ss"; textSize = 32f; gravity = Gravity.CENTER }
        val analog = AnalogClockView(this)
        container.addView(digital)
        container.addView(analog, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(320)))
'''
        extras='''
    /** Custom analog clock replaces the deprecated platform AnalogClock widget. */
    private class AnalogClockView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.DKGRAY; strokeWidth = 6f; style = Paint.Style.STROKE }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f; val cy = height / 2f; val r = minOf(width, height) * 0.4f
            canvas.drawCircle(cx, cy, r, paint)
            val cal = java.util.Calendar.getInstance()
            val minute = cal.get(java.util.Calendar.MINUTE)
            val hour = cal.get(java.util.Calendar.HOUR)
            fun hand(angleDeg: Float, length: Float, width: Float) {
                val rad = Math.toRadians((angleDeg - 90).toDouble())
                paint.strokeWidth = width
                canvas.drawLine(cx, cy, cx + kotlin.math.cos(rad).toFloat() * length, cy + kotlin.math.sin(rad).toFloat() * length, paint)
            }
            hand(minute * 6f, r * 0.82f, 5f)
            hand((hour + minute / 60f) * 30f, r * 0.58f, 9f)
            postInvalidateDelayed(1_000L)
        }
    }
'''
    elif kind == 'progress':
        body='''
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
'''
    else:
        body='''
        container.addView(label("نمونه بازسازی‌شده برای این منبع آموزشی."))
        container.addView(button("اجرای نمونه") { showInfo("Demo", "کد Kotlin این بخش با ساختار مدرن Android بازسازی شده است.") })
'''
    return body, extras, False

# Per-module manifests permissions and component additions.
def manifest_for(kind, title):
    perms=[]
    if kind in ('gps','gpsperm'): perms += ['android.permission.ACCESS_FINE_LOCATION','android.permission.ACCESS_COARSE_LOCATION']
    if kind == 'flashlight': perms += ['android.permission.CAMERA']
    if kind in ('webview','webviewui','webviewmanifest'): perms += ['android.permission.INTERNET']
    if kind in ('notification','notificationui','notificationperm','service'): perms += ['android.permission.POST_NOTIFICATIONS']
    if kind == 'service': perms += ['android.permission.FOREGROUND_SERVICE','android.permission.FOREGROUND_SERVICE_DATA_SYNC']
    perm_lines='\n'.join([f'    <!-- Permission required by this rebuilt feature. -->\n    <uses-permission android:name="{p}" />' for p in dict.fromkeys(perms)])
    service='''
        <!-- Foreground service declared explicitly and kept private to this app. -->
        <service
            android:name=".DemoForegroundService"
            android:exported="false"
            android:foregroundServiceType="dataSync" />''' if kind=='service' else ''
    return f'''<!-- Modern manifest generated for the rebuilt Android sample. -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
{perm_lines}
    <!-- Application metadata intentionally avoids exposing implementation details in the UI. -->
    <application
        android:allowBackup="true"
        android:label="{title.replace('&','&amp;').replace('"','&quot;')}"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <!-- Launcher activity must be exported because it owns an intent-filter on Android 12+. -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>{service}
    </application>
</manifest>
'''

catalog_rows=[]
for rank, original, slug, category, desc, kind, suggestion in specs:
    folder=f'{rank:02d}-{slug}'
    module=f'p{rank:02d}'
    pkg=f'com.asteam.appcollection.p{rank:02d}'
    title=slug.replace('-', ' ').title()
    build=f'''// Application module #{rank:02d}: {slug}.\n// The package/applicationId stays stable so future versions can update over the installed APK.\nplugins {{\n    id("com.android.application")\n}}\n\nandroid {{\n    namespace = "{pkg}"\n    compileSdk = 37\n\n    defaultConfig {{\n        applicationId = "{pkg}"\n        minSdk = 23\n        targetSdk = 37\n        versionCode = 1\n        versionName = "1.0.0"\n    }}\n}}\n\n// Shared shell provides the standard hamburger drawer/settings/about screens.\ndependencies {{\n    implementation(project(":shared-ui"))\n}}\n'''
    w(Path(folder)/'build.gradle.kts', build)
    w(Path(folder)/'src/main/AndroidManifest.xml', manifest_for(kind,title))
    body, extras, service_closed = render_for(kind, rank)
    # The service kind returns extras that closes the activity class itself before service class.
    source=f'''package {pkg}\n\n{imports}\n\n/**\n * Rebuilt Kotlin application #{rank:02d}.\n * Original Dropbox source: {original}\n * Classification: {category}\n *\n * The old Java/XML idea is preserved, while deprecated APIs and missing validation/lifecycle\n * handling are replaced with Android-compatible implementations for the current toolchain.\n */\n@Suppress("DEPRECATION")\nclass MainActivity : BaseDemoActivity() {{\n\n    /** Visible title used by the shared app shell. */\n    override val demoTitle: String = "{title}"\n\n    /** Short user-facing explanation of the rebuilt sample. */\n    override val demoDescription: String = "{desc}"\n\n    /** Keeps traceability to the original 2017 text file. */\n    override val sourceReference: String = "{original}"\n\n    /** Adds the interactive controls for this specific sample. */\n    override fun renderDemo(container: LinearLayout) {{\n{textwrap.indent(textwrap.dedent(body).strip(), '        ')}\n    }}\n\n{textwrap.indent(textwrap.dedent(extras).strip(), '    ') if extras else ''}\n'''
    if not service_closed:
        source += '}\n'
    else:
        source += '}\n'  # closes DemoForegroundService after extras; Activity was closed by extras prefix.
    # Clean accidental doubled leading indentation, but preserve Kotlin formatting.
    w(Path(folder)/f'src/main/java/com/asteam/appcollection/p{rank:02d}/MainActivity.kt', source)
    readme=f'''# {rank:02d} - {title}\n\n- **فایل اصلی:** `{original}`\n- **دسته:** {category}\n- **بازسازی:** Kotlin + Android API 37\n- **نسخه:** 1.0.0\n- **هدف:** {desc}\n\n## چه چیزهایی اصلاح شد؟\n\n- ساختار Java/XML قدیمی به Kotlin تبدیل شد.\n- APIهای منسوخ تا حد ممکن با APIهای فعلی جایگزین شدند.\n- اعتبارسنجی، مدیریت خطا و رفتار مناسب نسخه‌های جدید Android اضافه شد.\n- کدها و فایل‌های پروژه دارای کامنت توضیحی هستند.\n- ساختار package/versionCode ثابت است تا نسخه‌های بعدی روی نسخه قبلی نصب شوند.\n- منوی همبرگری سمت راست، تنظیمات، اعلان‌ها، اشتراک، درباره ما، تماس با ما و درباره نرم‌افزار از `shared-ui` تأمین می‌شود.\n\n## پیشنهاد توسعه بعدی\n\n{suggestion}\n\n> این پیشنهاد هنوز به‌عنوان قابلیت اضافه پیاده‌سازی نشده تا تصمیم توسعه هر پروژه جداگانه گرفته شود.\n'''
    w(Path(folder)/'README.md', readme)
    catalog_rows.append((rank,folder,original,category,desc,suggestion))

# Root catalog and readme.
from collections import Counter
counts=Counter(s[3] for s in specs)
catalog=['# App Collection - 78 Rebuilt Android Sources','',f'- بازسازی ارزشمند: **{counts["بازسازی ارزشمند"]}**',f'- کد آموزشی مفید: **{counts["کد آموزشی مفید"]}**',f'- تکراری / منسوخ بازسازی‌شده: **{counts["تکراری / منسوخ بازسازی‌شده"]}**','', '| اولویت | پوشه | فایل اصلی | دسته | خروجی |','|---:|---|---|---|---|']
for rank,folder,original,category,desc,suggestion in catalog_rows:
    catalog.append(f'| {rank} | `{folder}` | `{original}` | {category} | {desc} |')
w(Path('CATALOG.md'),'\n'.join(catalog)+'\n')
root_readme=f'''# App-Collection\n\nاین مخزن بازسازی کامل ۷۸ فایل قدیمی پوشه `Android Source` است. هیچ ورودی حذف نشده است. هر فایل اولیه یک اپ Android شماره‌دار دارد و رتبه‌ها بر اساس ارزش کاربردی/توسعه‌ای مرتب شده‌اند.\n\n## وضعیت دسته‌بندی\n\n- **{counts['بازسازی ارزشمند']}** مورد: ارزش بازسازی و تبدیل به برنامه کاربردی بیشتری داشتند.\n- **{counts['کد آموزشی مفید']}** مورد: ماهیت اصلی آن‌ها نمونه‌کد آموزشی بود و به اپ آموزشی Kotlin تبدیل شدند.\n- **{counts['تکراری / منسوخ بازسازی‌شده']}** مورد: تکراری یا متکی به APIهای قدیمی بودند، اما حذف نشدند و نسخه جدید مستقل دریافت کردند.\n\n## ابزار ساخت\n\n- Android Gradle Plugin: 9.3.0\n- compileSdk / targetSdk: 37\n- minSdk: 23\n- Kotlin: built-in Kotlin support in AGP 9.x\n- Gradle مورد نیاز: 9.5.0 یا جدیدتر سازگار با AGP 9.3\n\n## ساختار\n\nهر پوشه `NN-name` یک application module مستقل است. ماژول `shared-ui` فقط پوسته مشترک رابط کاربری و منوی همبرگری را فراهم می‌کند تا کد مشترک ۷۸ بار کپی نشود.\n\nبرای فهرست کامل و رتبه‌بندی، فایل [CATALOG.md](CATALOG.md) را ببینید.\n\n## APK\n\nWorkflow موجود در `.github/workflows/build-apks.yml` همه ۷۸ ماژول را Build و APKهای Debug قابل نصب را در یک Artifact جمع می‌کند. برای نسخه انتشار عمومی باید signing key مخصوص انتشار تعریف شود.\n'''
w(Path('README.md'),root_readme)

workflow='''# Build all 78 APKs whenever source is pushed to main.\nname: Build 78 Android APKs\n\non:\n  push:\n    branches: [main]\n  workflow_dispatch:\n\npermissions:\n  contents: read\n\njobs:\n  build:\n    runs-on: ubuntu-latest\n    timeout-minutes: 60\n    steps:\n      - name: Checkout source\n        uses: actions/checkout@v4\n\n      - name: Set up JDK 17\n        uses: actions/setup-java@v4\n        with:\n          distribution: temurin\n          java-version: '17'\n\n      - name: Set up Android SDK\n        uses: android-actions/setup-android@v3\n\n      - name: Install API 37 and Build Tools\n        run: sdkmanager \"platforms;android-37\" \"build-tools;36.0.0\" \"platform-tools\"\n\n      - name: Set up Gradle 9.5.0\n        uses: gradle/actions/setup-gradle@v4\n        with:\n          gradle-version: '9.5.0'\n\n      - name: Build all debug APKs\n        run: gradle assembleDebug --no-daemon --stacktrace\n\n      - name: Collect APKs with unique names\n        shell: bash\n        run: |\n          mkdir -p apk-output\n          for module in [0-9][0-9]-*; do\n            apk=\"$module/build/outputs/apk/debug/$module-debug.apk\"\n            # AGP normally names the APK after the project module; fall back to the first debug APK.\n            if [ ! -f \"$apk\" ]; then\n              apk=$(find \"$module/build/outputs/apk/debug\" -maxdepth 1 -name '*.apk' | head -n 1)\n            fi\n            if [ -n \"$apk\" ] && [ -f \"$apk\" ]; then\n              cp \"$apk\" \"apk-output/$module.apk\"\n            fi\n          done\n          count=$(find apk-output -name '*.apk' | wc -l)\n          echo \"Collected $count APKs\"\n          test \"$count\" -eq 78\n\n      - name: Upload all APKs\n        uses: actions/upload-artifact@v4\n        with:\n          name: app-collection-78-apks\n          path: apk-output/*.apk\n          if-no-files-found: error\n'''
w(Path('.github/workflows/build-apks.yml'),workflow)

# A tiny gitignore keeps build artifacts out of source control.
w(Path('.gitignore'),'''# Gradle and Android build output\n.gradle/\n**/build/\nlocal.properties\n.idea/\n*.iml\n''')

# Validation summary.
files=[p for p in ROOT.rglob('*') if p.is_file()]
print('generated files:',len(files))
print('modules:',len(specs))
print('root:',ROOT)
