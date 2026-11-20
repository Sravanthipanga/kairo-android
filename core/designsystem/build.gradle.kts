plugins {
    alias(libs.plugins.kairo.android.library)
    alias(libs.plugins.kairo.android.compose)
}

android {
    namespace = "com.sravanthi.kairo.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
}
