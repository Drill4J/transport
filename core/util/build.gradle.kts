plugins {
    id("kotlin-multiplatform")
    id("com.epam.drill.cross-compilation")
}

kotlin {
    targets {
        mingwX64()
        linuxX64()
        macosX64()

    }
    targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .forEach { it.compilations["main"].cinterops?.create("sockets") }
}
