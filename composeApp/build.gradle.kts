import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.myhub.kmp.web)
    // Compose Multiplatform 插件（必需：使用 Compose Multiplatform UI）
    alias(libs.plugins.jb.composeMultiplatform)
    // Compose 编译器插件（必需：编译 Compose 代码）
    alias(libs.plugins.jb.composeCompiler)
    // Compose 热重载插件（可选：开发时的热重载功能）
    alias(libs.plugins.composeHotReload)
    // Kotlin 序列化插件（必需：使用 @Serializable）
    alias(libs.plugins.kotlinSerialization)
    // CocoaPods 支持（用于 iOS 平台依赖管理）
    kotlin("native.cocoapods")
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
        // 为 CocoaPods 构建的 Framework 也添加 SQLite3 链接
        // 注意：podDebugFramework 和 podReleaseFramework 需要单独配置
        iosTarget.binaries.all {
            if (this is org.jetbrains.kotlin.gradle.plugin.mpp.Framework) {
                linkerOpts += listOf("-lsqlite3")
            }
        }
    }

    // CocoaPods 配置（默认使用 CocoaPods 管理 iOS 依赖）
    cocoapods {
        // Pod 仓库摘要
        summary = "MyHub Compose App"
        // Pod 主页
        homepage = "https://github.com/zhifu-tech/app-myhub"
        // Pod 版本
        version = "1.0.0"
        // Pod 名称（可选，默认使用 Gradle 项目名称）
        name = "composeApp"

        // 框架配置
        // 注意：使用静态链接可能有助于解决 Firebase 依赖链接问题
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        // 指定 Podfile 路径（关键：让 CocoaPods 插件自动管理）
        podfile = project.file("../iosApp/Podfile")

        // iOS 部署目标（使用外部定义的变量）
        ios.deploymentTarget = "15.0"

        // 添加 SQLite3 库链接（用于 SQLDelight NativeSqliteDriver）
        // 注意：
        // 1. linkerOpts 用于 Gradle 构建 Framework 时链接 SQLite3
        // 2. extraSpecAttributes["libraries"] 用于 podspec，告诉 Xcode 项目需要链接系统库
        // 3. 对于静态 Framework，Framework 本身已包含 SQLite3 符号，但 podspec 中的声明
        //    可以确保 Xcode 项目正确配置链接器标志（即使 Config.xcconfig 中已有 OTHER_LDFLAGS）
        // 4. 建议保留此配置，以确保 CocoaPods/Xcode 构建的一致性
        extraSpecAttributes["libraries"] = "'sqlite3'"

        // 注意：Firebase pods 不需要在 cocoapods 块中手动添加
        // dev.gitlive:firebase-analytics 会自动处理 FirebaseCore 和 FirebaseAnalytics 的 CocoaPods 依赖
        // 在 cocoapods 块中手动添加会导致符号重复定义错误（symbol multiply defined）
        // Firebase pods 应该在 Podfile 中直接添加（见 iosApp/Podfile），这样可以在 Xcode 构建时正确链接
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
            implementation(projects.feature.card)
            implementation(projects.feature.cardApi)
            implementation(projects.feature.dashboard)
            implementation(projects.feature.dashboardApi)
            implementation(projects.feature.favorite)
            implementation(projects.feature.favoriteApi)
            implementation(projects.feature.profile)
            implementation(projects.feature.profileApi)
            implementation(projects.feature.settings)

            implementation(projects.component.card)
            implementation(projects.component.mixed)

            implementation(projects.core.analytics)
            implementation(projects.core.appBuildConfig)
            implementation(projects.core.logger)
            implementation(projects.core.navigation)
            implementation(projects.core.platform)
            implementation(projects.core.platformCompose)

            implementation(projects.datastore.model)
            implementation(projects.datastore.repositoryClient)

            implementation(libs.jb.androidx.lifecycle.lifecycleRuntimeCompose)
            implementation(libs.jb.androidx.lifecycle.lifecycleViewModelCompose)

            implementation(libs.jb.androidx.window.windowCore)

            implementation(libs.jb.compose.material3.adaptive.adaptive)
            implementation(libs.jb.compose.material3.adaptive.navigationSuite)
            implementation(libs.jb.compose.components.componentsResources)
            implementation(libs.jb.compose.foundation.foundation)
            implementation(libs.jb.compose.material.materialIconsExtend)
            implementation(libs.jb.compose.material3.material3)
            implementation(libs.jb.compose.runtime.runtime)
            implementation(libs.jb.compose.ui.ui)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)

            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.core)

            if (project.isDev()) {
                implementation(libs.jb.compose.ui.uiToolingPreview)
            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        wasmJsMain.dependencies {
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            implementation(npm("sql.js", "1.12.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
        }

        jsMain.dependencies {
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            implementation(npm("sql.js", "1.12.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.2.1"))
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(libs.jb.compose.ui.uiTooling)
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
