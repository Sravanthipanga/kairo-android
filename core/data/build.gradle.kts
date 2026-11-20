plugins {
    alias(libs.plugins.kairo.android.library)
    alias(libs.plugins.kairo.hilt)
}

android {
    namespace = "com.sravanthi.kairo.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(libs.kotlinx.coroutines.core)
}
