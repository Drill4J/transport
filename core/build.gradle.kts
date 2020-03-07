plugins {
    id("org.jetbrains.kotlin.multiplatform") version ("1.3.70")
    id("com.epam.drill.cross-compilation") version "0.15.1"
}

val ktorLibsVersion: String by extra
val coroutinesVersion: String by extra

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

kotlin {

    targets {
        crossCompilation {
            common {
                dependencies {
                    implementation("io.ktor:ktor-io:$ktorLibsVersion")
                    implementation("io.ktor:ktor-utils-native:$ktorLibsVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
                    implementation(project(":core:util"))
                }
            }
        }
        mingwX64()
        linuxX64()
        macosX64()
    }


}