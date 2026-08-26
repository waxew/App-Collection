// Application module #51: notification-permission.
// The package/applicationId stays stable so future versions can update over the installed APK.
plugins {
    id("com.android.application")
}

android {
    namespace = "com.asteam.appcollection.p51"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.asteam.appcollection.p51"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}

// Shared shell provides the standard hamburger drawer/settings/about screens.
dependencies {
    implementation(project(":shared-ui"))
}
