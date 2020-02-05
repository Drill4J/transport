import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("kotlin-multiplatform")
}

kotlin {

    targets {
        if (isDevMode) {
            currentTarget("commonNative")
        } else {
            mingwX64()
            linuxX64()
            macosX64()
        }
    }

    sourceSets {

        val commonNativeMain: KotlinSourceSet = maybeCreate("commonNativeMain")
        with(commonNativeMain) {
            dependencies {
                implementation("io.ktor:ktor-io:$ktorLibsVersion")
                implementation("io.ktor:ktor-utils-native:$ktorLibsVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
                implementation(project(":core:util"))
            }
        }
        if (!isDevMode) {
            @Suppress("UNUSED_VARIABLE") val mingwX64Main by getting { dependsOn(commonNativeMain) }
            @Suppress("UNUSED_VARIABLE") val linuxX64Main by getting { dependsOn(commonNativeMain) }
            @Suppress("UNUSED_VARIABLE") val macosX64Main by getting { dependsOn(commonNativeMain) }
        }

    }
}