plugins {
    alias(libs.plugins.kairo.android.application)
    alias(libs.plugins.kairo.android.compose)
    alias(libs.plugins.kairo.hilt)
}

android {
    namespace = "com.sravanthi.kairo"

    defaultConfig {
        applicationId = "com.sravanthi.kairo"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.feature.medications)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
}
