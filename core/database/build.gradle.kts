plugins {
    alias(libs.plugins.kairo.android.library)
    alias(libs.plugins.kairo.android.room)
    alias(libs.plugins.kairo.hilt)
}

android {
    namespace = "com.sravanthi.kairo.core.database"
}

dependencies {
    implementation(projects.core.model)
}
