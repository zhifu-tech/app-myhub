import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.web)
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
    android {
        namespace = "tech.zhifu.app.myhub"
    }

    iosTargets().forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            freeCompilerArgs += listOf("-Xbinary=bundleId=tech.zhifu.app.myhub")
            linkerOpts += listOf("-lsqlite3")
        }
    }

    js {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    @Suppress("OPT_IN_USAGE")
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
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
            if (project.isDev()) {
                // Compose Preview 工具（必需：@Preview 注解）
                implementation(compose.components.uiToolingPreview)
            }
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
            // App Build Config（必需：应用构建配置）
            implementation(projects.core.appBuildConfig)
            // 平台抽象层（必需：平台特定实现，包含日志工具）
            implementation(projects.core.platform)
            // 平台 Compose UI 组件（必需：WindowSize 相关功能，LocalAppTheme, LocalAppLocale 等）
            implementation(projects.core.platformCompose)
            // 日志
            implementation(projects.core.logger)
            // 统计框架
            implementation(projects.core.analytics)
            // 数据层（必需：数据存储和网络）
            implementation(projects.core.datastoreModel)
            implementation(projects.core.datastoreRepositoryClient)
            // Feature 模块
            implementation(projects.feature.settings)
            implementation(projects.feature.dashboard)
            implementation(projects.feature.profile)
            // Component 模块
            implementation(projects.component.card)
            implementation(projects.component.mixed)
        }

        jvmMain.dependencies {
            // Desktop 相关
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }

        wasmJsMain.dependencies {
            // Webpack 插件依赖
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            implementation(npm("sql.js", "1.12.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
        }

        jsMain.dependencies {
            // Webpack 插件依赖
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            implementation(npm("sql.js", "1.12.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
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
