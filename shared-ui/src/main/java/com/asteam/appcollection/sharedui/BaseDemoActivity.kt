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
 * Common Activity shell used by every numbered application in App-Collection.
 *
 * Keeping navigation, profile handling and small reusable View helpers in one module prevents the
 * same infrastructure from being copied 78 times. Each numbered project only implements the
 * feature-specific content inside [renderDemo].
 *
 * Project-wide UI rules implemented here:
 * - The hamburger button is shown at the top-right of the RTL toolbar.
 * - The drawer opens from the logical END/right side of the screen.
 * - The drawer contains a circular local profile image and a persisted display name.
 * - Settings/About/Contact/About-software open dedicated Activities rather than temporary dialogs.
 * - About-software receives only user-facing title/description/version information.
 * - Back closes an open drawer first; otherwise normal Android back-stack navigation is used.
 *
 * The class also preserves small protected helper methods such as [showInfo], [label], [button]
 * and [input]. Several rebuilt examples use those helpers, so they form part of the shared source
 * API and must not be removed when the drawer implementation is refactored.
 */
abstract class BaseDemoActivity : Activity() {

    /** Human-readable title supplied by the concrete numbered application. */
    protected abstract val demoTitle: String

    /** Short user-facing description supplied by the concrete numbered application. */
    protected abstract val demoDescription: String

    /**
     * Historical source-file reference used only for source traceability/documentation.
     * It is intentionally never exposed in the user-facing About-software page.
     */
    protected abstract val sourceReference: String

    /** Feature-specific controls are inserted into this container by [renderDemo]. */
    private lateinit var demoContainer: LinearLayout

    /** Scroll wrapper representing the visible/hidden right-side drawer. */
    private lateinit var drawerScroll: ScrollView

    /** Vertical content container hosted inside [drawerScroll]. */
    private lateinit var drawerContent: LinearLayout

    /** Circular image refreshed after the user chooses or removes a local profile image. */
    private lateinit var profileImageView: ImageView

    /** Display name refreshed whenever this Activity returns from Settings. */
    private lateinit var profileNameView: TextView

    /** Android lifecycle entry point for every rebuilt demo Activity. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Construct the common shell before rendering feature-specific content.
        setContentView(buildAppShell())

        // Every sample starts with a concise description so its purpose is visible immediately.
        demoContainer.addView(label(demoDescription, 17f))

        // Only the numbered module owns the actual demonstration/tool logic.
        renderDemo(demoContainer)
    }

    /** Each numbered application implements its controls and behavior in this method. */
    protected abstract fun renderDemo(container: LinearLayout)

    /**
     * Returning from Settings may change the display name, so the drawer profile is refreshed on
     * every resume rather than only once during Activity creation.
     */
    override fun onResume() {
        super.onResume()
        if (::profileNameView.isInitialized) {
            refreshProfileBlock()
        }
    }

    /** Builds the toolbar, scrollable feature body and right-side drawer overlay. */
    private fun buildAppShell(): View {
        // FrameLayout lets the drawer overlay the page while preserving the feature screen below it.
        val rootFrame = FrameLayout(this)

        // Main page is RTL and vertically stacks toolbar + scrollable feature content.
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(Color.rgb(248, 249, 252))
        }

        // A lightweight platform-View toolbar keeps all 78 APKs small and dependency-free.
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
        }

        // In RTL order this first control is placed on the visual right side as required.
        val menuButton = Button(this).apply {
            text = "☰"
            textSize = 22f
            isAllCaps = false
            contentDescription = "باز کردن منوی برنامه"
            setOnClickListener { setDrawerVisible(!isDrawerVisible()) }
        }

        // Title consumes the remaining toolbar width.
        val titleView = TextView(this).apply {
            text = demoTitle
            textSize = 20f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.rgb(25, 30, 40))
            setPadding(dp(12), 0, dp(12), 0)
        }

        toolbar.addView(menuButton, LinearLayout.LayoutParams(dp(64), dp(52)))
        toolbar.addView(titleView, LinearLayout.LayoutParams(0, dp(52), 1f))
        page.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Concrete demos receive this container through renderDemo().
        demoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(18), dp(18), dp(18), dp(32))
        }

        // Scrolling prevents sample controls from being clipped on small screens/large font sizes.
        val featureScroll = ScrollView(this).apply {
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
            featureScroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        rootFrame.addView(
            page,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Drawer body is created once and wrapped in a ScrollView for compact phones.
        drawerContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(16), dp(24), dp(16), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        // The profile block is always the first area in the common drawer.
        addProfileBlock()
        drawerContent.addView(divider())

        // Every navigation row has an icon and an explicit action.
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

        // addDrawerItem already closes the drawer before invoking its action, therefore this row's
        // action is intentionally empty. Calling a toggle here would immediately reopen the drawer.
        addDrawerItem("بستن منو", android.R.drawable.ic_menu_close_clear_cancel) { }

        drawerScroll = ScrollView(this).apply {
            visibility = View.GONE
            elevation = dp(16).toFloat()
            setBackgroundColor(Color.WHITE)
            addView(
                drawerContent,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        // Gravity.END maps to the physical right side in this RTL application shell.
        rootFrame.addView(
            drawerScroll,
            FrameLayout.LayoutParams(dp(310), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END)
        )
        return rootFrame
    }

    /** Creates the circular profile image and centered user-name block. */
    private fun addProfileBlock() {
        // The oval background supplies a circular outline for clipToOutline.
        val circularBackground = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.rgb(238, 241, 247))
            setStroke(dp(1), Color.rgb(210, 215, 225))
        }

        // No network/upload permission is required; the selected image remains a local document URI.
        profileImageView = ImageView(this).apply {
            background = circularBackground
            clipToOutline = true
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_menu_myplaces)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            contentDescription = "تصویر پروفایل؛ برای تغییر لمس کنید"
            setOnClickListener { showProfileImageOptions() }
        }
        drawerContent.addView(
            profileImageView,
            LinearLayout.LayoutParams(dp(104), dp(104)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(10)
            }
        )

        // Name and user icon are centered below the profile image.
        profileNameView = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(30, 35, 45))
            setCompoundDrawablesRelativeWithIntrinsicBounds(
                android.R.drawable.ic_menu_myplaces,
                0,
                0,
                0
            )
            compoundDrawablePadding = dp(6)
            setPadding(dp(4), dp(4), dp(4), dp(12))
        }
        drawerContent.addView(
            profileNameView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        refreshProfileBlock()
    }

    /** Reloads the local profile name and optional persisted image URI. */
    private fun refreshProfileBlock() {
        val preferences = getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)

        // An empty/missing preference always has a safe visible default.
        profileNameView.text =
            preferences.getString(AppUiContract.KEY_PROFILE_NAME, "کاربر")?.ifBlank { "کاربر" }
                ?: "کاربر"

        val storedUri = preferences.getString(AppUiContract.KEY_PROFILE_IMAGE_URI, null)
        if (storedUri.isNullOrBlank()) {
            showDefaultProfileImage()
            return
        }

        // setImageURI can fail if the external document was moved/revoked; recover automatically.
        val loaded = runCatching {
            profileImageView.setPadding(0, 0, 0, 0)
            profileImageView.setImageURI(Uri.parse(storedUri))
            profileImageView.drawable != null
        }.getOrDefault(false)

        if (!loaded) {
            preferences.edit().remove(AppUiContract.KEY_PROFILE_IMAGE_URI).apply()
            showDefaultProfileImage()
        }
    }

    /** Restores the built-in neutral profile icon after removal or inaccessible URI recovery. */
    private fun showDefaultProfileImage() {
        profileImageView.setImageResource(android.R.drawable.ic_menu_myplaces)
        profileImageView.setPadding(dp(18), dp(18), dp(18), dp(18))
    }

    /** Shows the one-tap profile-image chooser/removal popup required by the drawer specification. */
    private fun showProfileImageOptions() {
        AlertDialog.Builder(this)
            .setTitle("تصویر پروفایل")
            .setItems(arrayOf("انتخاب تصویر از دستگاه", "حذف تصویر پروفایل")) { _, index ->
                when (index) {
                    0 -> openProfileImagePicker()
                    1 -> removeProfileImage()
                }
            }
            .setNegativeButton("انصراف", null)
            .show()
    }

    /** Opens Android's document picker and requests persistent read access to the chosen image. */
    private fun openProfileImagePicker() {
        val pickerIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        @Suppress("DEPRECATION")
        startActivityForResult(pickerIntent, AppUiContract.REQUEST_PROFILE_IMAGE)
    }

    /** Removes only the saved URI reference; the user's original image file is never deleted. */
    private fun removeProfileImage() {
        getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .remove(AppUiContract.KEY_PROFILE_IMAGE_URI)
            .apply()
        refreshProfileBlock()
    }

    /** Receives the profile document picker result while preserving results used by demo Activities. */
    @Deprecated("Platform Activity result API is retained to keep shared-ui dependency-light")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Ignore results that belong to camera/ringtone/gallery/etc. feature samples.
        if (requestCode != AppUiContract.REQUEST_PROFILE_IMAGE || resultCode != RESULT_OK) {
            return
        }

        val selectedUri = data?.data ?: return

        // Persist only permissions actually returned by the document provider.
        val takeFlags = data.flags and
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (takeFlags != 0) {
            runCatching {
                contentResolver.takePersistableUriPermission(selectedUri, takeFlags)
            }
        }

        getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putString(AppUiContract.KEY_PROFILE_IMAGE_URI, selectedUri.toString())
            .apply()
        refreshProfileBlock()
    }

    /** Adds one icon-bearing navigation/action row to the drawer. */
    private fun addDrawerItem(text: String, iconRes: Int, action: () -> Unit) {
        val item = Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
            compoundDrawablePadding = dp(10)
            setOnClickListener {
                // Navigation starts from a closed drawer so Back never reveals stale overlay state.
                setDrawerVisible(false)
                action()
            }
        }
        drawerContent.addView(
            item,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)).apply {
                bottomMargin = dp(4)
            }
        )
    }

    /** Creates a subtle separator between profile and navigation content. */
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

    /** Returns true only while the right-side drawer overlay is visible. */
    private fun isDrawerVisible(): Boolean =
        ::drawerScroll.isInitialized && drawerScroll.visibility == View.VISIBLE

    /** Centralizes drawer visibility changes for toolbar, menu rows and Back handling. */
    private fun setDrawerVisible(visible: Boolean) {
        if (::drawerScroll.isInitialized) {
            drawerScroll.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    /** Opens the dedicated shared Settings destination. */
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /** Opens Android's Sharesheet without assuming a store URL that may not exist yet. */
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

    /** Opens one of the dedicated About/Contact/Software pages with presentation-only data. */
    private fun openInfoPage(pageType: String) {
        startActivity(
            Intent(this, InfoPageActivity::class.java).apply {
                putExtra(AppUiContract.EXTRA_PAGE_TYPE, pageType)
                putExtra(AppUiContract.EXTRA_APP_TITLE, demoTitle)
                putExtra(AppUiContract.EXTRA_APP_DESCRIPTION, demoDescription)
                putExtra(AppUiContract.EXTRA_APP_VERSION, installedVersionName())
            }
        )
    }

    /** Reads only the installed versionName required by the About-software screen. */
    private fun installedVersionName(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    /**
     * Shows concise feature feedback for rebuilt examples.
     *
     * This protected helper existed in the original shared shell and is used by multiple numbered
     * modules for validation messages, selected-list values and unavailable-device capabilities.
     * It is intentionally retained as part of the shared API; removing it would break those demos.
     */
    protected fun showInfo(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("باشه", null)
            .show()
    }

    /** Creates a reusable TextView for explanations and feature output. */
    protected fun label(text: String = "", sizeSp: Float = 16f): TextView = TextView(this).apply {
        this.text = text
        textSize = sizeSp
        setTextColor(Color.rgb(35, 40, 50))
        setPadding(dp(6), dp(8), dp(6), dp(8))
        layoutDirection = View.LAYOUT_DIRECTION_RTL
    }

    /** Creates a reusable action button for feature-specific demo code. */
    protected fun button(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = 16f
        isAllCaps = false
        setOnClickListener { onClick() }
    }

    /** Creates a reusable text input with a visible hint. */
    protected fun input(hint: String): EditText = EditText(this).apply {
        this.hint = hint
        textSize = 16f
        setPadding(dp(10), dp(10), dp(10), dp(10))
    }

    /** Converts density-independent pixels into physical pixels for consistent spacing. */
    protected fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** Back closes the drawer first and otherwise returns to the previous Android destination. */
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (isDrawerVisible()) {
            setDrawerVisible(false)
        } else {
            super.onBackPressed()
        }
    }
}
