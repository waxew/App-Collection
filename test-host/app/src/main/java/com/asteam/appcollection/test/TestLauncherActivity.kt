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
 * The user installs only this one application ("تست"). Each button opens the original
 * rebuilt MainActivity for that numbered example, so the test host does not replace
 * the feature logic with mock screens.
 */
class TestLauncherActivity : Activity() {

    /** Metadata shown in the launcher. The class name maps directly to each original module. */
    private data class DemoItem(
        val number: Int,
        val title: String,
        val activityClassName: String
    )

    /** Complete list of all 78 rebuilt examples in their existing priority order. */
    private val demos = listOf(
        DemoItem(1, "GPS Live Location", "com.asteam.appcollection.p01.MainActivity"),
        DemoItem(2, "Flashlight", "com.asteam.appcollection.p02.MainActivity"),
        DemoItem(3, "SQLite CRUD", "com.asteam.appcollection.p03.MainActivity"),
        DemoItem(4, "Music Player", "com.asteam.appcollection.p04.MainActivity"),
        DemoItem(5, "WebView Browser", "com.asteam.appcollection.p05.MainActivity"),
        DemoItem(6, "Camera Launcher", "com.asteam.appcollection.p06.MainActivity"),
        DemoItem(7, "Battery Monitor", "com.asteam.appcollection.p07.MainActivity"),
        DemoItem(8, "Foreground Service", "com.asteam.appcollection.p08.MainActivity"),
        DemoItem(9, "Notification Modern", "com.asteam.appcollection.p09.MainActivity"),
        DemoItem(10, "Send Email", "com.asteam.appcollection.p10.MainActivity"),
        DemoItem(11, "Share Content", "com.asteam.appcollection.p11.MainActivity"),
        DemoItem(12, "Ringtone Picker", "com.asteam.appcollection.p12.MainActivity"),
        DemoItem(13, "Date Picker", "com.asteam.appcollection.p13.MainActivity"),
        DemoItem(14, "Countdown Timer", "com.asteam.appcollection.p14.MainActivity"),
        DemoItem(15, "Date Time", "com.asteam.appcollection.p15.MainActivity"),
        DemoItem(16, "Open Browser", "com.asteam.appcollection.p16.MainActivity"),
        DemoItem(17, "Dialer", "com.asteam.appcollection.p17.MainActivity"),
        DemoItem(18, "Image Gallery", "com.asteam.appcollection.p18.MainActivity"),
        DemoItem(19, "Custom List", "com.asteam.appcollection.p19.MainActivity"),
        DemoItem(20, "Custom Font", "com.asteam.appcollection.p20.MainActivity"),
        DemoItem(21, "Finger Paint", "com.asteam.appcollection.p21.MainActivity"),
        DemoItem(22, "Image Animations", "com.asteam.appcollection.p22.MainActivity"),
        DemoItem(23, "Chronometer", "com.asteam.appcollection.p23.MainActivity"),
        DemoItem(24, "Rating Bar", "com.asteam.appcollection.p24.MainActivity"),
        DemoItem(25, "Spinner Selector", "com.asteam.appcollection.p25.MainActivity"),
        DemoItem(26, "Autocomplete", "com.asteam.appcollection.p26.MainActivity"),
        DemoItem(27, "Multi Autocomplete", "com.asteam.appcollection.p27.MainActivity"),
        DemoItem(28, "Simple List", "com.asteam.appcollection.p28.MainActivity"),
        DemoItem(29, "Grid View", "com.asteam.appcollection.p29.MainActivity"),
        DemoItem(30, "Radio Group", "com.asteam.appcollection.p30.MainActivity"),
        DemoItem(31, "Checkbox", "com.asteam.appcollection.p31.MainActivity"),
        DemoItem(32, "Toggle Button", "com.asteam.appcollection.p32.MainActivity"),
        DemoItem(33, "Keyboard Input Types", "com.asteam.appcollection.p33.MainActivity"),
        DemoItem(34, "Long Press Context", "com.asteam.appcollection.p34.MainActivity"),
        DemoItem(35, "HTML Text", "com.asteam.appcollection.p35.MainActivity"),
        DemoItem(36, "Choice Dialog", "com.asteam.appcollection.p36.MainActivity"),
        DemoItem(37, "Animated Dialog", "com.asteam.appcollection.p37.MainActivity"),
        DemoItem(38, "Custom Toast", "com.asteam.appcollection.p38.MainActivity"),
        DemoItem(39, "Form Validation", "com.asteam.appcollection.p39.MainActivity"),
        DemoItem(40, "Array Adapter List", "com.asteam.appcollection.p40.MainActivity"),
        DemoItem(41, "GPS UI Layout", "com.asteam.appcollection.p41.MainActivity"),
        DemoItem(42, "GPS Permissions", "com.asteam.appcollection.p42.MainActivity"),
        DemoItem(43, "Music Player UI", "com.asteam.appcollection.p43.MainActivity"),
        DemoItem(44, "Audio Manifest Modern", "com.asteam.appcollection.p44.MainActivity"),
        DemoItem(45, "WebView UI Layout", "com.asteam.appcollection.p45.MainActivity"),
        DemoItem(46, "WebView Manifest Modern", "com.asteam.appcollection.p46.MainActivity"),
        DemoItem(47, "Email Form UI", "com.asteam.appcollection.p47.MainActivity"),
        DemoItem(48, "Email Manifest", "com.asteam.appcollection.p48.MainActivity"),
        DemoItem(49, "Notification Sound", "com.asteam.appcollection.p49.MainActivity"),
        DemoItem(50, "Notification UI", "com.asteam.appcollection.p50.MainActivity"),
        DemoItem(51, "Notification Permission", "com.asteam.appcollection.p51.MainActivity"),
        DemoItem(52, "Animated Dialog UI", "com.asteam.appcollection.p52.MainActivity"),
        DemoItem(53, "Dialog Dependency Migration", "com.asteam.appcollection.p53.MainActivity"),
        DemoItem(54, "Form UI Layout", "com.asteam.appcollection.p54.MainActivity"),
        DemoItem(55, "Form Manifest", "com.asteam.appcollection.p55.MainActivity"),
        DemoItem(56, "Custom List Layout", "com.asteam.appcollection.p56.MainActivity"),
        DemoItem(57, "Custom List Row", "com.asteam.appcollection.p57.MainActivity"),
        DemoItem(58, "Array List Layout", "com.asteam.appcollection.p58.MainActivity"),
        DemoItem(59, "List Manifest", "com.asteam.appcollection.p59.MainActivity"),
        DemoItem(60, "ListActivity Modern", "com.asteam.appcollection.p60.MainActivity"),
        DemoItem(61, "ListActivity Manifest", "com.asteam.appcollection.p61.MainActivity"),
        DemoItem(62, "Dialog Suite", "com.asteam.appcollection.p62.MainActivity"),
        DemoItem(63, "Custom Dialog One", "com.asteam.appcollection.p63.MainActivity"),
        DemoItem(64, "Custom Dialog Layout", "com.asteam.appcollection.p64.MainActivity"),
        DemoItem(65, "Custom Dialog Manifest", "com.asteam.appcollection.p65.MainActivity"),
        DemoItem(66, "Custom Dialog Two", "com.asteam.appcollection.p66.MainActivity"),
        DemoItem(67, "Custom Dialog Two Manifest", "com.asteam.appcollection.p67.MainActivity"),
        DemoItem(68, "Toast Layout Migration", "com.asteam.appcollection.p68.MainActivity"),
        DemoItem(69, "Toast Manifest", "com.asteam.appcollection.p69.MainActivity"),
        DemoItem(70, "Flashlight Legacy Rewrite", "com.asteam.appcollection.p70.MainActivity"),
        DemoItem(71, "Clock Modern", "com.asteam.appcollection.p71.MainActivity"),
        DemoItem(72, "Clock Toolbox", "com.asteam.appcollection.p72.MainActivity"),
        DemoItem(73, "Finger Paint Toolbox", "com.asteam.appcollection.p73.MainActivity"),
        DemoItem(74, "Move Object Animation", "com.asteam.appcollection.p74.MainActivity"),
        DemoItem(75, "Move Text Animation", "com.asteam.appcollection.p75.MainActivity"),
        DemoItem(76, "Date Picker Toolbox", "com.asteam.appcollection.p76.MainActivity"),
        DemoItem(77, "Send Email Toolbox", "com.asteam.appcollection.p77.MainActivity"),
        DemoItem(78, "Progress Dialog Modern", "com.asteam.appcollection.p78.MainActivity")
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

    /**
     * Loads the exact activity class from the original module and starts it inside this host APK.
     * Reflection avoids 78 direct imports while still failing safely if a class is missing.
     */
    private fun openDemo(demo: DemoItem) {
        try {
            val targetClass = Class.forName(demo.activityClassName)
            startActivity(Intent(this, targetClass))
        } catch (error: Throwable) {
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
