plugins {
    alias(libs.plugins.kairo.android.library)
    alias(libs.plugins.kairo.hilt)
}

android {
    namespace = "com.sravanthi.kairo.core.reminders"
}

dependencies {
    implementation(projects.core.data)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
