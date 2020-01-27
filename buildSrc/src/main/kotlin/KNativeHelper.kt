import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTestsPreset

val isDevMode = System.getProperty("idea.active") == "true"

val preset =
    when {
        Os.isFamily(Os.FAMILY_MAC) -> "macosX64"
        Os.isFamily(Os.FAMILY_UNIX) -> "linuxX64"
        Os.isFamily(Os.FAMILY_WINDOWS) -> "mingwX64"
        else -> throw RuntimeException("Target ${System.getProperty("os.name")} is not supported")
    }

fun KotlinMultiplatformExtension.currentTarget(
    name: String? = null,
    config: KotlinNativeTarget.() -> Unit = {}
): KotlinNativeTarget {
    val createTarget = (presets.getByName(preset) as KotlinNativeTargetWithTestsPreset).createTarget(name ?: preset)
    targets.add(createTarget)
    config(createTarget)
    return createTarget
}
