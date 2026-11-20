plugins {
    alias(libs.plugins.kairo.android.feature)
}

android {
    namespace = "com.sravanthi.kairo.feature.insights"
}

dependencies {
    implementation(projects.core.health)
    implementation(libs.androidx.activity.compose)
}
