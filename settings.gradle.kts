rootProject.name = "transport"

val scriptUrl: String by extra
apply(from = "$scriptUrl/maven-repo.settings.gradle.kts")

pluginManagement {
    val kotlinVersion: String by extra
    val drillGradlePluginVersion: String by extra
    val licenseVersion: String by extra
    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("com.epam.drill.cross-compilation") version drillGradlePluginVersion
        id("com.github.hierynomus.license") version licenseVersion
    }
}

include(":core")
