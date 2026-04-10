import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

configure<KotlinMultiplatformExtension> {
    android {
        compileSdk = libsCatalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libsCatalog.findVersion("android-minSdk").get().requiredVersion.toInt()
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    // 1. 解析当前激活的变体组件（独立参数）
    val env = project.getVariantEnvironment()
    val tier = project.getVariantTier()
    val channel = project.getVariantChannel()
    val envTitle = env.replaceFirstChar { it.uppercase() }
    val tierTitle = tier.replaceFirstChar { it.uppercase() }
    val channelTitle = channel.replaceFirstChar { it.uppercase() }

    sourceSets {
        fun KotlinSourceSet.injectPlatformVariant(platform: String) {
            // 注入平台通用代码 (e.g., src/nonWebMain)
            kotlin.srcDir("src/nonWebMain/kotlin")
            resources.srcDir("src/nonWebMain/resources")

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
        androidMain.get().injectPlatformVariant("android")
    }
}
