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
        versionCode = 1
        versionName = "1.0.0"
    }

    // Reuse the original Kotlin source files directly instead of duplicating their logic.
    // Every source keeps its existing package, so the 78 MainActivity classes remain unique.
    sourceSets {
        getByName("main") {
            java.srcDirs(
                "../../01-gps-live-location/src/main/java",
                "../../02-flashlight/src/main/java",
                "../../03-sqlite-crud/src/main/java",
                "../../04-music-player/src/main/java",
                "../../05-webview-browser/src/main/java",
                "../../06-camera-launcher/src/main/java",
                "../../07-battery-monitor/src/main/java",
                "../../08-foreground-service/src/main/java",
                "../../09-notification-modern/src/main/java",
                "../../10-send-email/src/main/java",
                "../../11-share-content/src/main/java",
                "../../12-ringtone-picker/src/main/java",
                "../../13-date-picker/src/main/java",
                "../../14-countdown-timer/src/main/java",
                "../../15-date-time/src/main/java",
                "../../16-open-browser/src/main/java",
                "../../17-dialer/src/main/java",
                "../../18-image-gallery/src/main/java",
                "../../19-custom-list/src/main/java",
                "../../20-custom-font/src/main/java",
                "../../21-finger-paint/src/main/java",
                "../../22-image-animations/src/main/java",
                "../../23-chronometer/src/main/java",
                "../../24-rating-bar/src/main/java",
                "../../25-spinner-selector/src/main/java",
                "../../26-autocomplete/src/main/java",
                "../../27-multi-autocomplete/src/main/java",
                "../../28-simple-list/src/main/java",
                "../../29-grid-view/src/main/java",
                "../../30-radio-group/src/main/java",
                "../../31-checkbox/src/main/java",
                "../../32-toggle-button/src/main/java",
                "../../33-keyboard-input-types/src/main/java",
                "../../34-long-press-context/src/main/java",
                "../../35-html-text/src/main/java",
                "../../36-choice-dialog/src/main/java",
                "../../37-animated-dialog/src/main/java",
                "../../38-custom-toast/src/main/java",
                "../../39-form-validation/src/main/java",
                "../../40-array-adapter-list/src/main/java",
                "../../41-gps-ui-layout/src/main/java",
                "../../42-gps-permissions/src/main/java",
                "../../43-music-player-ui/src/main/java",
                "../../44-audio-manifest-modern/src/main/java",
                "../../45-webview-ui-layout/src/main/java",
                "../../46-webview-manifest-modern/src/main/java",
                "../../47-email-form-ui/src/main/java",
                "../../48-email-manifest/src/main/java",
                "../../49-notification-sound/src/main/java",
                "../../50-notification-ui/src/main/java",
                "../../51-notification-permission/src/main/java",
                "../../52-animated-dialog-ui/src/main/java",
                "../../53-dialog-dependency-migration/src/main/java",
                "../../54-form-ui-layout/src/main/java",
                "../../55-form-manifest/src/main/java",
                "../../56-custom-list-layout/src/main/java",
                "../../57-custom-list-row/src/main/java",
                "../../58-array-list-layout/src/main/java",
                "../../59-list-manifest/src/main/java",
                "../../60-listactivity-modern/src/main/java",
                "../../61-listactivity-manifest/src/main/java",
                "../../62-dialog-suite/src/main/java",
                "../../63-custom-dialog-one/src/main/java",
                "../../64-custom-dialog-layout/src/main/java",
                "../../65-custom-dialog-manifest/src/main/java",
                "../../66-custom-dialog-two/src/main/java",
                "../../67-custom-dialog-two-manifest/src/main/java",
                "../../68-toast-layout-migration/src/main/java",
                "../../69-toast-manifest/src/main/java",
                "../../70-flashlight-legacy-rewrite/src/main/java",
                "../../71-clock-modern/src/main/java",
                "../../72-clock-toolbox/src/main/java",
                "../../73-finger-paint-toolbox/src/main/java",
                "../../74-move-object-animation/src/main/java",
                "../../75-move-text-animation/src/main/java",
                "../../76-date-picker-toolbox/src/main/java",
                "../../77-send-email-toolbox/src/main/java",
                "../../78-progress-dialog-modern/src/main/java"
            )
        }
    }
}

dependencies {
    implementation(project(":shared-ui"))
}
