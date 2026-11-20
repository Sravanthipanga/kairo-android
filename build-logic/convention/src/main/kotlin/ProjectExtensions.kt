import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

internal object KairoSdk {
    const val COMPILE = 36
    const val MIN = 26
    const val TARGET = 35
    val JAVA = JavaVersion.VERSION_17
}

/** Shared Android + Kotlin settings for every Android module. */
internal fun Project.configureKotlinAndroid(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.apply {
        compileSdk = KairoSdk.COMPILE
        defaultConfig {
            minSdk = KairoSdk.MIN
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileOptions {
            sourceCompatibility = KairoSdk.JAVA
            targetCompatibility = KairoSdk.JAVA
        }
    }
    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

internal fun Project.configureKotlinJvm() {
    extensions.configure(org.gradle.api.plugins.JavaPluginExtension::class.java) {
        sourceCompatibility = KairoSdk.JAVA
        targetCompatibility = KairoSdk.JAVA
    }
    extensions.configure(KotlinJvmProjectExtension::class.java) {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}
