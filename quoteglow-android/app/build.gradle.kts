plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.quoteglow.widgets"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quoteglow.widgets"
        minSdk = 31
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.glance:glance-appwidget:1.1.1")
}
