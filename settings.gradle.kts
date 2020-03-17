rootProject.name = "transport"
include(":core")

pluginManagement {
    repositories {
        maven(url = "http://oss.jfrog.org/oss-release-local")
        gradlePluginPortal()
    }
}