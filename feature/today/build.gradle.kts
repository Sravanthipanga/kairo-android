plugins {
    alias(libs.plugins.kairo.android.feature)
    alias(libs.plugins.kairo.android.screenshots)
}

android {
    namespace = "com.sravanthi.kairo.feature.today"
}

dependencies {
    implementation(projects.core.reminders)
    implementation(libs.androidx.activity.compose)
}
