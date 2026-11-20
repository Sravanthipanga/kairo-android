plugins {
    alias(libs.plugins.kairo.android.feature)
}

android {
    namespace = "com.sravanthi.kairo.feature.settings"
}

dependencies {
    implementation(libs.androidx.biometric)
}
