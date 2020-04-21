plugins {
    kotlin("multiplatform")
    id("com.epam.drill.cross-compilation")
    `maven-publish`
}

val ktorLibsVersion: String by extra
val coroutinesVersion: String by extra
val drillLoggerVersion: String by extra

kotlin {

    targets {
        crossCompilation {
            common {
                dependencies {
                    implementation("io.ktor:ktor-io:$ktorLibsVersion")
                    implementation("io.ktor:ktor-utils-native:$ktorLibsVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
                    implementation("com.epam.drill.logger:logger:$drillLoggerVersion")
                }
            }
        }
        mingwX64()
        linuxX64()
        macosX64()
    }
    targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .forEach { it.compilations.forEach { it.cinterops?.create("sockets") } }
}
