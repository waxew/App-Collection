package com.asteam.appcollection.sharedui

/**
 * Central contract for navigation extras and local preference keys used by the shared app shell.
 *
 * Keeping these strings in one object prevents subtle bugs where one Activity writes a preference
 * or Intent extra using a different spelling than the Activity that later reads it.
 */
object AppUiContract {

    /** Name of the SharedPreferences file used only for common drawer/settings values. */
    const val PREFERENCES_NAME = "as_team_app_settings"

    /** Boolean preference controlling whether user-facing notifications are enabled. */
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    /** User-editable display name shown below the drawer profile image. */
    const val KEY_PROFILE_NAME = "profile_name"

    /** Persisted content URI pointing to the locally selected profile image. */
    const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"

    /** Request code reserved for the platform document picker used by the profile image. */
    const val REQUEST_PROFILE_IMAGE = 9101

    /** Intent extra telling InfoPageActivity which information page to render. */
    const val EXTRA_PAGE_TYPE = "extra_page_type"

    /** Intent extra containing the current demo's user-facing title. */
    const val EXTRA_APP_TITLE = "extra_app_title"

    /** Intent extra containing the current demo's short user-facing description. */
    const val EXTRA_APP_DESCRIPTION = "extra_app_description"

    /** Intent extra containing the currently installed APK version name. */
    const val EXTRA_APP_VERSION = "extra_app_version"

    /** Page identifier for developer/team information. */
    const val PAGE_ABOUT_TEAM = "about_team"

    /** Page identifier for support contact information. */
    const val PAGE_CONTACT = "contact"

    /** Page identifier for the current app's description and version. */
    const val PAGE_ABOUT_SOFTWARE = "about_software"
}
