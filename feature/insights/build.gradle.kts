plugins {
    alias(libs.plugins.kairo.android.feature)
    alias(libs.plugins.kairo.android.screenshots)
}

android {
    namespace = "com.sravanthi.kairo.feature.insights"
}

dependencies {
    implementation(projects.core.health)
    implementation(libs.androidx.activity.compose)
}
