package com.asteam.appcollection.p11

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Standalone prototype used to validate the shared AS Team right-side hamburger drawer.
 *
 * This is the raw reusable platform. Each real application can replace colors, icons, typography,
 * backgrounds and cards while retaining the same navigation/profile/about/update behavior.
 * Meaningful implementation sections are documented so the component can later be ported cleanly.
 */
@Suppress("DEPRECATION")
class MainActivity : Activity() {

    /** Persistent local profile/settings store. No online user account is required for this demo. */
    private val prefs by lazy { getSharedPreferences("as_team_drawer_demo", MODE_PRIVATE) }

    /** Root overlay container holding the app page, dim scrim and physical-right drawer. */
    private lateinit var root: FrameLayout

    /** Main page host. Home, settings and about-software pages are rendered inside this activity. */
    private lateinit var pageHost: LinearLayout

    /** Right-side navigation drawer and its semi-transparent background layer. */
    private lateinit var drawer: LinearLayout
    private lateinit var scrim: View

    /** Profile widgets are retained so local changes appear immediately. */
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView

    /** Tracks whether Back should return to the home page instead of leaving the app. */
    private var secondaryPageOpen = false

    /** Request code for Android's system image picker. */
    private val imagePickerRequest = 2101

    /** Request code used only when Android 13+ requires runtime notification permission. */
    private val notificationPermissionRequest = 3401

    /** Private filename containing the final cropped profile photograph. */
    private val profilePhotoFileName = "profile_photo.jpg"

    /** Latest version kept temporarily if notification permission must be requested first. */
    private var pendingLatestVersion: String? = null

    /** Notification channel used specifically for update-available messages. */
    private val updateChannelId = "as_team_app_updates"

    /**
     * Demo metadata endpoint. Every production app gets its own metadata URL while sharing the
     * same checker code. This keeps version checks independent from Play Store availability.
     */
    private val updateMetadataUrl =
        "https://raw.githubusercontent.com/waxew/App-Collection/drawer-demo-build/update-metadata/drawer-demo.json"

    /**
     * Optional app-specific support/contact text.
     * A real app fills this value when the project has dedicated support channels.
     * Null intentionally demonstrates the project-wide AS Team fallback contact block.
     */
    private val appSpecificContactInfo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUpdateNotificationChannel()
        buildRootUi()
        renderHomePage()
    }

    /** Creates the global app shell and anchors the navigation panel to the physical right edge. */
    private fun buildRootUi() {
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(246, 248, 251))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        pageHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        root.addView(pageHost, FrameLayout.LayoutParams(-1, -1))

        // Tapping this darkened layer closes the drawer without affecting the underlying page.
        scrim = View(this).apply {
            setBackgroundColor(Color.argb(95, 0, 0, 0))
            visibility = View.GONE
            setOnClickListener { closeDrawer() }
        }
        root.addView(scrim, FrameLayout.LayoutParams(-1, -1))

        drawer = buildDrawer()
        root.addView(
            drawer,
            // Gravity.RIGHT is deliberate because END becomes the left edge inside an RTL parent.
            FrameLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT)
        )

        setContentView(root)
    }

    /** Builds the reusable raw drawer platform. Product-specific visual themes are applied later. */
    private fun buildDrawer(): LinearLayout {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            elevation = dp(20).toFloat()
            visibility = View.GONE
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val scrollContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(24), dp(18), dp(18))
        }

        // ----- Profile header -----
        profileImage = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_menu_camera)
            setBackgroundColor(Color.rgb(235, 239, 245))
            contentDescription = "تصویر پروفایل"
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setOval(0, 0, view.width, view.height)
                }
            }
            clipToOutline = true
            setOnClickListener { showProfileImageSheet() }
        }
        restoreProfileImage()

        val imageRow = FrameLayout(this).apply {
            addView(profileImage, FrameLayout.LayoutParams(dp(112), dp(112), Gravity.CENTER))
        }
        scrollContent.addView(imageRow, LinearLayout.LayoutParams(-1, dp(126)))

        profileName = TextView(this).apply {
            text = "👤  ${prefs.getString("profile_name", "کاربر برنامه")}" 
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(30, 36, 46))
            setPadding(dp(8), dp(8), dp(8), dp(14))
            setOnClickListener { editProfileName() }
        }
        scrollContent.addView(profileName, LinearLayout.LayoutParams(-1, -2))
        scrollContent.addView(divider())

        // ----- Fixed settings destination -----
        scrollContent.addView(drawerRow("⚙", "تنظیمات") {
            closeDrawer()
            renderSettingsPage()
        })
        scrollContent.addView(divider())

        // ----- Reserved area for product-specific navigation -----
        val reserved = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(18), dp(10), dp(18))
            addView(TextView(this@MainActivity).apply {
                text = "فضای گزینه‌های اختصاصی برنامه"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(125, 132, 145))
            })
            minimumHeight = dp(150)
            gravity = Gravity.CENTER
        }
        scrollContent.addView(reserved, LinearLayout.LayoutParams(-1, dp(170)))
        scrollContent.addView(divider())

        // "ارتباط با ما" is intentionally replaced by the richer About Software destination.
        scrollContent.addView(drawerRow("ⓘ", "درباره نرم افزار") {
            closeDrawer()
            renderAboutSoftwarePage()
        })

        // User requested this item directly underneath About Software in the shared drawer.
        scrollContent.addView(drawerRow("↗", "معرفی به دوستان") {
            closeDrawer()
            shareApp()
        })

        // Version and "other apps" no longer live inside the drawer; they belong to About Software.
        val scroller = ScrollView(this).apply {
            isFillViewport = true
            addView(
                scrollContent,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        panel.addView(scroller, LinearLayout.LayoutParams(-1, 0, 1f))
        return panel
    }

    /** Renders the neutral prototype home screen. Real apps replace only this visual content. */
    private fun renderHomePage() {
        secondaryPageOpen = false
        pageHost.removeAllViews()
        pageHost.addView(buildToolbar("نمونه نوار همبرگری"))

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(32), dp(28), dp(32))
        }
        body.addView(TextView(this).apply {
            text = "نمونه آزمایشی Drawer مشترک برنامه‌های AS Team"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(26, 32, 44))
        })
        body.addView(TextView(this).apply {
            text = "این پوسته خام است؛ رنگ، آیکون و استایل هر برنامه روی همین منطق اعمال می‌شود."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(90, 98, 112))
            setPadding(0, dp(16), 0, 0)
        })
        pageHost.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    /** Settings page shared conceptually by all apps; extra settings are appended per product. */
    private fun renderSettingsPage() {
        secondaryPageOpen = true
        pageHost.removeAllViews()
        pageHost.addView(buildToolbar("تنظیمات", showBack = true))

        val settings = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(28))
        }

        settings.addView(TextView(this).apply {
            text = "تنظیمات عمومی"
            textSize = 18f
            setTextColor(Color.rgb(28, 34, 45))
            setPadding(dp(6), dp(6), dp(6), dp(14))
        })

        // Notifications remain a mandatory setting in every app using the shared platform.
        val notifications = CheckBox(this).apply {
            text = "اعلان‌ها"
            textSize = 16f
            isChecked = prefs.getBoolean("notifications_enabled", true)
            setPadding(dp(6), dp(8), dp(6), dp(8))
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("notifications_enabled", checked).apply()
            }
        }
        settings.addView(notifications, LinearLayout.LayoutParams(-1, -2))

        settings.addView(TextView(this).apply {
            text = "تنظیمات اختصاصی هر برنامه نیز در همین صفحه و با ظاهر همان برنامه اضافه می‌شود."
            textSize = 14f
            setTextColor(Color.rgb(100, 108, 122))
            setPadding(dp(6), dp(12), dp(6), dp(6))
        })

        pageHost.addView(settings, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    /**
     * Full About Software page containing product purpose, contact methods, AS Team identity,
     * dynamic installed version, update check and the Other AS Team Apps destination.
     */
    private fun renderAboutSoftwarePage() {
        secondaryPageOpen = true
        pageHost.removeAllViews()
        pageHost.addView(buildToolbar("درباره نرم افزار", showBack = true))

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(22), dp(18), dp(22), dp(28))
        }

        // ----- 1. Software purpose and operation -----
        content.addView(TextView(this).apply {
            text = "درباره این نرم افزار"
            textSize = 19f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(28, 34, 45))
            setPadding(dp(6), dp(8), dp(6), dp(12))
        })
        content.addView(TextView(this).apply {
            text = "این نسخه آزمایشی برای بررسی ساختار استاندارد منوی همبرگری برنامه‌های AS Team ساخته شده است. هدف آن نمایش پروفایل کاربر، تنظیمات، صفحه درباره نرم افزار، مدیریت تصویر پروفایل و زیرساخت بررسی نسخه جدید است. در هر برنامه واقعی، این متن با توضیح دقیق عملکرد، هدف و کارهایی که همان نرم افزار انجام می‌دهد جایگزین می‌شود."
            textSize = 15.5f
            setTextColor(Color.rgb(62, 69, 82))
            setLineSpacing(0f, 1.25f)
            setPadding(dp(6), 0, dp(6), dp(18))
        })

        content.addView(divider())

        // ----- 2. Contact methods -----
        content.addView(TextView(this).apply {
            text = "☎  راه های ارتباطی با ما :"
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(31, 37, 49))
            setPadding(dp(6), dp(18), dp(6), dp(10))
        })

        val contactText = appSpecificContactInfo?.takeIf { it.isNotBlank() }
            ?: "Develop by AS Team Group\nAS.Support.info@Gmail.Com"

        content.addView(TextView(this).apply {
            text = contactText
            textSize = 15f
            setTextColor(Color.rgb(75, 83, 98))
            setLineSpacing(0f, 1.2f)
            setPadding(dp(6), 0, dp(6), dp(22))
        })

        // Flexible space keeps the fixed team block away from the very bottom, matching the standard.
        content.addView(View(this), LinearLayout.LayoutParams(-1, dp(36)))
        content.addView(divider())

        // Fixed AS Team identity block moved here from the removed Contact page.
        content.addView(TextView(this).apply {
            text = "گروه توسعه فناوری و نرم افزاری as Team"
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(28, 34, 45))
            setPadding(dp(8), dp(20), dp(8), dp(7))
        })
        content.addView(TextView(this).apply {
            text = "AS.Support.info@Gmail.Com"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(80, 88, 104))
            setPadding(dp(8), 0, dp(8), dp(20))
        })

        content.addView(divider())

        // ----- 3. Installed version and explicit update check -----
        content.addView(TextView(this).apply {
            text = "نسخه ${currentVersion()}"
            textSize = 16f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.rgb(45, 52, 65))
            setPadding(0, dp(18), 0, dp(8))
        })

        content.addView(Button(this).apply {
            text = "⟳"
            textSize = 28f
            gravity = Gravity.CENTER
            contentDescription = "بررسی به روز بودن نرم افزار"
            isAllCaps = false
            setOnClickListener {
                isEnabled = false
                text = "…"
                checkForUpdates {
                    isEnabled = true
                    text = "⟳"
                }
            }
        }, LinearLayout.LayoutParams(dp(72), dp(62)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })

        content.addView(TextView(this).apply {
            text = "بررسی آخرین نسخه"
            textSize = 13.5f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(105, 112, 125))
            setPadding(0, 0, 0, dp(18))
        })

        content.addView(divider())

        // ----- 4. Other AS Team applications -----
        content.addView(Button(this).apply {
            text = "▦  سایر برنامه های AS Team"
            textSize = 16f
            isAllCaps = false
            setOnClickListener {
                Toast.makeText(this@MainActivity, "لینک این بخش بعداً اضافه می‌شود", Toast.LENGTH_SHORT).show()
            }
        }, LinearLayout.LayoutParams(-1, dp(58)).apply {
            topMargin = dp(14)
            bottomMargin = dp(18)
        })

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(-1, -2))
        }
        pageHost.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    /** Shares a short introduction using Android Sharesheet; store link can be injected later. */
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "AS Team Drawer Demo")
            putExtra(
                Intent.EXTRA_TEXT,
                "این برنامه را ببینید؛ توسعه داده شده توسط AS Team."
            )
        }
        startActivity(Intent.createChooser(shareIntent, "معرفی به دوستان"))
    }

    /** Toolbar shared by prototype pages; hamburger always remains at the upper-right. */
    private fun buildToolbar(titleText: String, showBack: Boolean = false): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
            elevation = dp(4).toFloat()
        }

        val action = Button(this).apply {
            text = if (showBack) "‹" else "☰"
            textSize = if (showBack) 30f else 24f
            isAllCaps = false
            contentDescription = if (showBack) "بازگشت" else "باز کردن نوار همبرگری"
            setOnClickListener {
                if (showBack) renderHomePage() else openDrawer()
            }
        }
        bar.addView(action, LinearLayout.LayoutParams(dp(62), dp(52)))

        bar.addView(TextView(this).apply {
            text = titleText
            textSize = 19f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.rgb(25, 31, 42))
            setPadding(dp(12), 0, dp(12), 0)
        }, LinearLayout.LayoutParams(0, dp(52), 1f))

        return bar
    }

    /** Creates one reusable RTL drawer row with a semantic icon and title. */
    private fun drawerRow(icon: String, title: String, onClick: () -> Unit): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(4), dp(10), dp(4))
            isClickable = true
            isFocusable = true
            setBackgroundColor(Color.TRANSPARENT)
            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 22f
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(dp(48), dp(52)))
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
                setTextColor(Color.rgb(35, 41, 52))
            }, LinearLayout.LayoutParams(0, dp(52), 1f))
            setOnClickListener { onClick() }
        }
    }

    /** Thin neutral divider keeps the raw platform clean without dictating product-specific styling. */
    private fun divider(): View = View(this).apply {
        setBackgroundColor(Color.rgb(229, 233, 240))
        layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply {
            topMargin = dp(6)
            bottomMargin = dp(6)
        }
    }

    /** Opens the physical-right drawer with a short slide animation. */
    private fun openDrawer() {
        if (drawer.visibility == View.VISIBLE) return
        drawer.visibility = View.VISIBLE
        scrim.visibility = View.VISIBLE
        drawer.translationX = dp(320).toFloat()
        drawer.animate().translationX(0f).setDuration(220).start()
    }

    /** Closes the drawer and then removes its page dim layer. */
    private fun closeDrawer() {
        if (drawer.visibility != View.VISIBLE) return
        drawer.animate().translationX(dp(320).toFloat()).setDuration(180).withEndAction {
            drawer.visibility = View.GONE
            scrim.visibility = View.GONE
            drawer.translationX = 0f
        }.start()
    }

    /** Presents profile-photo actions without requiring broad storage permissions. */
    private fun showProfileImageSheet() {
        AlertDialog.Builder(this)
            .setTitle("تصویر پروفایل")
            .setItems(arrayOf("انتخاب و برش عکس", "حذف تصویر فعلی")) { _, which ->
                if (which == 0) pickProfileImage() else clearProfileImage()
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /** Uses Android's system document picker; only the selected image becomes readable by this app. */
    private fun pickProfileImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, imagePickerRequest)
    }

    /** Loads the selected image and opens the built-in drag/pinch/crop editor before saving. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != imagePickerRequest || resultCode != RESULT_OK) return

        val uri = data?.data ?: return
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val bitmap = decodeSampledBitmap(uri)
        if (bitmap == null) {
            Toast.makeText(this, "خواندن تصویر انجام نشد", Toast.LENGTH_SHORT).show()
            return
        }
        showCropDialog(bitmap)
    }

    /**
     * Decodes a reasonably sized bitmap so large camera images do not consume excessive memory.
     * The final profile photo is only 720px, so decoding full camera resolution is unnecessary.
     */
    private fun decodeSampledBitmap(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        val longestSide = maxOf(bounds.outWidth, bounds.outHeight)
        while (longestSide / sampleSize > 2200) sampleSize *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    /** Displays the crop view with pinch zoom, one-finger drag and explicit save/cancel actions. */
    private fun showCropDialog(bitmap: Bitmap) {
        val cropView = ProfileCropView(this).apply {
            setSourceBitmap(bitmap)
            minimumHeight = dp(360)
        }

        val helper = TextView(this).apply {
            text = "با دو انگشت زوم کنید و با یک انگشت تصویر را جابه‌جا کنید."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(80, 88, 100))
            setPadding(dp(10), dp(8), dp(10), dp(10))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(4))
            addView(helper, LinearLayout.LayoutParams(-1, -2))
            addView(cropView, LinearLayout.LayoutParams(-1, dp(390)))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("تنظیم تصویر پروفایل")
            .setView(content)
            .setPositiveButton("ذخیره", null)
            .setNegativeButton("انصراف") { _, _ -> bitmap.recycle() }
            .create()

        dialog.setOnShowListener {
            // Override default dismissal so a failed crop does not unexpectedly close the editor.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val cropped = cropView.createCroppedBitmap()
                if (cropped == null) {
                    Toast.makeText(this, "برش تصویر انجام نشد", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (saveProfileBitmap(cropped)) {
                    profileImage.setImageBitmap(cropped)
                    prefs.edit().putBoolean("has_profile_photo", true).apply()
                    bitmap.recycle()
                    dialog.dismiss()
                } else {
                    cropped.recycle()
                    Toast.makeText(this, "ذخیره تصویر انجام نشد", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.setOnDismissListener {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        dialog.show()
    }

    /** Saves the final cropped photo into private app storage. */
    private fun saveProfileBitmap(bitmap: Bitmap): Boolean = runCatching {
        val target = File(filesDir, profilePhotoFileName)
        FileOutputStream(target).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        }
        true
    }.getOrDefault(false)

    /** Restores the privately stored cropped profile photo whenever the application starts. */
    private fun restoreProfileImage() {
        val target = File(filesDir, profilePhotoFileName)
        if (!target.exists()) return
        runCatching { BitmapFactory.decodeFile(target.absolutePath) }
            .getOrNull()
            ?.let { profileImage.setImageBitmap(it) }
    }

    /** Removes profile metadata and the private cropped-image file. */
    private fun clearProfileImage() {
        File(filesDir, profilePhotoFileName).delete()
        prefs.edit().remove("has_profile_photo").apply()
        profileImage.setImageResource(android.R.drawable.ic_menu_camera)
    }

    /** Local profile-name editor displayed by tapping the name row. */
    private fun editProfileName() {
        val editor = EditText(this).apply {
            setText(prefs.getString("profile_name", "کاربر برنامه"))
            hint = "نام کاربر"
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }
        AlertDialog.Builder(this)
            .setTitle("نام کاربر")
            .setView(editor)
            .setPositiveButton("ذخیره") { _, _ ->
                val value = editor.text.toString().trim().ifBlank { "کاربر برنامه" }
                prefs.edit().putString("profile_name", value).apply()
                profileName.text = "👤  $value"
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /**
     * Downloads tiny JSON metadata on a worker thread and compares its latestVersion with the
     * versionName currently installed. If an update exists, the app posts a real notification.
     */
    private fun checkForUpdates(onFinished: () -> Unit) {
        Toast.makeText(this, "در حال بررسی آخرین نسخه…", Toast.LENGTH_SHORT).show()

        Thread {
            val result = runCatching {
                val connection = (URL(updateMetadataUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 7000
                    readTimeout = 7000
                    requestMethod = "GET"
                    useCaches = false
                }

                try {
                    if (connection.responseCode !in 200..299) {
                        error("HTTP ${connection.responseCode}")
                    }
                    val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                    JSONObject(jsonText).getString("latestVersion")
                } finally {
                    connection.disconnect()
                }
            }

            runOnUiThread {
                onFinished()
                result.onSuccess { latestVersion ->
                    if (isVersionNewer(latestVersion, currentVersion())) {
                        notifyUpdateAvailable(latestVersion)
                    } else {
                        Toast.makeText(this, "نرم افزار به‌روز است", Toast.LENGTH_SHORT).show()
                    }
                }.onFailure {
                    Toast.makeText(this, "بررسی نسخه انجام نشد؛ اتصال اینترنت را بررسی کنید", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    /** Compares dotted numeric version names such as 1.2.10 and 1.3.0 safely. */
    private fun isVersionNewer(latest: String, installed: String): Boolean {
        val latestParts = latest.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val installedParts = installed.split(".").map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }
        val count = maxOf(latestParts.size, installedParts.size)

        for (index in 0 until count) {
            val latestValue = latestParts.getOrElse(index) { 0 }
            val installedValue = installedParts.getOrElse(index) { 0 }
            if (latestValue > installedValue) return true
            if (latestValue < installedValue) return false
        }
        return false
    }

    /** Creates the Android notification channel once on Android 8.0 and newer. */
    private fun createUpdateNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            updateChannelId,
            "به‌روزرسانی نرم افزار",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "اعلان رسیدن نسخه جدید برنامه"
        }
        manager.createNotificationChannel(channel)
    }

    /** Posts the update notification, requesting Android 13+ permission only when actually needed. */
    private fun notifyUpdateAvailable(latestVersion: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingLatestVersion = latestVersion
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), notificationPermissionRequest)
            return
        }
        postUpdateNotification(latestVersion)
    }

    /** Builds the actual system notification indicating that a newer release is available. */
    private fun postUpdateNotification(latestVersion: String) {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, updateChannelId)
        } else {
            Notification.Builder(this)
        }

        val notification = builder
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("نسخه جدید نرم افزار منتشر شده")
            .setContentText("نسخه $latestVersion در دسترس است.")
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(9101, notification)
    }

    /** Handles the notification permission result and falls back to an in-app alert if denied. */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != notificationPermissionRequest) return

        val latestVersion = pendingLatestVersion ?: return
        pendingLatestVersion = null

        if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            postUpdateNotification(latestVersion)
        } else {
            AlertDialog.Builder(this)
                .setTitle("نسخه جدید رسیده")
                .setMessage("نسخه $latestVersion منتشر شده است. برای دریافت اعلان سیستمی، دسترسی اعلان‌ها را فعال کنید.")
                .setPositiveButton("باشه", null)
                .show()
        }
    }

    /** Reads versionName from the installed APK instead of hard-coding it in the UI. */
    private fun currentVersion(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    /** Density helper keeps physical dimensions consistent across different Android screens. */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** Back closes drawer first, then exits subpages, and only then follows normal Android behavior. */
    override fun onBackPressed() {
        when {
            ::drawer.isInitialized && drawer.visibility == View.VISIBLE -> closeDrawer()
            secondaryPageOpen -> renderHomePage()
            else -> super.onBackPressed()
        }
    }
}
