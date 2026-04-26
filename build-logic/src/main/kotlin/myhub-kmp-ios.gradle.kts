import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}
configure<KotlinMultiplatformExtension> {

    iosTargets()

    // 1. 解析当前激活的变体组件（独立参数）
    val env = project.getVariantEnvironment()
    val tier = project.getVariantTier()
    val channel = project.getVariantChannel()
    val envTitle = env.replaceFirstChar { it.uppercase() }
    val tierTitle = tier.replaceFirstChar { it.uppercase() }
    val channelTitle = channel.replaceFirstChar { it.uppercase() }

    sourceSets {
        var baseMain = commonMain.get()
        baseMain = maybeCreate("commonNativeMain").apply { dependsOn(baseMain) }
        baseMain = maybeCreate("mobileMain").apply { dependsOn(baseMain) }
        nativeMain.get().dependsOn(baseMain)

        fun KotlinSourceSet.injectPlatformVariant(platform: String) {
            // 注入nonWeb, 代码不对 web平台生效，其他都可以生效。
            kotlin.srcDir("src/shared/nonWeb/kotlin")
            resources.srcDir("src/shared/nonWeb/resources")

            val p = platform.lowercase()
            // 注入平台环境代码 (e.g., src/androidDevMain)
            kotlin.srcDir("src/${p}${envTitle}Main/kotlin")
            resources.srcDir("src/${p}${envTitle}Main/resources")
            // 注入平台级别代码 (e.g., src/androidFreeMain)
            kotlin.srcDir("src/${p}${tierTitle}Main/kotlin")
            resources.srcDir("src/${p}${tierTitle}Main/resources")
            // 注入平台渠道代码 (e.g., src/androidGooglePlayMain)，如果指定了渠道
            kotlin.srcDir("src/${p}${channelTitle}Main/kotlin")
            resources.srcDir("src/${p}${channelTitle}Main/resources")
        }
        iosMain.get().injectPlatformVariant("ios")
    }
}
