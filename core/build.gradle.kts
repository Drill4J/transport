plugins {
    id("org.jetbrains.kotlin.multiplatform") version ("1.3.70")
    id("com.epam.drill.cross-compilation") version "0.16.0"
}

val ktorLibsVersion: String by extra
val coroutinesVersion: String by extra

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url = "https://oss.jfrog.org/artifactory/list/oss-release-local")
}

kotlin {

    targets {
        crossCompilation {
            common {
                dependencies {
                    implementation("io.ktor:ktor-io:$ktorLibsVersion")
                    implementation("io.ktor:ktor-utils-native:$ktorLibsVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
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