package com.asteam.appcollection.p11

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Outline
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * Standalone prototype used to validate the shared AS Team right-side hamburger drawer.
 * Every UI section is intentionally documented so this file can later be used as the
 * implementation reference for the common drawer component across all Android apps.
 */
@Suppress("DEPRECATION")
class MainActivity : Activity() {

    // Persistent local profile/settings store. No network account is required for this prototype.
    private val prefs by lazy { getSharedPreferences("as_team_drawer_demo", MODE_PRIVATE) }

    // Root overlay container. The drawer is placed over the normal application page inside it.
    private lateinit var root: FrameLayout

    // Main page container. Contact/settings pages are rendered here without creating extra activities.
    private lateinit var pageHost: LinearLayout

    // Right-side navigation drawer and its semi-transparent background scrim.
    private lateinit var drawer: LinearLayout
    private lateinit var scrim: View

    // Profile widgets are retained so selected data can be refreshed immediately.
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView

    // Tracks whether a secondary page is open so Android Back first returns to the main screen.
    private var secondaryPageOpen = false

    // Request code for Android's document/image picker.
    private val imagePickerRequest = 2101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildRootUi()
        renderHomePage()
    }

    /** Creates the global app shell: page host, dim layer and drawer anchored to the physical right. */
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

        // Scrim visually separates drawer from the page and also closes it when tapped.
        scrim = View(this).apply {
            setBackgroundColor(Color.argb(95, 0, 0, 0))
            visibility = View.GONE
            setOnClickListener { closeDrawer() }
        }
        root.addView(scrim, FrameLayout.LayoutParams(-1, -1))

        drawer = buildDrawer()
        root.addView(
            drawer,
            // Gravity.RIGHT is deliberate: END would become the left edge in an RTL parent.
            FrameLayout.LayoutParams(dp(320), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.RIGHT)
        )

        setContentView(root)
    }

    /** Builds the complete professional drawer requested as the reusable standard for all apps. */
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
            addView(
                profileImage,
                FrameLayout.LayoutParams(dp(112), dp(112), Gravity.CENTER)
            )
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

        // Reserved dynamic area. Each app will later inject its own menu rows here.
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

        // Communication destination is common, while its upper content is app-specific.
        scrollContent.addView(drawerRow("☎", "ارتباط با ما") {
            closeDrawer()
            renderContactPage()
        })

        scrollContent.addView(divider())
        scrollContent.addView(drawerRow("▦", "سایر برنامه‌های ما") {
            Toast.makeText(this, "لینک این بخش بعداً اضافه می‌شود", Toast.LENGTH_SHORT).show()
        })

        scrollContent.addView(divider())

        val version = currentVersion()
        scrollContent.addView(TextView(this).apply {
            text = "نسخه $version"
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

    /** Main prototype screen. Only the hamburger interaction is intentionally emphasized here. */
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
            text = "برای تست، آیکون سه‌خط بالا سمت راست را بزنید."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(90, 98, 112))
            setPadding(0, dp(16), 0, 0)
        })
        pageHost.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    /** Contact page rendered inside the same activity to keep this APK a one-activity prototype. */
    private fun renderContactPage() {
        secondaryPageOpen = true
        pageHost.removeAllViews()
        pageHost.addView(buildToolbar("ارتباط با ما", showBack = true))

        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(22), dp(24), dp(24))
        }

        val appSpecific = TextView(this).apply {
            text = "اطلاعات، توضیحات نرم‌افزار و راه‌های ارتباطی اختصاصی هر برنامه در این قسمت قرار می‌گیرد."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(56, 63, 75))
            setPadding(dp(8), dp(24), dp(8), dp(24))
        }
        outer.addView(appSpecific)

        // Weighted spacer keeps the AS Team identity block around the second sixth from the bottom.
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

        // Small bottom spacer stops the identity block from sticking to the screen edge.
        outer.addView(View(this), LinearLayout.LayoutParams(-1, dp(72)))
        pageHost.addView(outer, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    /** Toolbar shared by prototype pages; hamburger always stays at the upper-right. */
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

    /** A consistent drawer row with a related icon on the right side in RTL layout. */
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

    /** Thin neutral divider used instead of visually heavy separator lines. */
    private fun divider(): View = View(this).apply {
        setBackgroundColor(Color.rgb(229, 233, 240))
        layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply {
            topMargin = dp(6)
            bottomMargin = dp(6)
        }
    }

    /** Opens the right drawer with a simple slide animation. */
    private fun openDrawer() {
        if (drawer.visibility == View.VISIBLE) return
        drawer.visibility = View.VISIBLE
        scrim.visibility = View.VISIBLE
        drawer.translationX = dp(320).toFloat()
        drawer.animate().translationX(0f).setDuration(220).start()
    }

    /** Closes the drawer and removes the page dim layer. */
    private fun closeDrawer() {
        if (drawer.visibility != View.VISIBLE) return
        drawer.animate().translationX(dp(320).toFloat()).setDuration(180).withEndAction {
            drawer.visibility = View.GONE
            scrim.visibility = View.GONE
            drawer.translationX = 0f
        }.start()
    }

    /** Bottom-sheet-like choice dialog for profile image selection/removal. */
    private fun showProfileImageSheet() {
        AlertDialog.Builder(this)
            .setTitle("تصویر پروفایل")
            .setItems(arrayOf("انتخاب از گالری", "حذف تصویر فعلی")) { _, which ->
                if (which == 0) pickProfileImage() else clearProfileImage()
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /** Uses Android's system document picker, avoiding broad storage permissions. */
    private fun pickProfileImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, imagePickerRequest)
    }

    /** Saves the selected image URI persistently so it survives app restarts. */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePickerRequest && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            runCatching {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            prefs.edit().putString("profile_uri", uri.toString()).apply()
            profileImage.setImageURI(uri)
        }
    }

    /** Restores the user's selected profile image when the app is opened again. */
    private fun restoreProfileImage() {
        val saved = prefs.getString("profile_uri", null) ?: return
        runCatching { profileImage.setImageURI(Uri.parse(saved)) }
            .onFailure { profileImage.setImageResource(android.R.drawable.ic_menu_camera) }
    }

    /** Removes the stored profile photo and restores the neutral camera placeholder. */
    private fun clearProfileImage() {
        prefs.edit().remove("profile_uri").apply()
        profileImage.setImageResource(android.R.drawable.ic_menu_camera)
    }

    /** Optional profile-name editor; name is stored locally beside the profile image. */
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

    /** Reads versionName directly from the installed APK so future releases update automatically. */
    private fun currentVersion(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    /** Density helper keeps dimensions visually stable on different Android screen densities. */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** Back closes drawer first, then exits a subpage, and only then leaves the app. */
    override fun onBackPressed() {
        when {
            ::drawer.isInitialized && drawer.visibility == View.VISIBLE -> closeDrawer()
            secondaryPageOpen -> renderHomePage()
            else -> super.onBackPressed()
        }
    }
}
