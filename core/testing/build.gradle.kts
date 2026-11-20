plugins {
    alias(libs.plugins.kairo.android.library)
}

android {
    namespace = "com.sravanthi.kairo.core.testing"
}

dependencies {
    api(projects.core.data)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
