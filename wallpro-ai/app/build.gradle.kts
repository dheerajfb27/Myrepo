plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }

android { namespace="com.dheeraj.wallproai"; compileSdk=36
    defaultConfig { applicationId="com.dheeraj.wallproai"; minSdk=29; targetSdk=36; versionCode=2; versionName="0.2.0" }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
