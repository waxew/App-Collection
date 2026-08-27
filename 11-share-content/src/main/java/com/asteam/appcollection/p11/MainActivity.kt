package com.asteam.appcollection.p11

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Outline
import android.net.Uri
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
import java.io.File
import java.io.FileOutputStream

/**
 * Standalone prototype used to validate the shared AS Team right-side hamburger drawer.
 *
 * This file is intentionally self-contained and heavily commented because the resulting drawer
 * acts as the implementation reference for every Android application in this project. Visual
 * styling can vary per app while navigation behavior, profile editing and settings remain common.
 */
@Suppress("DEPRECATION")
class MainActivity : Activity() {

    /** Persistent local profile/settings store. No network account is required. */
    private val prefs by lazy { getSharedPreferences("as_team_drawer_demo", MODE_PRIVATE) }

    /** Root overlay container holding the app page, dim scrim and physical-right drawer. */
    private lateinit var root: FrameLayout

    /** Main page host. Home, settings and contact pages are rendered here. */
    private lateinit var pageHost: LinearLayout

    /** Right-side navigation drawer and its semi-transparent background layer. */
    private lateinit var drawer: LinearLayout
    private lateinit var scrim: View

    /** Profile widgets are retained so local changes appear immediately. */
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView

    /** Tracks whether Back should return to the home page instead of leaving the app. */
    private var secondaryPageOpen = false

    /** Request code for Android's system document picker. */
    private val imagePickerRequest = 2101

    /** Private filename containing the final cropped profile photograph. */
    private val profilePhotoFileName = "profile_photo.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    /** Builds the reusable raw drawer platform. Each product may later apply its own colors/icons. */
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

        // Communication destination is common, while upper page content remains product-specific.
        scrollContent.addView(drawerRow("☎", "ارتباط با ما") {
            closeDrawer()
            renderContactPage()
        })

        scrollContent.addView(divider())
        scrollContent.addView(drawerRow("▦", "سایر برنامه‌های ما") {
            Toast.makeText(this, "لینک این بخش بعداً اضافه می‌شود", Toast.LENGTH_SHORT).show()
        })
        scrollContent.addView(divider())

        // Version comes directly from the installed APK and therefore changes automatically per release.
        scrollContent.addView(TextView(this).apply {
            text = "نسخه ${currentVersion()}"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(105, 112, 125))
            setPadding(0, dp(16), 0, dp(8))
        })

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

    /** Renders the neutral prototype home screen. Product apps replace only this visual content. */
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

    /** Settings page shared conceptually by all apps; extra settings can be appended per product. */
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

        // Notifications are a mandatory section of the shared Android app standard.
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

    /** Contact page rendered inside the same activity to keep this prototype lightweight. */
    private fun renderContactPage() {
        secondaryPageOpen = true
        pageHost.removeAllViews()
        pageHost.addView(buildToolbar("ارتباط با ما", showBack = true))

        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(24))
        }

        outer.addView(TextView(this).apply {
            text = "اطلاعات، توضیحات نرم‌افزار و راه‌های ارتباطی اختصاصی هر برنامه در این قسمت قرار می‌گیرد."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(56, 63, 75))
            setPadding(dp(8), dp(24), dp(8), dp(24))
        })

        // Weighted spacer keeps the AS Team identity block above the bottom edge.
        outer.addView(View(this), LinearLayout.LayoutParams(-1, 0, 1f))
        outer.addView(divider())
        outer.addView(TextView(this).apply {
            text = "گروه توسعه فناوری و نرم افزاری as Team"
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(28, 34, 45))
            setPadding(dp(8), dp(24), dp(8), dp(8))
        })
        outer.addView(TextView(this).apply {
            text = "AS.Support.info@Gmail.Com"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(80, 88, 104))
            setPadding(dp(8), 0, dp(8), dp(24))
        })
        outer.addView(View(this), LinearLayout.LayoutParams(-1, dp(72)))
        pageHost.addView(outer, LinearLayout.LayoutParams(-1, 0, 1f))
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

    /** Presents profile-photo actions without requiring storage permissions. */
    private fun showProfileImageSheet() {
        AlertDialog.Builder(this)
            .setTitle("تصویر پروفایل")
            .setItems(arrayOf("انتخاب و برش عکس", "حذف تصویر فعلی")) { _, which ->
                if (which == 0) pickProfileImage() else clearProfileImage()
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /** Uses Android's system document picker; only the chosen image becomes readable by this app. */
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
     * Decodes a reasonably sized bitmap so large camera images do not consume excessive heap memory.
     * The final profile image is only 720px, therefore decoding multi-thousand-pixel photos is unnecessary.
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

    /** Displays the custom crop view with pinch zoom, one-finger drag and explicit save/cancel actions. */
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
            // Override the default positive-button dismissal so a failed crop does not close the editor.
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
            // The source bitmap is only a temporary editor asset; recycle it after dialog closure when safe.
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        dialog.show()
    }

    /** Saves the final cropped photo into private app storage so no external URI is needed afterward. */
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

    /** Removes both profile metadata and the private cropped-image file. */
    private fun clearProfileImage() {
        File(filesDir, profilePhotoFileName).delete()
        prefs.edit().remove("has_profile_photo").apply()
        profileImage.setImageResource(android.R.drawable.ic_menu_camera)
    }

    /** Optional local profile-name editor displayed by tapping the name row. */
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

    /** Reads versionName from the installed APK instead of hard-coding it in the menu. */
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
