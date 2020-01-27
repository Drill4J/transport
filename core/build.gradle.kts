import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("kotlin-multiplatform")
    `maven-publish`
}
repositories {
    mavenCentral()
    jcenter()
    mavenLocal()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "https://dl.bintray.com/kotlin/ktor/")
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
                implementation("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVersion") {
                    exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common")
                }
                implementation("io.ktor:ktor-utils-native:$ktorUtilVersion") {
                    exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common")
                    exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-native")
                }
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

tasks.withType<KotlinNativeCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlinx.io.core.ExperimentalIoApi"
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
}
publishing {
    repositories {
        maven {

            url = uri("http://oss.jfrog.org/oss-release-local")
            credentials {
                username =
                    if (project.hasProperty("bintrayUser"))
                        project.property("bintrayUser").toString()
                    else System.getenv("BINTRAY_USER")
                password =
                    if (project.hasProperty("bintrayApiKey"))
                        project.property("bintrayApiKey").toString()
                    else System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}