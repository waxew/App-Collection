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
 * IMPORTANT ORDERING DETAIL:
 * A plugins.withId callback can run as soon as a module applies the Android plugin. The module's own
 * build.gradle.kts is still being evaluated at that point and can later overwrite values configured
 * by the root project. Therefore release overrides are applied inside projectsEvaluated, after all
 * 78 module scripts have finished. This guarantees the values injected by CI are the final Android
 * package metadata rather than merely values printed in artifact filenames.
 */
gradle.projectsEvaluated {
    // Read the optional CI properties only once after all project scripts are available.
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

    // Apply the final override to every subproject that is an installable Android application.
    subprojects.forEach { subproject ->
        subproject.plugins.withId("com.android.application") {
            subproject.extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
                defaultConfig {
                    // versionCode is Android's authoritative update-order number.
                    if (releaseVersionCode != null) {
                        versionCode = releaseVersionCode
                    }

                    // versionName is the user-facing release label displayed in About software.
                    if (releaseVersionName != null) {
                        versionName = releaseVersionName
                    }
                }
            }
        }
    }
}
