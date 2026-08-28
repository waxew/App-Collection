package com.asteam.appcollection.test

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Main launcher for the consolidated testing APK.
 *
 * The user installs only this one application ("تست"). Each button opens the real
 * rebuilt MainActivity for that numbered example. Direct class references are used
 * deliberately: if even one numbered activity is missing from the consolidated Kotlin
 * source set, compilation fails instead of producing an apparently valid but broken APK.
 */
class TestLauncherActivity : Activity() {

    /** Metadata shown in the launcher plus the real compile-time Activity class. */
    private data class DemoItem(
        val number: Int,
        val title: String,
        val activityClass: Class<out Activity>
    )

    /**
     * Complete list of all 78 rebuilt examples in their existing priority order.
     * Fully-qualified class literals avoid ambiguous imports because every module uses
     * the simple class name MainActivity.
     */
    private val demos = listOf(
        DemoItem(1, "GPS Live Location", com.asteam.appcollection.p01.MainActivity::class.java),
        DemoItem(2, "Flashlight", com.asteam.appcollection.p02.MainActivity::class.java),
        DemoItem(3, "SQLite CRUD", com.asteam.appcollection.p03.MainActivity::class.java),
        DemoItem(4, "Music Player", com.asteam.appcollection.p04.MainActivity::class.java),
        DemoItem(5, "WebView Browser", com.asteam.appcollection.p05.MainActivity::class.java),
        DemoItem(6, "Camera Launcher", com.asteam.appcollection.p06.MainActivity::class.java),
        DemoItem(7, "Battery Monitor", com.asteam.appcollection.p07.MainActivity::class.java),
        DemoItem(8, "Foreground Service", com.asteam.appcollection.p08.MainActivity::class.java),
        DemoItem(9, "Notification Modern", com.asteam.appcollection.p09.MainActivity::class.java),
        DemoItem(10, "Send Email", com.asteam.appcollection.p10.MainActivity::class.java),
        DemoItem(11, "Share Content", com.asteam.appcollection.p11.MainActivity::class.java),
        DemoItem(12, "Ringtone Picker", com.asteam.appcollection.p12.MainActivity::class.java),
        DemoItem(13, "Date Picker", com.asteam.appcollection.p13.MainActivity::class.java),
        DemoItem(14, "Countdown Timer", com.asteam.appcollection.p14.MainActivity::class.java),
        DemoItem(15, "Date Time", com.asteam.appcollection.p15.MainActivity::class.java),
        DemoItem(16, "Open Browser", com.asteam.appcollection.p16.MainActivity::class.java),
        DemoItem(17, "Dialer", com.asteam.appcollection.p17.MainActivity::class.java),
        DemoItem(18, "Image Gallery", com.asteam.appcollection.p18.MainActivity::class.java),
        DemoItem(19, "Custom List", com.asteam.appcollection.p19.MainActivity::class.java),
        DemoItem(20, "Custom Font", com.asteam.appcollection.p20.MainActivity::class.java),
        DemoItem(21, "Finger Paint", com.asteam.appcollection.p21.MainActivity::class.java),
        DemoItem(22, "Image Animations", com.asteam.appcollection.p22.MainActivity::class.java),
        DemoItem(23, "Chronometer", com.asteam.appcollection.p23.MainActivity::class.java),
        DemoItem(24, "Rating Bar", com.asteam.appcollection.p24.MainActivity::class.java),
        DemoItem(25, "Spinner Selector", com.asteam.appcollection.p25.MainActivity::class.java),
        DemoItem(26, "Autocomplete", com.asteam.appcollection.p26.MainActivity::class.java),
        DemoItem(27, "Multi Autocomplete", com.asteam.appcollection.p27.MainActivity::class.java),
        DemoItem(28, "Simple List", com.asteam.appcollection.p28.MainActivity::class.java),
        DemoItem(29, "Grid View", com.asteam.appcollection.p29.MainActivity::class.java),
        DemoItem(30, "Radio Group", com.asteam.appcollection.p30.MainActivity::class.java),
        DemoItem(31, "Checkbox", com.asteam.appcollection.p31.MainActivity::class.java),
        DemoItem(32, "Toggle Button", com.asteam.appcollection.p32.MainActivity::class.java),
        DemoItem(33, "Keyboard Input Types", com.asteam.appcollection.p33.MainActivity::class.java),
        DemoItem(34, "Long Press Context", com.asteam.appcollection.p34.MainActivity::class.java),
        DemoItem(35, "HTML Text", com.asteam.appcollection.p35.MainActivity::class.java),
        DemoItem(36, "Choice Dialog", com.asteam.appcollection.p36.MainActivity::class.java),
        DemoItem(37, "Animated Dialog", com.asteam.appcollection.p37.MainActivity::class.java),
        DemoItem(38, "Custom Toast", com.asteam.appcollection.p38.MainActivity::class.java),
        DemoItem(39, "Form Validation", com.asteam.appcollection.p39.MainActivity::class.java),
        DemoItem(40, "Array Adapter List", com.asteam.appcollection.p40.MainActivity::class.java),
        DemoItem(41, "GPS UI Layout", com.asteam.appcollection.p41.MainActivity::class.java),
        DemoItem(42, "GPS Permissions", com.asteam.appcollection.p42.MainActivity::class.java),
        DemoItem(43, "Music Player UI", com.asteam.appcollection.p43.MainActivity::class.java),
        DemoItem(44, "Audio Manifest Modern", com.asteam.appcollection.p44.MainActivity::class.java),
        DemoItem(45, "WebView UI Layout", com.asteam.appcollection.p45.MainActivity::class.java),
        DemoItem(46, "WebView Manifest Modern", com.asteam.appcollection.p46.MainActivity::class.java),
        DemoItem(47, "Email Form UI", com.asteam.appcollection.p47.MainActivity::class.java),
        DemoItem(48, "Email Manifest", com.asteam.appcollection.p48.MainActivity::class.java),
        DemoItem(49, "Notification Sound", com.asteam.appcollection.p49.MainActivity::class.java),
        DemoItem(50, "Notification UI", com.asteam.appcollection.p50.MainActivity::class.java),
        DemoItem(51, "Notification Permission", com.asteam.appcollection.p51.MainActivity::class.java),
        DemoItem(52, "Animated Dialog UI", com.asteam.appcollection.p52.MainActivity::class.java),
        DemoItem(53, "Dialog Dependency Migration", com.asteam.appcollection.p53.MainActivity::class.java),
        DemoItem(54, "Form UI Layout", com.asteam.appcollection.p54.MainActivity::class.java),
        DemoItem(55, "Form Manifest", com.asteam.appcollection.p55.MainActivity::class.java),
        DemoItem(56, "Custom List Layout", com.asteam.appcollection.p56.MainActivity::class.java),
        DemoItem(57, "Custom List Row", com.asteam.appcollection.p57.MainActivity::class.java),
        DemoItem(58, "Array List Layout", com.asteam.appcollection.p58.MainActivity::class.java),
        DemoItem(59, "List Manifest", com.asteam.appcollection.p59.MainActivity::class.java),
        DemoItem(60, "ListActivity Modern", com.asteam.appcollection.p60.MainActivity::class.java),
        DemoItem(61, "ListActivity Manifest", com.asteam.appcollection.p61.MainActivity::class.java),
        DemoItem(62, "Dialog Suite", com.asteam.appcollection.p62.MainActivity::class.java),
        DemoItem(63, "Custom Dialog One", com.asteam.appcollection.p63.MainActivity::class.java),
        DemoItem(64, "Custom Dialog Layout", com.asteam.appcollection.p64.MainActivity::class.java),
        DemoItem(65, "Custom Dialog Manifest", com.asteam.appcollection.p65.MainActivity::class.java),
        DemoItem(66, "Custom Dialog Two", com.asteam.appcollection.p66.MainActivity::class.java),
        DemoItem(67, "Custom Dialog Two Manifest", com.asteam.appcollection.p67.MainActivity::class.java),
        DemoItem(68, "Toast Layout Migration", com.asteam.appcollection.p68.MainActivity::class.java),
        DemoItem(69, "Toast Manifest", com.asteam.appcollection.p69.MainActivity::class.java),
        DemoItem(70, "Flashlight Legacy Rewrite", com.asteam.appcollection.p70.MainActivity::class.java),
        DemoItem(71, "Clock Modern", com.asteam.appcollection.p71.MainActivity::class.java),
        DemoItem(72, "Clock Toolbox", com.asteam.appcollection.p72.MainActivity::class.java),
        DemoItem(73, "Finger Paint Toolbox", com.asteam.appcollection.p73.MainActivity::class.java),
        DemoItem(74, "Move Object Animation", com.asteam.appcollection.p74.MainActivity::class.java),
        DemoItem(75, "Move Text Animation", com.asteam.appcollection.p75.MainActivity::class.java),
        DemoItem(76, "Date Picker Toolbox", com.asteam.appcollection.p76.MainActivity::class.java),
        DemoItem(77, "Send Email Toolbox", com.asteam.appcollection.p77.MainActivity::class.java),
        DemoItem(78, "Progress Dialog Modern", com.asteam.appcollection.p78.MainActivity::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A programmatic layout keeps this host independent from extra XML resources.
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        val title = TextView(this).apply {
            text = "تست ۷۸ بخش"
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "برای اجرای هر نمونه، روی گزینه مربوطه بزنید."
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(16))
        }
        root.addView(subtitle)

        // One real launch button is created for every original application module.
        demos.forEach { demo ->
            val button = Button(this).apply {
                text = "%02d - %s".format(demo.number, demo.title)
                isAllCaps = false
                gravity = Gravity.CENTER_VERTICAL
                setOnClickListener { openDemo(demo) }
            }
            root.addView(
                button,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
            )
        }

        setContentView(
            ScrollView(this).apply {
                isFillViewport = true
                addView(root)
            }
        )
    }

    /** Starts the actual embedded Activity. Missing classes are compile-time errors now. */
    private fun openDemo(demo: DemoItem) {
        try {
            startActivity(Intent(this, demo.activityClass))
        } catch (error: Throwable) {
            // Preserve the concrete exception type so device testing immediately reveals the cause.
            Toast.makeText(
                this,
                "بخش ${demo.number} اجرا نشد: ${error.javaClass.simpleName}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Converts density-independent pixels to physical pixels for consistent spacing. */
    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
