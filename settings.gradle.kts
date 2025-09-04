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
    }
}

rootProject.name = "deuna-sdk-android"

include(":sdk")

// NOTE: Uncomment the following lines to include examples for local development
// The next lines must be commented when a release will be published
//include( "embedded-widgets")
//include( "widgets-in-modal")
//include( "checkout-web-wrapper")
//project(":embedded-widgets").projectDir = file("examples/embedded-widgets")
//project(":widgets-in-modal").projectDir = file("examples/widgets-in-modal")
//project(":checkout-web-wrapper").projectDir = file("examples/checkout-web-wrapper")