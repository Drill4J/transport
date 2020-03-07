rootProject.name = "transport"
include(":core")
include(":core:util")

pluginManagement {
    repositories {
        maven(url = "http://oss.jfrog.org/oss-release-local")
        gradlePluginPortal()
    }
}