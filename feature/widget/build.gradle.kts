plugins {
    alias(libs.plugins.kairo.android.library)
    alias(libs.plugins.kairo.android.compose)
    alias(libs.plugins.kairo.hilt)
}

android {
    namespace = "com.sravanthi.kairo.feature.widget"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.reminders)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
