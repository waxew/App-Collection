package com.asteam.appcollection.sharedui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Base screen shared by all 78 rebuilt applications.
 *
 * The class deliberately uses Android platform Views only. That keeps every tiny educational APK
 * small while still giving all modules one consistent shell. Any cross-project navigation or UI
 * rule is implemented here once instead of being copied into 78 separate projects.
 *
 * Shared shell contract:
 * - Hamburger control appears at the top-right.
 * - The drawer opens as a real right-side overlay.
 * - A small profile block is shown at the top of the drawer.
 * - Tapping the profile image lets the user choose or remove a locally stored image.
 * - Settings, About us, Contact us and About software open dedicated Activities.
 * - Share uses the Android system Sharesheet.
 * - About software contains only user-facing app text and the installed version; no package name,
 *   applicationId, source filename or other internal identifier is exposed there.
 * - Back closes the drawer first and otherwise follows normal Android back-stack behavior.
 */
abstract class BaseDemoActivity : Activity() {

    /** Human-readable screen title supplied by each numbered application module. */
    protected abstract val demoTitle: String

    /** Short user-facing explanation supplied by each numbered application module. */
    protected abstract val demoDescription: String

    /**
     * Historical source reference retained only inside source/documentation for traceability.
     * It is intentionally NOT displayed in the About software screen.
     */
    protected abstract val sourceReference: String

    /** Right-side drawer overlay created after Activity.onCreate starts. */
    private lateinit var drawer: LinearLayout

    /** Feature content container populated by the concrete numbered demo. */
    private lateinit var demoContainer: LinearLayout

    /** Drawer profile image refreshed after the user selects or removes a local picture. */
    private lateinit var profileImageView: ImageView

    /** Drawer username text refreshed when SettingsActivity changes the stored display name. */
    private lateinit var profileNameView: TextView

    /** Called by Android when the concrete Activity is first created. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Build the common toolbar/content/drawer shell before the module renders its own controls.
        setContentView(buildAppShell())

        // Put a concise explanation at the beginning of every demo so the sample is self-describing.
        demoContainer.addView(label(demoDescription, 17f))

        // Delegate only the actual educational feature to the numbered module implementation.
        renderDemo(demoContainer)
    }

    /** Each numbered application inserts its own interactive controls into this container. */
    protected abstract fun renderDemo(container: LinearLayout)

    /**
     * Refresh profile data whenever the Activity becomes visible again.
     * This is important after returning from the dedicated Settings page.
     */
    override fun onResume() {
        super.onResume()
        if (::profileNameView.isInitialized) {
            refreshProfileBlock()
        }
    }

    /** Builds the toolbar, scrollable feature content and right-side drawer overlay. */
    private fun buildAppShell(): View {
        // FrameLayout allows the drawer to sit above the page instead of replacing its content.
        val frame = FrameLayout(this)

        // Main vertical page: toolbar on top and scrollable demo content below it.
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 249, 252))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Compact top bar shared by every numbered application.
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Hamburger button is placed first in RTL order, therefore it appears on the right edge.
        val menuButton = Button(this).apply {
            text = "☰"
            textSize = 22f
            isAllCaps = false
            contentDescription = "باز کردن نوار همبرگری"
            setOnClickListener { toggleDrawer() }
        }

        // App title consumes all remaining toolbar width.
        val title = TextView(this).apply {
            text = demoTitle
            textSize = 20f
            setTextColor(Color.rgb(25, 30, 40))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }

        toolbar.addView(menuButton, LinearLayout.LayoutParams(dp(64), dp(52)))
        toolbar.addView(title, LinearLayout.LayoutParams(0, dp(52), 1f))
        page.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Feature-specific controls are inserted into this vertical container by renderDemo().
        demoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(32))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Scrolling prevents controls from being cut off on compact phones or large accessibility text.
        val contentScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(
                demoContainer,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
        page.addView(
            contentScroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        frame.addView(
            page,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // A ScrollView around the drawer keeps all menu entries reachable on small displays.
        val drawerScroll = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
            elevation = dp(16).toFloat()
        }

        // The actual drawer content is a vertical list anchored inside the drawer ScrollView.
        drawer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(24), dp(16), dp(24))
            setBackgroundColor(Color.WHITE)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Profile block follows the project-wide drawer convention.
        addProfileBlock()

        // A compact divider visually separates profile content from navigation entries.
        drawer.addView(divider())

        // Every navigation item has a platform icon and opens the required destination/action.
        addDrawerItem("تنظیمات", android.R.drawable.ic_menu_preferences) { openSettings() }
        addDrawerItem("معرفی به دوستان", android.R.drawable.ic_menu_share) { shareApp() }
        addDrawerItem("درباره ما", android.R.drawable.ic_menu_info_details) {
            openInfoPage(AppUiContract.PAGE_ABOUT_TEAM)
        }
        addDrawerItem("تماس با ما", android.R.drawable.ic_dialog_email) {
            openInfoPage(AppUiContract.PAGE_CONTACT)
        }
        addDrawerItem("درباره نرم افزار", android.R.drawable.ic_menu_help) {
            openInfoPage(AppUiContract.PAGE_ABOUT_SOFTWARE)
        }
        addDrawerItem("بستن منو", android.R.drawable.ic_menu_close_clear_cancel) { toggleDrawer() }

        // Put the vertical drawer inside its scrolling wrapper.
        drawerScroll.addView(
            drawer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Gravity.END keeps the overlay on the logical right side of the screen.
        val drawerParams = FrameLayout.LayoutParams(
            dp(310),
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.END
        )
        frame.addView(drawerScroll, drawerParams)

        // Store the wrapper's visibility inside drawer tag so toggleDrawer() can control the overlay.
        drawer.tag = drawerScroll
        return frame
    }

    /** Creates the circular profile image and centered username area shown above drawer items. */
    private fun addProfileBlock() {
        // Oval background supplies a circular outline used by clipToOutline on supported Android APIs.
        val circularBackground = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.rgb(238, 241, 247))
            setStroke(dp(1), Color.rgb(210, 215, 225))
        }

        // Image is kept local to the device; no upload/network permission is involved.
        profileImageView = ImageView(this).apply {
            background = circularBackground
            clipToOutline = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_menu_myplaces)
            contentDescription = "تصویر پروفایل؛ برای تغییر لمس کنید"
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setOnClickListener { showProfileImageOptions() }
        }

        // Center the circular image independently from RTL/LTR layout direction.
        drawer.addView(
            profileImageView,
            LinearLayout.LayoutParams(dp(104), dp(104)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(10)
            }
        )

        // Username is stored per application because every numbered APK has independent preferences.
        profileNameView = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.rgb(30, 35, 45))
            gravity = Gravity.CENTER
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                android.R.drawable.ic_menu_myplaces,
                0,
                0,
                0
            )
            compoundDrawablePadding = dp(6)
            setPadding(dp(4), dp(4), dp(4), dp(12))
        }
        drawer.addView(
            profileNameView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Populate the two profile widgets from SharedPreferences immediately.
        refreshProfileBlock()
    }

    /** Reloads username and optional persisted profile-image URI from local SharedPreferences. */
    private fun refreshProfileBlock() {
        val preferences = getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)

        // Default username keeps the profile block usable before the user changes Settings.
        profileNameView.text = preferences.getString(AppUiContract.KEY_PROFILE_NAME, "کاربر") ?: "کاربر"

        // Restore the persisted document URI when one exists; fall back to the platform user icon.
        val storedUri = preferences.getString(AppUiContract.KEY_PROFILE_IMAGE_URI, null)
        if (storedUri.isNullOrBlank()) {
            profileImageView.setImageResource(android.R.drawable.ic_menu_myplaces)
            profileImageView.setPadding(dp(18), dp(18), dp(18), dp(18))
        } else {
            val uri = Uri.parse(storedUri)
            val loaded = runCatching {
                profileImageView.setPadding(0, 0, 0, 0)
                profileImageView.setImageURI(uri)
                true
            }.getOrDefault(false)

            // Broken/removed external documents must not leave an empty profile image.
            if (!loaded || profileImageView.drawable == null) {
                preferences.edit().remove(AppUiContract.KEY_PROFILE_IMAGE_URI).apply()
                profileImageView.setImageResource(android.R.drawable.ic_menu_myplaces)
                profileImageView.setPadding(dp(18), dp(18), dp(18), dp(18))
            }
        }
    }

    /** Shows the required one-tap popup for selecting or removing a profile image. */
    private fun showProfileImageOptions() {
        val choices = arrayOf("انتخاب تصویر از دستگاه", "حذف تصویر پروفایل")
        AlertDialog.Builder(this)
            .setTitle("تصویر پروفایل")
            .setItems(choices) { _, which ->
                when (which) {
                    0 -> openProfileImagePicker()
                    1 -> removeProfileImage()
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /** Opens Android's document picker so the selected image can be referenced persistently. */
    private fun openProfileImagePicker() {
        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(pickerIntent, AppUiContract.REQUEST_PROFILE_IMAGE)
    }

    /** Clears only the locally saved profile-image reference; the original user file is untouched. */
    private fun removeProfileImage() {
        getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .remove(AppUiContract.KEY_PROFILE_IMAGE_URI)
            .apply()
        refreshProfileBlock()
    }

    /** Receives the document-picker result and persists long-term read access to the chosen image. */
    @Deprecated("Used for platform-only compatibility; the project intentionally avoids extra Activity dependencies")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Ignore unrelated Activity results used by feature demos.
        if (requestCode != AppUiContract.REQUEST_PROFILE_IMAGE || resultCode != RESULT_OK) {
            return
        }

        // A successful picker result should contain exactly one selected document URI.
        val selectedUri = data?.data ?: return

        // Persist read permission when the DocumentsProvider supports it.
        runCatching {
            contentResolver.takePersistableUriPermission(
                selectedUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        // Store the URI as text so it survives application process restarts.
        getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putString(AppUiContract.KEY_PROFILE_IMAGE_URI, selectedUri.toString())
            .apply()
        refreshProfileBlock()
    }

    /** Adds one icon-bearing clickable row to the right-side drawer. */
    private fun addDrawerItem(text: String, iconRes: Int, action: () -> Unit) {
        val item = Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
            compoundDrawablePadding = dp(10)
            setOnClickListener {
                // Hide the overlay before navigation so returning with Back never shows stale UI.
                setDrawerVisible(false)
                action()
            }
        }
        drawer.addView(
            item,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
                bottomMargin = dp(4)
            }
        )
    }

    /** Builds a one-pixel-style divider using density-aware dimensions. */
    private fun divider(): View = View(this).apply {
        setBackgroundColor(Color.rgb(222, 225, 232))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(1)
        ).apply {
            topMargin = dp(6)
            bottomMargin = dp(12)
        }
    }

    /** Returns the drawer ScrollView wrapper stored during shell construction. */
    private fun drawerWrapper(): ScrollView = drawer.tag as ScrollView

    /** Shows or hides the right-side drawer overlay. */
    private fun toggleDrawer() {
        val wrapper = drawerWrapper()
        setDrawerVisible(wrapper.visibility != View.VISIBLE)
    }

    /** Applies drawer visibility in one place so Back and click navigation behave consistently. */
    private fun setDrawerVisible(visible: Boolean) {
        drawerWrapper().visibility = if (visible) View.VISIBLE else View.GONE
    }

    /** Opens the dedicated Settings screen rather than showing an inline dialog. */
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /** Uses Android Sharesheet to introduce/share the current app without hard-coding a store URL. */
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, demoTitle)
            putExtra(
                Intent.EXTRA_TEXT,
                "$demoTitle - ساخته شده توسط گروه توسعه و برنامه نویسی AS Team"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "معرفی به دوستان"))
    }

    /** Opens one of the dedicated user-facing information pages and supplies only display data. */
    private fun openInfoPage(pageType: String) {
        val destination = Intent(this, InfoPageActivity::class.java).apply {
            putExtra(AppUiContract.EXTRA_PAGE_TYPE, pageType)
            putExtra(AppUiContract.EXTRA_APP_TITLE, demoTitle)
            putExtra(AppUiContract.EXTRA_APP_DESCRIPTION, demoDescription)
            putExtra(AppUiContract.EXTRA_APP_VERSION, installedVersionName())
        }
        startActivity(destination)
    }

    /** Reads the installed APK version name without exposing package/application identifiers. */
    private fun installedVersionName(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    /** Creates a reusable TextView for explanations and feature output. */
    protected fun label(text: String = "", sizeSp: Float = 16f): TextView = TextView(this).apply {
        this.text = text
        textSize = sizeSp
        setTextColor(Color.rgb(35, 40, 50))
        setPadding(dp(6), dp(8), dp(6), dp(8))
        layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    /** Creates a reusable action button for numbered demo implementations. */
    protected fun button(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = 16f
        isAllCaps = false
        setOnClickListener { onClick() }
    }

    /** Creates a reusable EditText with a visible hint for numbered demo implementations. */
    protected fun input(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 16f
        setPadding(dp(10), dp(10), dp(10), dp(10))
    }

    /** Converts density-independent pixels into physical screen pixels. */
    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** Back closes the drawer first; otherwise Android returns to the previous Activity normally. */
    @Deprecated("Android calls this legacy override on the platform Activity used by these demos")
    override fun onBackPressed() {
        val drawerIsVisible = ::drawer.isInitialized && drawerWrapper().visibility == View.VISIBLE
        if (drawerIsVisible) {
            setDrawerVisible(false)
        } else {
            super.onBackPressed()
        }
    }
}
