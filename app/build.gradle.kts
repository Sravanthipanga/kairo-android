import java.util.Properties

plugins {
    alias(libs.plugins.kairo.android.application)
    alias(libs.plugins.kairo.android.compose)
    alias(libs.plugins.kairo.hilt)
}

/**
 * Release signing comes from outside the repository, never from committed files:
 * - locally: a git-ignored `keystore.properties` in the project root, or
 * - in CI: the KAIRO_* environment variables set from GitHub secrets.
 * Without either, release builds are produced unsigned. See RELEASING.md.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun signingValue(property: String, env: String): String? =
    keystoreProperties.getProperty(property) ?: providers.environmentVariable(env).orNull

android {
    namespace = "com.sravanthi.kairo"

    signingConfigs {
        create("release") {
            signingValue("storeFile", "KAIRO_KEYSTORE_PATH")?.let { path ->
                storeFile = file(path)
                storePassword = signingValue("storePassword", "KAIRO_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "KAIRO_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "KAIRO_KEY_PASSWORD")
            }
        }
    }

    defaultConfig {
        applicationId = "com.sravanthi.kairo"
        // Bump both for every Play Store upload: versionCode must always increase.
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.reminders)
    implementation(projects.feature.medications)
    implementation(projects.feature.today)
    implementation(projects.feature.settings)
    implementation(projects.feature.insights)
    implementation(projects.feature.widget)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    implementation(libs.androidx.compose.material.icons.extended)
}
