import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val Project.libsCatalog
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun KotlinMultiplatformExtension.iosTargets(): List<KotlinNativeTarget> = listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
)

/**
 * 获取当前变体的环境类型
 *
 * @param defaultEnv 默认环境，如果未指定 appEnv 属性时使用，默认为 "dev"
 * @return 环境类型："dev" 或 "prod"
 *
 * @sample
 * ```kotlin
 * val env = project.getVariantEnvironment() // "dev" 或 "prod"
 * val env = project.getVariantEnvironment("prod") // 指定默认值
 * ```
 */
fun Project.getVariantEnvironment(defaultEnv: String = "dev"): String {
    return findProperty("appEnv")?.toString() ?: defaultEnv
}

/**
 * 获取当前变体的级别类型
 *
 * @param defaultTier 默认级别，如果未指定 appTier 属性时使用，默认为 "free"
 * @return 级别类型："free" 或 "premium"
 *
 * @sample
 * ```kotlin
 * val tier = project.getVariantTier() // "free" 或 "premium"
 * val tier = project.getVariantTier("premium") // 指定默认值
 * ```
 */
fun Project.getVariantTier(defaultTier: String = "free"): String {
    return findProperty("appTier")?.toString() ?: defaultTier
}

/**
 * 判断当前是否为开发环境（dev）
 *
 * @return true 如果是 dev 环境（通过 appEnv 属性判断，不区分大小写）
 *
 * @sample
 * ```kotlin
 * if (project.isDev()) {
 *     // 开发环境特定逻辑
 *     implementation(compose.components.uiToolingPreview)
 * }
 * ```
 */
fun Project.isDev(): Boolean {
    val env = getVariantEnvironment()
    return env.equals("dev", ignoreCase = true)
}

/**
 * 判断当前是否为生产环境（prod）
 *
 * @return true 如果是 prod 环境（通过 appEnv 属性判断，不区分大小写）
 *
 * @sample
 * ```kotlin
 * if (project.isProd()) {
 *     // 生产环境特定逻辑
 *     implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
 * }
 * ```
 */
fun Project.isProd(): Boolean {
    val env = getVariantEnvironment()
    return env.equals("prod", ignoreCase = true)
}

/**
 * 判断当前是否为免费级别（free）
 *
 * @return true 如果是 free 级别（通过 appTier 属性判断，不区分大小写）
 *
 * @sample
 * ```kotlin
 * if (project.isFree()) {
 *     // 免费版本特定逻辑
 * }
 * ```
 */
fun Project.isFree(): Boolean {
    val tier = getVariantTier()
    return tier.equals("free", ignoreCase = true)
}

/**
 * 判断当前是否为高级级别（premium）
 *
 * @return true 如果是 premium 级别（通过 appTier 属性判断，不区分大小写）
 *
 * @sample
 * ```kotlin
 * if (project.isPremium()) {
 *     // 高级版本特定逻辑
 *     implementation("com.example:premium-features:1.0.0")
 * }
 * ```
 */
fun Project.isPremium(): Boolean {
    val tier = getVariantTier()
    return tier.equals("premium", ignoreCase = true)
}

// ========== 渠道判断函数 ==========

/**
 * 获取当前变体的渠道类型
 *
 * @param defaultChannel 默认渠道，如果未指定 appChannel 属性时使用，默认为 "channel"
 * @return 渠道类型，例如 "googlePlay", "umeng", "vivo"，如果未指定则返回默认值 "channel"
 *
 * @sample
 * ```kotlin
 * val channel = project.getVariantChannel() // "googlePlay" 或 "channel"（默认值）
 * val channel = project.getVariantChannel("custom") // 指定默认值
 * ```
 */
fun Project.getVariantChannel(defaultChannel: String = "channel"): String {
    return findProperty("appChannel")?.toString() ?: defaultChannel
}

/**
 * 判断当前是否为指定渠道
 *
 * @param channel 渠道名称，不区分大小写
 * @return true 如果是指定渠道
 *
 * @sample
 * ```kotlin
 * if (project.isChannel("googlePlay")) {
 *     // Google Play 渠道特定逻辑
 *     implementation("com.google.firebase:firebase-analytics-ktx:21.5.0")
 * }
 * ```
 */
fun Project.isChannel(channel: String): Boolean {
    val currentChannel = getVariantChannel()
    return currentChannel.equals(channel, ignoreCase = true)
}

