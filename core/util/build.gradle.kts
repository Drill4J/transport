plugins {
    id("kotlin-multiplatform")
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
