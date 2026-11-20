plugins {
    alias(libs.plugins.kairo.android.library)
    alias(libs.plugins.kairo.hilt)
}

android {
    namespace = "com.sravanthi.kairo.core.health"
}

dependencies {
    api(projects.core.model)
    implementation(libs.androidx.health.connect.client)
    implementation(libs.androidx.activity.compose)
}
