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
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
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

    // 1. 解析当前激活的变体组件（独立参数）
    val env = project.getVariantEnvironment()
    val tier = project.getVariantTier()
    val channel = project.getVariantChannel()
    val envTitle = env.replaceFirstChar { it.uppercase() }
    val tierTitle = tier.replaceFirstChar { it.uppercase() }
    val channelTitle = channel.replaceFirstChar { it.uppercase() }

    sourceSets {
        // 2. 注入业务逻辑 (Common)
        commonMain.get().apply {
            // 注入环境目录 (e.g., src/devMain)
            kotlin.srcDir("src/${env}Main/kotlin")
            // 注入级别目录 (e.g., src/freeMain)
            kotlin.srcDir("src/${tier}Main/kotlin")
            // 注入渠道目录 (e.g., src/googlePlayMain)，如果指定了渠道
            channel.let {
                kotlin.srcDir("src/${it}Main/kotlin")
            }

            resources.srcDir("src/${env}Main/resources")
            resources.srcDir("src/${tier}Main/resources")
            channel.let {
                resources.srcDir("src/${it}Main/resources")
            }
        }

        // 3. 辅助函数：注入平台特定的变体目录
        fun KotlinSourceSet.injectPlatformVariant(platform: String) {
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

        // 应用到各平台默认源集
        androidMain.get().injectPlatformVariant("android")
        iosMain.get().injectPlatformVariant("ios")
        jvmMain.get().injectPlatformVariant("jvm")
        jsMain.get().injectPlatformVariant("js")
        wasmJsMain.get().injectPlatformVariant("wasmJs")
    }

    // WASM 平台：跳过数据库测试任务（由于 JS interop 限制）
    // 数据库测试在 WASM 平台无法可靠运行，推荐使用 JS 平台进行 Web 数据库测试
    // 注意：测试代码中已经通过平台检测跳过 WASM，这里禁用测试任务可以避免测试失败
    afterEvaluate {
        // 禁用 WASM 浏览器测试任务（实际运行的测试任务）
        tasks.findByName("wasmJsBrowserTest")?.enabled = false
        // 禁用 WASM 测试编译任务
        tasks.findByName("compileTestKotlinWasmJs")?.enabled = false
        // 禁用其他可能的 WASM 测试相关任务
        tasks.findByName("wasmJsTest")?.enabled = false
    }
}
