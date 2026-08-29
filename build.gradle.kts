// Android Gradle Plugin is declared once and reused by all 78 application modules.
// AGP 9.3 includes built-in Kotlin support, so a separate Kotlin Android plugin is not required.
plugins {
    id("com.android.application") version "9.3.0" apply false
    id("com.android.library") version "9.3.0" apply false
}

/**
 * Shared release-version rule for every Android application in this repository.
 *
 * Each numbered module intentionally keeps readable local defaults such as versionCode=1 and
 * versionName=1.0.0. CI passes -PreleaseVersionCode and -PreleaseVersionName when producing a new
 * distributable build.
 *
 * AGP 9 locks the Android DSL before Gradle's projectsEvaluated callback. Changing defaultConfig
 * there produces AgpDslLockedException ("It is too late to set versionCode"). The supported
 * lifecycle hook is androidComponents.finalizeDsl: AGP invokes it after the module build script has
 * finished assigning its local defaults but before that DSL becomes immutable. This gives CI the
 * final authoritative package version without editing 78 module files for every release.
 */
subprojects {
    // This callback runs only for installable Android application modules, not for shared-ui.
    plugins.withId("com.android.application") {
        // Resolve optional release properties from the command line/CI for this Gradle invocation.
        val releaseVersionCode = providers
            .gradleProperty("releaseVersionCode")
            .orNull
            ?.toIntOrNull()
            ?.takeIf { it > 0 }

        val releaseVersionName = providers
            .gradleProperty("releaseVersionName")
            .orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        // ApplicationAndroidComponentsExtension exposes lifecycle-safe AGP configuration hooks.
        extensions.configure<com.android.build.api.variant.ApplicationAndroidComponentsExtension> {
            finalizeDsl { applicationExtension ->
                // versionCode is Android's authoritative update-order value.
                if (releaseVersionCode != null) {
                    applicationExtension.defaultConfig.versionCode = releaseVersionCode
                }

                // versionName is the human-readable version displayed in About software/releases.
                if (releaseVersionName != null) {
                    applicationExtension.defaultConfig.versionName = releaseVersionName
                }
            }
        }
    }
}
