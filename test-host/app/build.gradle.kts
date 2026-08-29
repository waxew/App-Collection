// Single installable host used to test all 78 rebuilt examples in one APK.
// The original 78 application modules remain untouched as reference projects.
plugins {
    id("com.android.application")
}

android {
    namespace = "com.asteam.appcollection.test"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.asteam.appcollection.test"
        minSdk = 23
        targetSdk = 36

        // Local builds keep a small readable version. CI may inject a monotonically increasing
        // releaseVersionCode so a newly built test host can update the previous installation.
        versionCode = providers.gradleProperty("releaseVersionCode").orNull?.toIntOrNull() ?: 2
        versionName = "1.0.1"

        // AndroidJUnitRunner executes the 78-screen launch smoke test on an emulator/device.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /*
     * IMPORTANT: AGP 9 uses built-in Kotlin support.
     *
     * In the first consolidated host build these paths were incorrectly added to
     * AndroidSourceSet.java. AGP 9 does NOT use custom Java source directories as
     * additional Kotlin source directories. The APK therefore contained the menu's
     * class-name strings but not the actual p01..p78 MainActivity bytecode, causing
     * ClassNotFoundException for every button.
     *
     * The supported AGP 9 configuration is AndroidSourceSet.kotlin. We point it at
     * the existing Kotlin source directories so no feature implementation is copied,
     * forked, or silently changed. Any future edit to an original numbered module is
     * therefore picked up by this one-APK test host automatically.
     */
    sourceSets {
        getByName("main") {
            kotlin.directories += "../../01-gps-live-location/src/main/java"
            kotlin.directories += "../../02-flashlight/src/main/java"
            kotlin.directories += "../../03-sqlite-crud/src/main/java"
            kotlin.directories += "../../04-music-player/src/main/java"
            kotlin.directories += "../../05-webview-browser/src/main/java"
            kotlin.directories += "../../06-camera-launcher/src/main/java"
            kotlin.directories += "../../07-battery-monitor/src/main/java"
            kotlin.directories += "../../08-foreground-service/src/main/java"
            kotlin.directories += "../../09-notification-modern/src/main/java"
            kotlin.directories += "../../10-send-email/src/main/java"
            kotlin.directories += "../../11-share-content/src/main/java"
            kotlin.directories += "../../12-ringtone-picker/src/main/java"
            kotlin.directories += "../../13-date-picker/src/main/java"
            kotlin.directories += "../../14-countdown-timer/src/main/java"
            kotlin.directories += "../../15-date-time/src/main/java"
            kotlin.directories += "../../16-open-browser/src/main/java"
            kotlin.directories += "../../17-dialer/src/main/java"
            kotlin.directories += "../../18-image-gallery/src/main/java"
            kotlin.directories += "../../19-custom-list/src/main/java"
            kotlin.directories += "../../20-custom-font/src/main/java"
            kotlin.directories += "../../21-finger-paint/src/main/java"
            kotlin.directories += "../../22-image-animations/src/main/java"
            kotlin.directories += "../../23-chronometer/src/main/java"
            kotlin.directories += "../../24-rating-bar/src/main/java"
            kotlin.directories += "../../25-spinner-selector/src/main/java"
            kotlin.directories += "../../26-autocomplete/src/main/java"
            kotlin.directories += "../../27-multi-autocomplete/src/main/java"
            kotlin.directories += "../../28-simple-list/src/main/java"
            kotlin.directories += "../../29-grid-view/src/main/java"
            kotlin.directories += "../../30-radio-group/src/main/java"
            kotlin.directories += "../../31-checkbox/src/main/java"
            kotlin.directories += "../../32-toggle-button/src/main/java"
            kotlin.directories += "../../33-keyboard-input-types/src/main/java"
            kotlin.directories += "../../34-long-press-context/src/main/java"
            kotlin.directories += "../../35-html-text/src/main/java"
            kotlin.directories += "../../36-choice-dialog/src/main/java"
            kotlin.directories += "../../37-animated-dialog/src/main/java"
            kotlin.directories += "../../38-custom-toast/src/main/java"
            kotlin.directories += "../../39-form-validation/src/main/java"
            kotlin.directories += "../../40-array-adapter-list/src/main/java"
            kotlin.directories += "../../41-gps-ui-layout/src/main/java"
            kotlin.directories += "../../42-gps-permissions/src/main/java"
            kotlin.directories += "../../43-music-player-ui/src/main/java"
            kotlin.directories += "../../44-audio-manifest-modern/src/main/java"
            kotlin.directories += "../../45-webview-ui-layout/src/main/java"
            kotlin.directories += "../../46-webview-manifest-modern/src/main/java"
            kotlin.directories += "../../47-email-form-ui/src/main/java"
            kotlin.directories += "../../48-email-manifest/src/main/java"
            kotlin.directories += "../../49-notification-sound/src/main/java"
            kotlin.directories += "../../50-notification-ui/src/main/java"
            kotlin.directories += "../../51-notification-permission/src/main/java"
            kotlin.directories += "../../52-animated-dialog-ui/src/main/java"
            kotlin.directories += "../../53-dialog-dependency-migration/src/main/java"
            kotlin.directories += "../../54-form-ui-layout/src/main/java"
            kotlin.directories += "../../55-form-manifest/src/main/java"
            kotlin.directories += "../../56-custom-list-layout/src/main/java"
            kotlin.directories += "../../57-custom-list-row/src/main/java"
            kotlin.directories += "../../58-array-list-layout/src/main/java"
            kotlin.directories += "../../59-list-manifest/src/main/java"
            kotlin.directories += "../../60-listactivity-modern/src/main/java"
            kotlin.directories += "../../61-listactivity-manifest/src/main/java"
            kotlin.directories += "../../62-dialog-suite/src/main/java"
            kotlin.directories += "../../63-custom-dialog-one/src/main/java"
            kotlin.directories += "../../64-custom-dialog-layout/src/main/java"
            kotlin.directories += "../../65-custom-dialog-manifest/src/main/java"
            kotlin.directories += "../../66-custom-dialog-two/src/main/java"
            kotlin.directories += "../../67-custom-dialog-two-manifest/src/main/java"
            kotlin.directories += "../../68-toast-layout-migration/src/main/java"
            kotlin.directories += "../../69-toast-manifest/src/main/java"
            kotlin.directories += "../../70-flashlight-legacy-rewrite/src/main/java"
            kotlin.directories += "../../71-clock-modern/src/main/java"
            kotlin.directories += "../../72-clock-toolbox/src/main/java"
            kotlin.directories += "../../73-finger-paint-toolbox/src/main/java"
            kotlin.directories += "../../74-move-object-animation/src/main/java"
            kotlin.directories += "../../75-move-text-animation/src/main/java"
            kotlin.directories += "../../76-date-picker-toolbox/src/main/java"
            kotlin.directories += "../../77-send-email-toolbox/src/main/java"
            kotlin.directories += "../../78-progress-dialog-modern/src/main/java"
        }
    }
}

dependencies {
    // Shared shell contains the common hamburger drawer, settings, about and contact UI.
    implementation(project(":shared-ui"))

    // Current stable AndroidX Test components used only by emulator/device QA.
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.3.0")
}
