// Standalone Gradle settings for the one-APK test host.
// Keeping this project nested means the original 78 application modules remain unchanged.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "App-Collection-Test-Host"

// The host itself is the only installable application in this nested build.
include(":app")

// Reuse the common drawer/settings/about implementation from the main repository.
include(":shared-ui")
project(":shared-ui").projectDir = file("../shared-ui")
