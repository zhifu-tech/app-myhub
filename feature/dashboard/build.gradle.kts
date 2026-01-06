plugins {
    alias(libs.plugins.myhub.kmp)
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

            // 项目模块依赖
            implementation(projects.core.platform)
            implementation(projects.core.platformCompose)
            implementation(projects.core.logger)

            implementation(projects.core.datastoreModel)
            implementation(projects.core.datastoreRepositoryClient)

            // Component 模块
            implementation(projects.component.card)

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

