plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.feature.dashboard.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.dashboard"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.cardApi)
            implementation(projects.feature.dashboardApi)
            // Compose UI 依赖
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // Material Icons 扩展（必需：Icons.Default.* 图标）
            implementation(compose.materialIconsExtended)

            // Kotlinx 库依赖
            implementation(libs.kotlinx.coroutines.core)

            // 依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.jb.androidx.window.windowCore)

            // 项目模块依赖
            implementation(projects.core.platform)
            implementation(projects.core.platformCompose)
            implementation(projects.core.logger)
            implementation(projects.core.navigation)

            implementation(projects.datastore.model)
            implementation(projects.datastore.repositoryClient)

            // Component 模块
            implementation(projects.component.card)

            // Feature 模块
            implementation(projects.feature.card) // card 模块

            // Preview 支持（仅在 dev 环境）
            if (project.isDev()) {
                implementation(compose.components.uiToolingPreview)
            }
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}

