plugins {
    kotlin("multiplatform")
    id("com.epam.drill.cross-compilation")
    `maven-publish`
}

val ktorLibsVersion: String by extra
val coroutinesVersion: String by extra
val drillLoggerVersion: String by extra

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")).with(module("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion-native-mt"))
    }
}

kotlin {

    targets {
        crossCompilation {
            common {
                defaultSourceSet {
                    dependsOn(sourceSets.named("commonMain").get())
                }
                dependencies {
                    implementation("io.ktor:ktor-io:$ktorLibsVersion")
                    implementation("io.ktor:ktor-utils-native:$ktorLibsVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
                    implementation("com.epam.drill.logger:logger:$drillLoggerVersion")
                }
            }
            posix {
                defaultSourceSet {
                    dependsOn(sourceSets.named("commonMain").get())
                }
            }
        }
        mingwX64()
        linuxX64()
        macosX64()
    }
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
        }
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
            }
        }
    }
    targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .forEach { it.compilations.forEach { it.cinterops?.create("sockets") } }
}
