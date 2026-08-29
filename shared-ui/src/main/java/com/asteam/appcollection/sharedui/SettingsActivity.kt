package com.asteam.appcollection.sharedui

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

/**
 * Dedicated settings page shared by every numbered application.
 *
 * The screen intentionally stores only cross-project user preferences. Feature-specific settings
 * remain inside their own numbered application so the shared module does not own demo logic.
 */
class SettingsActivity : Activity() {

    /** Username field whose value is displayed in the hamburger-drawer profile block. */
    private lateinit var profileNameInput: EditText

    /** User preference controlling app-generated notifications where a demo supports them. */
    private lateinit var notificationsSwitch: Switch

    /** Android entry point for the dedicated settings Activity. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildScreen())
        loadSavedValues()
    }

    /** Builds a simple RTL settings page using only platform Views. */
    private fun buildScreen(): View {
        // Root page keeps the toolbar fixed while the settings body can scroll independently.
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 249, 252))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Header contains an explicit Back button so navigation is clear even without ActionBar.
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Finish returns to the exact numbered application that opened Settings.
        val backButton = Button(this).apply {
            text = "بازگشت"
            isAllCaps = false
            setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_media_previous, 0, 0, 0)
            compoundDrawablePadding = dp(4)
            setOnClickListener { finish() }
        }

        // Screen title consumes the remaining toolbar width.
        val title = TextView(this).apply {
            text = "تنظیمات"
            textSize = 21f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(Color.rgb(25, 30, 40))
            setPadding(dp(12), 0, dp(12), 0)
        }

        header.addView(backButton, LinearLayout.LayoutParams(dp(112), dp(52)))
        header.addView(title, LinearLayout.LayoutParams(0, dp(52), 1f))
        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // ScrollView keeps the page usable with large accessibility fonts and small screens.
        val bodyScroll = ScrollView(this).apply {
            isFillViewport = true
        }

        // Vertical body groups profile and notification preferences into readable sections.
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(28))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Profile section lets a user choose the text shown below the drawer image.
        body.addView(sectionTitle("پروفایل"))
        body.addView(label("نام نمایشی در منوی برنامه"))
        profileNameInput = EditText(this).apply {
            hint = "کاربر"
            textSize = 16f
            setSingleLine(true)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        body.addView(
            profileNameInput,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Divider keeps the mandatory Notifications section visually distinct.
        body.addView(divider())

        // Notifications section is present in every app as required by the shared Android rules.
        body.addView(sectionTitle("اعلان‌ها"))
        notificationsSwitch = Switch(this).apply {
            text = "اعلان‌های برنامه فعال باشد"
            textSize = 16f
            setPadding(0, dp(8), 0, dp(8))
        }
        body.addView(
            notificationsSwitch,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        body.addView(
            label(
                "این گزینه ترجیح کاربر را ذخیره می‌کند. در نمونه‌هایی که اعلان واقعی دارند، " +
                    "مجوز سیستمی Android نیز هنگام نیاز مدیریت می‌شود."
            )
        )

        // Save button persists both values atomically in the shared preference file.
        val saveButton = Button(this).apply {
            text = "ذخیره تنظیمات"
            isAllCaps = false
            textSize = 16f
            setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0)
            compoundDrawablePadding = dp(8)
            setOnClickListener { saveValues() }
        }
        body.addView(
            saveButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )

        bodyScroll.addView(
            body,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            bodyScroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        return root
    }

    /** Loads current preferences after all editable Views have been created. */
    private fun loadSavedValues() {
        val preferences = getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)

        // A missing profile name is represented as the simple default value "کاربر".
        profileNameInput.setText(
            preferences.getString(AppUiContract.KEY_PROFILE_NAME, "کاربر") ?: "کاربر"
        )

        // Notifications default to enabled until the user explicitly turns them off.
        notificationsSwitch.isChecked = preferences.getBoolean(
            AppUiContract.KEY_NOTIFICATIONS_ENABLED,
            true
        )
    }

    /** Validates and stores the visible settings locally on the current device. */
    private fun saveValues() {
        // Trim whitespace so the drawer never displays a name consisting only of spaces.
        val requestedName = profileNameInput.text.toString().trim()
        val safeName = requestedName.ifBlank { "کاربر" }

        // Store both settings in one editor operation and make them visible immediately.
        getSharedPreferences(AppUiContract.PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putString(AppUiContract.KEY_PROFILE_NAME, safeName)
            .putBoolean(AppUiContract.KEY_NOTIFICATIONS_ENABLED, notificationsSwitch.isChecked)
            .apply()

        // Reflect normalization such as an empty name becoming "کاربر" back in the field.
        profileNameInput.setText(safeName)
        Toast.makeText(this, "تنظیمات ذخیره شد", Toast.LENGTH_SHORT).show()
    }

    /** Creates a bold-looking section heading without requiring external style resources. */
    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 19f
        setTextColor(Color.rgb(25, 30, 40))
        setPadding(0, dp(8), 0, dp(8))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    /** Creates normal explanatory text used throughout the Settings page. */
    private fun label(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(Color.rgb(55, 60, 70))
        setPadding(0, dp(4), 0, dp(8))
    }

    /** Creates a subtle horizontal separator between Settings sections. */
    private fun divider(): View = View(this).apply {
        setBackgroundColor(Color.rgb(220, 223, 230))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(1)
        ).apply {
            topMargin = dp(20)
            bottomMargin = dp(14)
        }
    }

    /** Converts density-independent pixels into physical pixels for consistent spacing. */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
