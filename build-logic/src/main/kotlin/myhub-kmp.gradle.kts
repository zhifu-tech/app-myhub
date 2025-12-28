import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

configure<KotlinMultiplatformExtension> {
    applyDefaultHierarchyTemplate()

    // 抑制 expect/actual classes Beta 警告
    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    android {
        compileSdk = libsCatalog.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libsCatalog.findVersion("android-minSdk").get().requiredVersion.toInt()

        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosTargets()

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    // 1. 解析当前激活的变体组件 (例如 "devFree" -> env="dev", tier="free")
    val activeVariant = (project.findProperty("appVariant") ?: "devFree").toString()

    val isDev = activeVariant.startsWith("dev", ignoreCase = true)
    val env = if (isDev) "dev" else "prod"
    val envTitle = env.replaceFirstChar { it.uppercase() }

    val isPremium = activeVariant.contains("Premium", ignoreCase = true)
    val tier = if (isPremium) "premium" else "free"
    val tierTitle = tier.replaceFirstChar { it.uppercase() }

    val variantTitle = activeVariant.replaceFirstChar { it.uppercase() }

    sourceSets {
        // 2. 注入业务逻辑 (Common)
        commonMain.get().apply {
            // 注入环境通用目录 (e.g., src/devMain)
            kotlin.srcDir("src/${env}Main/kotlin")
            // 注入级别通用目录 (e.g., src/freeMain)
            kotlin.srcDir("src/${tier}Main/kotlin")
            // 注入完整变体目录 (e.g., src/devFreeMain)
            kotlin.srcDir("src/${activeVariant}Main/kotlin")

            resources.srcDir("src/${env}Main/resources")
            resources.srcDir("src/${tier}Main/resources")
            resources.srcDir("src/${activeVariant}Main/resources")
        }

        // 3. 辅助函数：注入平台特定的变体组合目录
        fun KotlinSourceSet.injectPlatformVariant(platform: String) {
            val p = platform.lowercase()
            // 注入平台环境代码 (e.g., src/androidDevMain)
            kotlin.srcDir("src/${p}${envTitle}Main/kotlin")
            // 注入平台级别代码 (e.g., src/androidFreeMain)
            kotlin.srcDir("src/${p}${tierTitle}Main/kotlin")
            // 注入平台全变体代码 (e.g., src/androidDevFreeMain)
            kotlin.srcDir("src/${p}${variantTitle}Main/kotlin")

            resources.srcDir("src/${p}${envTitle}Main/resources")
            resources.srcDir("src/${p}${tierTitle}Main/resources")
            resources.srcDir("src/${p}${variantTitle}Main/resources")
        }

        // 应用到各平台默认源集
        androidMain.get().injectPlatformVariant("android")
        iosMain.get().injectPlatformVariant("ios")
        jvmMain.get().injectPlatformVariant("jvm")
        jsMain.get().injectPlatformVariant("js")
    }
}
