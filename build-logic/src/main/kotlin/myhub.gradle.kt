import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val Project.libsCatalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")


fun KotlinMultiplatformExtension.iosTargets() = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

fun Project.getVariantEnvironment(defaultEnv: String = "dev") = findProperty("appEnv")?.toString() ?: defaultEnv

fun Project.getVariantTier(defaultTier: String = "free"): String = findProperty("appTier")?.toString() ?: defaultTier

fun Project.isDev(): Boolean = getVariantEnvironment().equals("dev", ignoreCase = true)

fun Project.isProd(): Boolean = getVariantEnvironment().equals("prod", ignoreCase = true)

fun Project.isFree(): Boolean = getVariantTier().equals("free", ignoreCase = true)

fun Project.isPremium(): Boolean = getVariantTier().equals("premium", ignoreCase = true)

fun Project.getVariantChannel(defaultChannel: String = "channel"): String =
    findProperty("appChannel")?.toString() ?: defaultChannel

fun Project.isChannelGooglePlay(): Boolean = getVariantChannel().equals("googlePlay", ignoreCase = true)

fun Project.isChannelUmeng(): Boolean = getVariantChannel().equals("umeng", ignoreCase = true)

// ============================================================================
// 平台按需加载工具函数
// ============================================================================
private fun Project.isPlatformEnabled(platformKey: String) =
    rootProject.extensions.extraProperties.takeIf { it.has(platformKey) }
        ?.let { it.get(platformKey) == true }
        ?: false

fun Project.isAndroidEnabled(): Boolean = isPlatformEnabled("isAndroidEnabled")

fun Project.isIosEnabled(): Boolean = isPlatformEnabled("isIosEnabled")

fun Project.isJvmEnabled(): Boolean = isPlatformEnabled("isJvmEnabled")

fun Project.isJsEnabled(): Boolean = isPlatformEnabled("isJsEnabled")

fun Project.isWasmJsEnabled(): Boolean = isPlatformEnabled("isWasmJsEnabled")

fun Project.isServerEnabled(): Boolean = isPlatformEnabled("isServerEnabled")
