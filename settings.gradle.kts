pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // maven { url = uri("https://artifacts.mercadolibre.com/repository/android-releases") }
    }
}

rootProject.name = "deuna-sdk-android"

include(":sdk")

// NOTE: Uncomment the following lines to include examples for local development
// The next lines must be commented when a release will be published
//include( "checkout-web-wrapper")
//include( "explore")
//project(":checkout-web-wrapper").projectDir = file("examples/checkout-web-wrapper")
//project(":explore").projectDir = file("examples/explore")