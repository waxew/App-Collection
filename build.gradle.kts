// Android Gradle Plugin is declared once and reused by all 78 application modules.
// AGP 9.3 includes built-in Kotlin support, so a separate Kotlin Android plugin is not required.
plugins {
    id("com.android.application") version "9.3.0" apply false
    id("com.android.library") version "9.3.0" apply false
}
