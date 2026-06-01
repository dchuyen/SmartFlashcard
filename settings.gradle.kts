pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Đăng ký tập trung phiên bản các plugin cốt lõi tại đây
        id("com.android.application") version "8.2.2"
        id("com.google.gms.google-services") version "4.4.1" // Hạ xuống 4.4.1 để tương thích tốt hơn với AGP 8.2.2
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "SmartFlashcard"
include(":app")