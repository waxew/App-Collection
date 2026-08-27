// Temporary application module used to compile the isolated AS Team drawer prototype on this branch.
// The package/applicationId remains stable so repeated test APKs can update over the prior install.
plugins {
    id("com.android.application")
}

android {
    namespace = "com.asteam.appcollection.p11"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.asteam.appcollection.p11"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}

// The shared module stays linked so this prototype is compatible with the collection architecture.
dependencies {
    implementation(project(":shared-ui"))
}
