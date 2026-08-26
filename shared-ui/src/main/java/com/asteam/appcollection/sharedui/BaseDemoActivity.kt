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
