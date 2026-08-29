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
 * This class owns only infrastructure that is genuinely shared by all 78 rebuilt examples:
 * toolbar/drawer navigation, local profile preferences, dedicated information/settings pages and
 * a handful of small View/helper APIs. Keeping those concerns here avoids copying the same code
 * into 78 application modules while leaving each module responsible for its actual feature logic.
 *
 * Permanent UI rules implemented here:
 * - The hamburger control is displayed at the top-right of the RTL toolbar.
 * - The drawer overlays the screen from the logical END/right edge.
 * - A circular profile image and display name appear at the top of the drawer.
 * - Tapping the image lets the user select or remove a local image.
 * - Settings, About us, Contact us and About software are dedicated Activities.
 * - About software receives only user-facing title/description/version data.
 * - Back closes an open drawer before following Android's normal Activity back stack.
 *
 * Protected helpers such as [showInfo], [label], [button], [input] and [dp] are part of the shared
 * source API. Numbered modules call them directly, so a future refactor must preserve compatible
 * signatures or update every caller in the same commit.
 */
abstract class BaseDemoActivity : Activity() {

    /** Human-readable title supplied by the concrete numbered application. */
    protected abstract val demoTitle: String

    /** Short user-facing description supplied by the concrete numbered application. */
    protected abstract val demoDescription: String

    /**
     * Historical original-file reference retained only for source traceability/documentation.
     * This value is intentionally never shown in the user-facing About-software page.
     */
    protected abstract val sourceReference: String

    /** Container into which the concrete module renders its feature-specific controls. */
    private lateinit var demoContainer: LinearLayout

    /** Scroll wrapper whose visibility represents the open/closed drawer state. */
    private lateinit var drawerScroll: ScrollView

    /** Vertical list containing profile content and common navigation entries. */
    private lateinit var drawerContent: LinearLayout

    /** Circular profile ImageView refreshed after local image selection/removal. */
    private lateinit var profileImageView: ImageView

    /** Drawer display-name TextView refreshed after returning from Settings. */
    private lateinit var profileNameView: TextView

    /** Android lifecycle entry point for every rebuilt demo Activity. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Construct the reusable page shell before feature-specific widgets are added.
        setContentView(buildAppShell())

        // Keep each sample self-describing by placing its concise explanation first.
        demoContainer.addView(label(demoDescription, 17f))

        // Delegate only the unique educational/tool logic to the numbered application.
        renderDemo(demoContainer)
    }

    /** Each numbered application implements its controls and behavior inside this container. */
    protected abstract fun renderDemo(container: LinearLayout)

    /** Refresh shared profile state when returning from Settings or the document picker. */
    override fun onResume() {
        super.onResume()
        if (::profileNameView.isInitialized) {
            refreshProfileBlock()
        }
    }

    /** Builds the toolbar, scrollable feature page and right-side drawer overlay. */
    private fun buildAppShell(): View {
        // FrameLayout allows the drawer to overlay feature content instead of replacing it.
        val rootFrame = FrameLayout(this)

        // Main page vertically stacks the toolbar and scrollable demo content.
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(Color.rgb(248, 249, 252))
        }

        // Platform Views keep these small educational APKs dependency-light.
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
        }

        // This first item in RTL order is rendered at the visual right side of the toolbar.
        val menuButton = Button(this).apply {
            text = "☰"
            textSize = 22f
            isAllCaps = false
            contentDescription = "باز کردن منوی برنامه"
            setOnClickListener { setDrawerVisible(!isDrawerVisible()) }
        }

        // The current demo title fills the remaining toolbar width.
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

        // Numbered modules populate this container in renderDemo().
        demoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(18), dp(18), dp(18), dp(32))
        }

        // Scrolling prevents controls from being clipped on compact devices or large font scales.
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

        // Drawer body is created once and kept independently scrollable on short displays.
        drawerContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(dp(16), dp(24), dp(16), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        // The project-wide profile block always appears before navigation entries.
        addProfileBlock()
        drawerContent.addView(divider())

        // Every common navigation/action row includes an icon as required by the UI contract.
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

        // addDrawerItem closes the drawer before invoking the action, so no second toggle is needed.
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

        // Gravity.END maps this RTL drawer to the physical right side of the screen.
        rootFrame.addView(
            drawerScroll,
            FrameLayout.LayoutParams(dp(310), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.END)
        )
        return rootFrame
    }

    /** Creates the centered circular profile image and user-name block. */
    private fun addProfileBlock() {
        // Oval drawable supplies the circular background/outline used by clipToOutline.
        val circularBackground = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.rgb(238, 241, 247))
            setStroke(dp(1), Color.rgb(210, 215, 225))
        }

        // Profile images are local document references; no upload/network permission is required.
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

        // The user icon and persisted display name sit directly beneath the circular image.
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

    /** Reloads the locally persisted profile name and optional document URI. */
    private fun refreshProfileBlock() {
        val preferences = getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)

        // Missing or whitespace-only names have a stable visible default.
        profileNameView.text =
            preferences.getString(AppUiContract.KEY_PROFILE_NAME, "کاربر")?.ifBlank { "کاربر" }
                ?: "کاربر"

        val storedUri = preferences.getString(AppUiContract.KEY_PROFILE_IMAGE_URI, null)
        if (storedUri.isNullOrBlank()) {
            showDefaultProfileImage()
            return
        }

        // External documents can later move/be revoked; recover instead of leaving an empty image.
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

    /** Restores the built-in user icon after removal or inaccessible document recovery. */
    private fun showDefaultProfileImage() {
        profileImageView.setImageResource(android.R.drawable.ic_menu_myplaces)
        profileImageView.setPadding(dp(18), dp(18), dp(18), dp(18))
    }

    /** Shows the required one-tap popup for choosing or removing the profile image. */
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

    /** Opens Android's document picker and requests persistable access to the selected image. */
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

    /** Clears only the stored URI; the user's original image file is never deleted. */
    private fun removeProfileImage() {
        getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .remove(AppUiContract.KEY_PROFILE_IMAGE_URI)
            .apply()
        refreshProfileBlock()
    }

    /** Receives the shared profile picker while leaving feature-specific Activity results untouched. */
    @Deprecated("Platform result callback is retained to keep shared-ui dependency-light")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Camera/ringtone/gallery and other demo results use different request codes and pass through.
        if (requestCode != AppUiContract.REQUEST_PROFILE_IMAGE || resultCode != RESULT_OK) {
            return
        }

        val selectedUri = data?.data ?: return

        // Persist only grant bits actually returned by the DocumentsProvider.
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

    /** Adds one icon-bearing clickable row to the common drawer. */
    private fun addDrawerItem(text: String, iconRes: Int, action: () -> Unit) {
        val item = Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
            compoundDrawablePadding = dp(10)
            setOnClickListener {
                // Always navigate from a closed overlay so Back does not reveal stale drawer state.
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

    /** Creates the subtle divider between profile content and navigation rows. */
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

    /** Centralizes drawer state changes for the toolbar, menu entries and Back handling. */
    private fun setDrawerVisible(visible: Boolean) {
        if (::drawerScroll.isInitialized) {
            drawerScroll.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    /** Opens the dedicated shared Settings screen. */
    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /** Opens Android Sharesheet without hard-coding a store link that may not exist yet. */
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

    /** Opens a dedicated About/Contact/Software destination with presentation-only values. */
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

    /** Reads only versionName because internal package/application identifiers are not displayed. */
    private fun installedVersionName(): String = runCatching {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    }.getOrDefault("1.0.0")

    /**
     * Shows feature feedback when a caller needs only a message.
     *
     * This one-argument form is kept for compatibility with simple demos. It delegates to the
     * two-argument form so dialog styling/behavior has one implementation.
     */
    protected fun showInfo(message: String) {
        showInfo(demoTitle, message)
    }

    /**
     * Shows feature feedback with an explicit dialog title and message.
     *
     * Older rebuilt modules such as GPS, WebView, email and list examples call this two-argument
     * form. Keeping both overloads prevents a shared-shell refactor from breaking those modules.
     */
    protected fun showInfo(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
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

    /** Back closes the drawer first; otherwise Android returns to the previous destination. */
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (isDrawerVisible()) {
            setDrawerVisible(false)
        } else {
            super.onBackPressed()
        }
    }
}
