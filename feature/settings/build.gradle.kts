plugins {
    alias(libs.plugins.myhub.kmp)
    alias(libs.plugins.myhub.kmp.android)
    alias(libs.plugins.myhub.kmp.ios)
    alias(libs.plugins.myhub.kmp.jvm)
    alias(libs.plugins.myhub.kmp.js)
    alias(libs.plugins.myhub.kmp.wasmJs)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "tech.zhifu.app.myhub.feature.settings.resources"
    generateResClass = always
}

kotlin {
    android {
        namespace = "tech.zhifu.app.myhub.feature.settings"
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
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // 本地存储依赖
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings)

            // 依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)

            // 项目模块依赖
            implementation(projects.core.platform)
            implementation(projects.core.platformCompose)
            implementation(projects.core.logger)

            implementation(projects.core.datastoreModel)
            implementation(projects.core.datastoreRepositoryClient)
            // Preview 支持
            if (project.isDev()) {
                implementation(compose.components.uiToolingPreview)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    "androidRuntimeClasspath"(compose.uiTooling)
}
