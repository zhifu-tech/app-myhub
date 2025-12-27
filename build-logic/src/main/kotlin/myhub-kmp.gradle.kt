import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val Project.libsCatalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun KotlinMultiplatformExtension.iosTargets(): List<KotlinNativeTarget> = listOf(
    iosArm64(),
    iosSimulatorArm64()
)
