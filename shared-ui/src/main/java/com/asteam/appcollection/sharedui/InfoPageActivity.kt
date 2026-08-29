package com.asteam.appcollection.sharedui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Dedicated information page used for About us, Contact us and About software.
 *
 * The page type and app-facing text are passed through AppUiContract Intent extras. Keeping these
 * screens in the shared library guarantees that all 78 applications follow the same wording and
 * layout without copying implementation code into every module.
 */
class InfoPageActivity : Activity() {

    /** Android entry point that builds the requested information page. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildScreen())
    }

    /** Builds a fixed toolbar plus a centered scrollable information body. */
    private fun buildScreen(): View {
        // Root page keeps the dedicated destination visually consistent with the main app shell.
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 249, 252))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Determine the visible page title before building the header.
        val pageType = intent.getStringExtra(AppUiContract.EXTRA_PAGE_TYPE)
            ?: AppUiContract.PAGE_ABOUT_SOFTWARE
        val toolbarTitle = when (pageType) {
            AppUiContract.PAGE_ABOUT_TEAM -> "درباره ما"
            AppUiContract.PAGE_CONTACT -> "تماس با ما"
            else -> "درباره نرم افزار"
        }

        // Header includes an explicit Back control because these Activities intentionally use NoActionBar.
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // finish() restores the exact previous numbered app via Android's normal back stack.
        val backButton = Button(this).apply {
            text = "بازگشت"
            isAllCaps = false
            setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_media_previous, 0, 0, 0)
            compoundDrawablePadding = dp(4)
            setOnClickListener { finish() }
        }

        // Visible title names the selected drawer destination.
        val headerTitle = TextView(this).apply {
            text = toolbarTitle
            textSize = 21f
            setTextColor(Color.rgb(25, 30, 40))
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }
        header.addView(backButton, LinearLayout.LayoutParams(dp(112), dp(52)))
        header.addView(headerTitle, LinearLayout.LayoutParams(0, dp(52), 1f))
        root.addView(
            header,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // ScrollView prevents accessibility text scaling from clipping the informational content.
        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        // Centered vertical body follows the user's requested About/Contact page alignment.
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(42), dp(24), dp(42))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        // Dispatch only to known local page renderers; no remote/web content is loaded here.
        when (pageType) {
            AppUiContract.PAGE_ABOUT_TEAM -> renderAboutTeam(body)
            AppUiContract.PAGE_CONTACT -> renderContact(body)
            else -> renderAboutSoftware(body)
        }

        scroll.addView(
            body,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            scroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
        )
        return root
    }

    /** Renders the fixed developer-group information required by the project-wide drawer rules. */
    private fun renderAboutTeam(container: LinearLayout) {
        container.addView(
            centeredText(
                text = "گروه توسعه و برنامه نویسی AS Team",
                sizeSp = 20f,
                bold = true
            )
        )
        container.addView(spacer(dp(18)))
        container.addView(
            centeredText(
                text = "تمامی حقوق مربوط به این برنامه انحصاری میباشد",
                sizeSp = 16f
            )
        )
    }

    /** Renders centered support contact information and a direct email action. */
    private fun renderContact(container: LinearLayout) {
        container.addView(
            centeredText(
                text = "گروه توسعه و برنامه نویسی AS Team",
                sizeSp = 20f,
                bold = true
            )
        )
        container.addView(spacer(dp(24)))
        container.addView(centeredText("ایمیل پشتیبانی", 16f, true))
        container.addView(centeredText(SUPPORT_EMAIL, 16f))

        // ACTION_SENDTO with mailto limits the chooser to apps that can actually handle email.
        val emailButton = Button(this).apply {
            text = "ارسال ایمیل"
            isAllCaps = false
            setCompoundDrawablesRelativeWithIntrinsicBounds(android.R.drawable.ic_dialog_email, 0, 0, 0)
            compoundDrawablePadding = dp(8)
            setOnClickListener {
                val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:$SUPPORT_EMAIL")
                }
                runCatching { startActivity(mailIntent) }
            }
        }
        container.addView(
            emailButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(24)
            }
        )
    }

    /**
     * Renders only the user-facing app description and version.
     * Package name, applicationId, source filename and other technical identifiers are omitted.
     */
    private fun renderAboutSoftware(container: LinearLayout) {
        // Extras are presentation-only and originate from the current numbered BaseDemoActivity.
        val appTitle = intent.getStringExtra(AppUiContract.EXTRA_APP_TITLE).orEmpty()
        val description = intent.getStringExtra(AppUiContract.EXTRA_APP_DESCRIPTION).orEmpty()
        val version = intent.getStringExtra(AppUiContract.EXTRA_APP_VERSION).orEmpty().ifBlank { "1.0.0" }

        // The title is useful to the user but contains no internal Android package information.
        if (appTitle.isNotBlank()) {
            container.addView(centeredText(appTitle, 20f, true))
            container.addView(spacer(dp(16)))
        }

        // Description is intentionally concise because these are small focused applications.
        container.addView(
            centeredText(
                text = description.ifBlank { "برنامه کاربردی اندروید از مجموعه AS Team" },
                sizeSp = 16f
            )
        )
        container.addView(spacer(dp(20)))
        container.addView(centeredText("نسخه برنامه: $version", 16f, true))
    }

    /** Creates a centered TextView with optional bold emphasis. */
    private fun centeredText(text: String, sizeSp: Float, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = sizeSp
            setTextColor(Color.rgb(35, 40, 50))
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(5), dp(4), dp(5))
            if (bold) {
                setTypeface(typeface, Typeface.BOLD)
            }
        }

    /** Creates a transparent fixed-height spacer between centered content blocks. */
    private fun spacer(heightPx: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, heightPx)
    }

    /** Converts density-independent pixels to physical pixels for consistent spacing. */
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    /** Support address centralized here so Contact page text and email action cannot diverge. */
    private companion object {
        const val SUPPORT_EMAIL = "as.team.support@gmail.com"
    }
}
