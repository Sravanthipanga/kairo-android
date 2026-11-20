pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "kairo"

include(":app")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:reminders")
include(":core:health")
include(":core:testing")
include(":feature:medications")
include(":feature:today")
include(":feature:settings")
include(":feature:insights")
include(":feature:widget")
