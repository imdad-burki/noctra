pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "noctra"
include(":app")
include(":core:common")
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":core:clock")
include(":core:wallpaper")
include(":feature:home")
include(":feature:clock")
include(":feature:discover")
include(":service:livewallpaper")
