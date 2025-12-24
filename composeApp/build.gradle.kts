import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Kotlin Multiplatform 插件（必需：KMP 项目基础）
    alias(libs.plugins.kotlinMultiplatform)
    // Android KMP 库插件（必需：支持 Android 平台）
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    // Compose Multiplatform 插件（必需：使用 Compose Multiplatform UI）
    alias(libs.plugins.composeMultiplatform)
    // Compose 编译器插件（必需：编译 Compose 代码）
    alias(libs.plugins.composeCompiler)
    // Compose 热重载插件（可选：开发时的热重载功能）
    alias(libs.plugins.composeHotReload)
    // Kotlin 序列化插件（必需：使用 @Serializable）
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.resources"
    generateResClass = always
}

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        namespace = "tech.zhifu.app.myhub"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            freeCompilerArgs += listOf("-Xbinary=bundleId=tech.zhifu.app.myhub")
            linkerOpts += listOf("-lsqlite3")
        }
    }

    jvm()

    js {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            // ========== Compose UI 依赖 ==========
            // Compose 运行时（必需：所有 Compose 组件的基础）
            implementation(compose.runtime)
            // Compose 基础组件（必需：布局、滚动等）
            implementation(compose.foundation)
            // Material3 组件库（必需：Material Design 3 组件）
            implementation(compose.material3)
            // Compose UI 核心（必需：UI 组件和工具）
            implementation(compose.ui)
            // Compose 资源组件（必需：使用 composeResources）
            implementation(compose.components.resources)
            // Compose Preview 工具（必需：@Preview 注解）
            implementation(compose.components.uiToolingPreview)
            // Material Icons 扩展（必需：Icons.Default.* 图标）
            implementation(compose.materialIconsExtended)

            // ========== AndroidX Lifecycle 依赖 ==========
            // ViewModel Compose 集成（可选：DashboardViewModel 存在但未使用 ViewModel 功能）
            // 如果未来需要使用 ViewModel，需要保留此依赖
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            // Lifecycle Runtime Compose（可选：LaunchedEffect 来自 compose.runtime，不是此依赖）
            // 如果不需要 lifecycle 相关的状态管理，可以考虑移除
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // ========== 本地存储依赖 ==========
            // Multiplatform Settings（必需：SettingsManager 使用 Settings）
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings)

            // ========== Kotlinx 库依赖 ==========
            // Kotlin 序列化（必需：SettingsManager 使用 Json，AppConfig 使用 @Serializable）
            implementation(libs.kotlinx.serialization.json)
            // Kotlin 协程（必需：SettingsManager 使用 CoroutineScope, launch, delay）
            implementation(libs.kotlinx.coroutines.core)
            // Kotlin 日期时间（可选：代码中未使用，可以考虑移除）
            implementation(libs.kotlinx.datetime)

            // ========== 依赖注入 ==========
            // Koin 核心（必需：initKoin 使用）
            implementation(libs.koin.core)
            // Koin Compose ViewModel（可选：如果使用 ViewModel，需要此依赖）
            implementation(libs.koin.compose.viewmodel)

            // ========== 项目模块依赖 ==========
            // 平台抽象层（必需：平台特定实现）
            implementation(projects.core.platform)
            // 本地存储层（必需：LocalAppTheme, LocalAppLocale 等）
            implementation(projects.core.local)
            // 数据层（必需：数据存储和网络）
            implementation(projects.core.datastore)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "tech.zhifu.app.myhub.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "tech.zhifu.app.myhub"
            packageVersion = "1.0.0"
        }
    }
}
