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
            currentTarget()
        } else {
            mingwX64()
            linuxX64()
            macosX64()
        }
    }
    targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .forEach { it.compilations["main"].cinterops?.create("sockets") }
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