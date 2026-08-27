// Android Gradle Plugin is declared once and reused by all 78 application modules.
// AGP 9.3 includes built-in Kotlin support, so a separate Kotlin Android plugin is not required.
plugins {
    id("com.android.application") version "9.3.0" apply false
    id("com.android.library") version "9.3.0" apply false
}

/**
 * Shared release-version rule for every Android application in this repository.
 *
 * Each module keeps its normal local versionCode/versionName in its own build.gradle.kts so the
 * source remains readable. In CI, the workflow passes -PreleaseVersionCode=<monotonic number>.
 * That value overrides versionCode for every application module in the current build.
 *
 * Why this is required:
 * Android/Google Play use versionCode to decide whether an APK is newer than the installed one.
 * Rebuilding changed source forever with versionCode=1 is not a valid release/update strategy.
 */
subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
            val ciVersionCode = providers
                .gradleProperty("releaseVersionCode")
                .orNull
                ?.toIntOrNull()

            if (ciVersionCode != null && ciVersionCode > 0) {
                defaultConfig {
                    versionCode = ciVersionCode
                }
            }
        }
    }
}
